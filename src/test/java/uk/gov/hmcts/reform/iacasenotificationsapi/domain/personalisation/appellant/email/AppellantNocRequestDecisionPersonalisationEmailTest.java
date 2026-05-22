package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantNocRequestDecisionPersonalisationEmailTest {

    private final String emailTemplateId = "someEmailTemplateId";
    private final long mockedAppealReferenceNumber = 1236;
    private final String mockedAppellantGivenNames = "someAppellantGivenNames";
    private final String mockedAppellantFamilyName = "someAppellantFamilyName";
    private final String mockedAppellantEmailAddress = "appelant@example.net";
    private final String dateOfBirth = "2020-03-01";
    private final String expectedDateOfBirth = "1 Mar 2020";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "customer.services@example.com";
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    private AppellantNocRequestDecisionPersonalisationEmail appellantNocRequestDecisionPersonalisationEmail;

    @BeforeEach
    void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(callback.getCaseDetails().getId()).thenReturn(mockedAppealReferenceNumber);

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
        Long caseId = 12345L;
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

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantNocRequestDecisionPersonalisationEmail.getRecipientsList(asylumCase));
        assertEquals("appellantEmailAddress is not present", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_personalisation_when_date_of_birth_is_null() {
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantNocRequestDecisionPersonalisationEmail.getPersonalisation(callback));
        assertEquals("Appellant's birth of date is not present", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        initializePrefixes(appellantNocRequestDecisionPersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        when(caseDetails.getId()).thenReturn(mockedAppealReferenceNumber);
        Map<String, String> personalisation = appellantNocRequestDecisionPersonalisationEmail.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("Ref Number", String.valueOf(mockedAppealReferenceNumber))
            .containsEntry("Given names", mockedAppellantGivenNames)
            .containsEntry("Family name", mockedAppellantFamilyName)
            .containsEntry("Date Of Birth", expectedDateOfBirth);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_return_personalisation_when_only_mandatory_information_given(YesOrNo isAda) {

        initializePrefixes(appellantNocRequestDecisionPersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(dateOfBirth));

        Map<String, String> personalisation = appellantNocRequestDecisionPersonalisationEmail.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("Ref Number", String.valueOf(mockedAppealReferenceNumber))
            .containsEntry("Given names", "")
            .containsEntry("Family name", "")
            .containsEntry("Date Of Birth", expectedDateOfBirth);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }
}
