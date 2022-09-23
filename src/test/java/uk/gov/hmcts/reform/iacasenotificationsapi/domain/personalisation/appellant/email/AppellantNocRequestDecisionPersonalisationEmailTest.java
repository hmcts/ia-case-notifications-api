package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantNocRequestDecisionPersonalisationEmailTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    private Long caseId = 12345L;
    private String emailTemplateId = "someEmailTemplateId";


    private final Long ccdCaseId = 1234555577779999L;
    private final String ccdCaseIdFormatted = "1234-5555-7777-9999";
    private String mockedAppellantGivenNames = "someAppellantGivenNames";
    private String mockedAppellantFamilyName = "someAppellantFamilyName";
    private String mockedAppellantEmailAddress = "appelant@example.net";
    private String dateOfBirth = "2020-03-01";
    private String expectedDateOfBirth = "1 Mar 2020";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "customer.services@example.com";

    private AppellantNocRequestDecisionPersonalisationEmail appellantNocRequestDecisionPersonalisationEmail;

    @BeforeEach
    void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(callback.getCaseDetails().getId()).thenReturn(ccdCaseId);

        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(dateOfBirth));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));
        when(asylumCase.read(EMAIL, String.class)).thenReturn(Optional.of(mockedAppellantEmailAddress));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        appellantNocRequestDecisionPersonalisationEmail = new AppellantNocRequestDecisionPersonalisationEmail(
            emailTemplateId, customerServicesProvider);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(emailTemplateId, appellantNocRequestDecisionPersonalisationEmail.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_NOC_REQUEST_DECISION_APPELLANT_EMAIL",
            appellantNocRequestDecisionPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        assertTrue(appellantNocRequestDecisionPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        when(asylumCase.read(EMAIL, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantNocRequestDecisionPersonalisationEmail.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appellantEmailAddress is not present");
    }

    @Test
    void should_throw_exception_on_personalisation_when_date_of_birth_is_null() {
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantNocRequestDecisionPersonalisationEmail.getPersonalisation(callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appellant's date of birth is not present");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = appellantNocRequestDecisionPersonalisationEmail.getPersonalisation(callback);

        assertEquals(ccdCaseIdFormatted, personalisation.get("Ref Number"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("Given names"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("Family name"));
        assertEquals(expectedDateOfBirth, personalisation.get("Date Of Birth"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

    }

    @Test
    void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(dateOfBirth));



        Map<String, String> personalisation = appellantNocRequestDecisionPersonalisationEmail.getPersonalisation(callback);

        assertEquals(ccdCaseIdFormatted, personalisation.get("Ref Number"));
        assertEquals("", personalisation.get("Given names"));
        assertEquals("", personalisation.get("Family name"));
        assertEquals(expectedDateOfBirth, personalisation.get("Date Of Birth"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
