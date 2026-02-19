package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NLR_EMAIL;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SendInviteToNonLegalRepPersonalisationTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private final Long caseId = 12345L;
    private final String templateId = "afterListingTemplateId";
    private final String aipFrontendUrl = "http://localhost";
    private final String nlrEmail = "nonLegalRep@email.com";
    private final String appealReferenceNumber = "hmctsReference";
    private final String homeOfficeReference = "homeOfficeReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";

    private SendInviteToNonLegalRepPersonalisation sendInviteToNonLegalRepPersonalisation;

    @BeforeEach
    public void setUp() {
        sendInviteToNonLegalRepPersonalisation = new SendInviteToNonLegalRepPersonalisation(
            templateId,
            aipFrontendUrl,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_nlr_email_address() {
        when(asylumCase.read(NLR_EMAIL, String.class)).thenReturn(Optional.of(nlrEmail));
        assertEquals(Collections.singleton(nlrEmail),
            sendInviteToNonLegalRepPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_throw_if_no_nlr_email_address() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> sendInviteToNonLegalRepPersonalisation.getRecipientsList(asylumCase));
        assertEquals("NLR email address is not present", exception.getMessage());
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId,
            sendInviteToNonLegalRepPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_SEND_INVITE_TO_NON_LEGAL_REP",
            sendInviteToNonLegalRepPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReference));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        Map<String, String> customerServicesPersonalisation = Map.of(
            "customerServicesTelephone", customerServicesTelephone,
            "customerServicesEmail", customerServicesEmail
        );
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(customerServicesPersonalisation);

        Map<String, String> personalisation =
            sendInviteToNonLegalRepPersonalisation.getPersonalisation(callback);

        assertFalse(personalisation.isEmpty());
        assertEquals(personalisation.get("appealReferenceNumber"), appealReferenceNumber);
        assertEquals(personalisation.get("homeOfficeReferenceNumber"), homeOfficeReference);
        assertEquals(personalisation.get("appellantGivenNames"), appellantGivenNames);
        assertEquals(personalisation.get("appellantFamilyName"), appellantFamilyName);
        assertEquals(personalisation.get("customerServicesTelephone"), customerServicesTelephone);
        assertEquals(personalisation.get("customerServicesEmail"), customerServicesEmail);
        assertEquals(personalisation.get("Hyperlink to service"), aipFrontendUrl);
    }

    @Test
    public void should_throw_exception_when_callback_is_null() {

        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> sendInviteToNonLegalRepPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}
