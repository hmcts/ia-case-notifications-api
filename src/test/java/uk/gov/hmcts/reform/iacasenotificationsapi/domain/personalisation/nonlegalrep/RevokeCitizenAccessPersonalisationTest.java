package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REVOKE_ACCESS_DL;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RevokeCitizenAccessPersonalisationTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private final Long caseId = 12345L;
    private final String templateId = "joinAppealConfirmationTemplateId";
    private final String email = "someEmail";
    private final Value userValue = new Value("someIdamId", email + " - someGivenNames someFamilyName");
    private final String appealReferenceNumber = "hmctsReference";
    private final String homeOfficeReference = "homeOfficeReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";

    private RevokeCitizenAccessPersonalisation revokeCitizenAccessPersonalisation;

    @BeforeEach
    public void setUp() {
        revokeCitizenAccessPersonalisation = new RevokeCitizenAccessPersonalisation(
            templateId,
            customerServicesProvider
        );
    }

    @Test
    public void recipients_list_should_throw_if_no_dl() {
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> revokeCitizenAccessPersonalisation.getRecipientsList(asylumCase));
        verify(asylumCase, times(1)).read(REVOKE_ACCESS_DL, DynamicList.class);
        assertEquals("Dynamic list of users to revoke access from is not present.", exception.getMessage());
    }

    @Test
    public void recipients_list_should_return_revoke_dl_value() {
        DynamicList dynamicList = new DynamicList(userValue, List.of(userValue));
        when(asylumCase.read(REVOKE_ACCESS_DL, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        Set<String> recipients = revokeCitizenAccessPersonalisation.getRecipientsList(asylumCase);
        verify(asylumCase, times(1)).read(REVOKE_ACCESS_DL, DynamicList.class);
        assertFalse(recipients.isEmpty());
        assertEquals(email, recipients.iterator().next());
    }

    @Test
    public void recipients_list_should_return_revoke_empty_if_dl_has_null_value() {
        DynamicList dynamicList = new DynamicList(null, List.of(userValue));
        when(asylumCase.read(REVOKE_ACCESS_DL, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        Set<String> recipients = revokeCitizenAccessPersonalisation.getRecipientsList(asylumCase);
        verify(asylumCase, times(1)).read(REVOKE_ACCESS_DL, DynamicList.class);
        assertTrue(recipients.isEmpty());
    }

    @Test
    public void recipients_list_should_return_revoke_empty_if_dl_has_invalid_value_label() {
        DynamicList dynamicList = new DynamicList(new Value("someIdamCode", "invalidLabel"), List.of(userValue));
        when(asylumCase.read(REVOKE_ACCESS_DL, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        Set<String> recipients = revokeCitizenAccessPersonalisation.getRecipientsList(asylumCase);
        verify(asylumCase, times(1)).read(REVOKE_ACCESS_DL, DynamicList.class);
        assertTrue(recipients.isEmpty());
    }

    public void should_return_given_template_id() {
        assertEquals(templateId,
            revokeCitizenAccessPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_REVOKE_CITIZEN_ACCESS_EMAIL",
            revokeCitizenAccessPersonalisation.getReferenceId(caseId));
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
            revokeCitizenAccessPersonalisation.getPersonalisation(callback);

        assertFalse(personalisation.isEmpty());
        assertEquals(personalisation.get("appealReferenceNumber"), appealReferenceNumber);
        assertEquals(personalisation.get("homeOfficeReferenceNumber"), homeOfficeReference);
        assertEquals(personalisation.get("appellantGivenNames"), appellantGivenNames);
        assertEquals(personalisation.get("appellantFamilyName"), appellantFamilyName);
        assertEquals(personalisation.get("customerServicesTelephone"), customerServicesTelephone);
        assertEquals(personalisation.get("customerServicesEmail"), customerServicesEmail);
        verify(asylumCase, times(1)).write(REVOKE_ACCESS_DL, null);
    }


    @Test
    public void should_throw_exception_when_callback_is_null() {

        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> revokeCitizenAccessPersonalisation.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }
}
