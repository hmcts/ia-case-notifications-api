package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantFtpaApplicationDecisionPersonalisationEmailTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private final String respondentGrantedPartiallyGrantedEmailTemplateId = "respondentGrantedPartiallyGrantedEmailTemplateId";
    private final String respondentNotAdmittedEmailTemplateId = "respondentNotAdmittedEmailTemplateId";
    private final String respondentRefusedEmailTemplateId = "respondentRefusedEmailTemplateId";
    private final String appellantGrantedEmailTemplateId = "appellantGrantedEmailTemplateId";
    private final String appellantPartiallyGrantedEmailTemplateId = "appellantPartiallyGrantedEmailTemplateId";
    private final String appellantNotAdmittedEmailTemplateId = "appellantNotAdmittedEmailTemplateId";
    private final String appellantRefusedEmailTemplateId = "appellantRefusedEmailTemplateId";


    private final String iaAipFrontendUrl = "http://localhost";

    private final String mockedAriaListingReferenceNumber = "ariaListingReferenceNumber";

    private final String homeOfficeReferenceNumber = "someHOReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final long oocDays = 28;
    private final long inCountryDays = 14;
    private final LocalDate today = LocalDate.now();
    private final String expectedDueDateOoc = today.plusDays(oocDays).format(DateTimeFormatter.ofPattern("d MMM yyyy"));
    private final String expectedDueDateInCountry = today.plusDays(inCountryDays).format(DateTimeFormatter.ofPattern("d MMM yyyy"));

    private AppellantFtpaApplicationDecisionPersonalisationEmail appellantFtpaApplicationDecisionPersonalisationEmail;

    @BeforeEach
    public void setup() {

        String appealReferenceNumber = "someReferenceNumber";
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(mockedAriaListingReferenceNumber));
        String customerServicesEmail = "cust.services@example.com";
        String customerServicesTelephone = "555 555 555";
        when((customerServicesProvider.getCustomerServicesPersonalisation())).thenReturn(
            Map.of(
                "customerServicesTelephone", customerServicesTelephone,
                "customerServicesEmail", customerServicesEmail
            ));

        appellantFtpaApplicationDecisionPersonalisationEmail = new AppellantFtpaApplicationDecisionPersonalisationEmail(
            respondentGrantedPartiallyGrantedEmailTemplateId,
            respondentNotAdmittedEmailTemplateId,
            respondentRefusedEmailTemplateId,
            appellantGrantedEmailTemplateId,
            appellantPartiallyGrantedEmailTemplateId,
            appellantNotAdmittedEmailTemplateId,
            appellantRefusedEmailTemplateId,
            iaAipFrontendUrl,
            oocDays,
            inCountryDays,
            recipientsFinder,
            customerServicesProvider);

    }

    static Stream<Arguments> decisionScenarios() {
        return Stream.of(
            Arguments.of(Optional.of(FTPA_GRANTED), Optional.empty()),
            Arguments.of(Optional.of(FTPA_PARTIALLY_GRANTED), Optional.empty()),
            Arguments.of(Optional.of(FTPA_NOT_ADMITTED), Optional.empty()),
            Arguments.of(Optional.of(FTPA_REFUSED), Optional.empty()),
            Arguments.of(Optional.empty(), Optional.of(FTPA_GRANTED)),
            Arguments.of(Optional.empty(), Optional.of(FTPA_PARTIALLY_GRANTED)),
            Arguments.of(Optional.empty(), Optional.of(FTPA_NOT_ADMITTED)),
            Arguments.of(Optional.empty(), Optional.of(FTPA_REFUSED))
        );
    }

    @ParameterizedTest
    @MethodSource("decisionScenarios")
    public void should_return_given_template_id_for_respondent_ftpa_decision(Optional<FtpaDecisionOutcomeType> ljDecision, Optional<FtpaDecisionOutcomeType> rjDecision) {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(ApplicantType.RESPONDENT));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(ljDecision);
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(rjDecision);

        if (ljDecision.map(decision -> decision.equals(FTPA_GRANTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_GRANTED)).orElse(false)
            || ljDecision.map(decision -> decision.equals(FTPA_PARTIALLY_GRANTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_PARTIALLY_GRANTED)).orElse(false)) {

            assertEquals(respondentGrantedPartiallyGrantedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)) {

            assertEquals(respondentNotAdmittedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)) {

            assertEquals(respondentRefusedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        }
    }

    @ParameterizedTest
    @MethodSource("decisionScenarios")
    public void should_return_given_template_id_for_appellant_ftpa_decision(Optional<FtpaDecisionOutcomeType> ljDecision, Optional<FtpaDecisionOutcomeType> rjDecision) {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(ApplicantType.APPELLANT));
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(ljDecision);
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(rjDecision);

        if (ljDecision.map(decision -> decision.equals(FTPA_GRANTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_GRANTED)).orElse(false)) {

            assertEquals(appellantGrantedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_PARTIALLY_GRANTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_PARTIALLY_GRANTED)).orElse(false)) {

            assertEquals(appellantPartiallyGrantedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)) {

            assertEquals(appellantNotAdmittedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)) {

            assertEquals(appellantRefusedEmailTemplateId, appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        }
    }

    @Test
    public void should_throw_error_if_ftpa_applicant_type_missing() {
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        assertEquals("ftpaApplicantType is not present", exception.getMessage());
    }

    @Test
    public void should_throw_error_if_applicant_type_appellant_and_ftpa_appellant_decision_missing() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(ApplicantType.APPELLANT));
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        assertEquals("ftpaAppellantDecisionOutcomeType is not present", exception.getMessage());
    }

    @Test
    public void should_throw_error_if_applicant_type_respondent_and_ftpa_respondent_decision_missing() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(ApplicantType.RESPONDENT));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        assertEquals("ftpaRespondentDecisionOutcomeType is not present", exception.getMessage());
    }

    @Test
    public void should_throw_error_if_applicant_type_is_neither_respondent_nor_appellant() {
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantFtpaApplicationDecisionPersonalisationEmail.getTemplateId(asylumCase));
        assertEquals("ftpaApplicantType is not present", exception.getMessage());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_FTPA_APPLICATION_DECISION_TO_APPELLANT_EMAIL",
            appellantFtpaApplicationDecisionPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        String mockedAppellantEmail = "fake@faketest.com";
        String mockedAppellantMobilePhone = "07123456789";
        Subscriber subscriber = new Subscriber(
            SubscriberType.APPELLANT, //subscriberType
            mockedAppellantEmail, //email
            YES, // wants email
            mockedAppellantMobilePhone, //mobileNumber
            YES // wants sms
        );

        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL)).thenCallRealMethod();
        when(asylumCase.read(SUBSCRIPTIONS))
            .thenReturn(Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber))));

        assertTrue(appellantFtpaApplicationDecisionPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmail));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL)).thenCallRealMethod();

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantFtpaApplicationDecisionPersonalisationEmail.getRecipientsList(null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }


    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void should_return_personalisation_when_all_information_given(YesOrNo appellantInUk) {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(ApplicantType.RESPONDENT));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(FTPA_GRANTED));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(appellantInUk));

        Map<String, String> personalisation =
            appellantFtpaApplicationDecisionPersonalisationEmail.getPersonalisation(asylumCase);

        String mockedAppealReferenceNumber = "someReferenceNumber";
        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", mockedAppealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToService", iaAipFrontendUrl);
        assertEquals("\nListing reference: " + mockedAriaListingReferenceNumber,
            personalisation.get("listingReferenceLine"));
        assertNotNull(personalisation.get("applicationDecision"));
        assertEquals(appellantInUk.equals(YES)
                ? expectedDueDateInCountry
                : expectedDueDateOoc,
            personalisation.get("dueDate"));

    }

    @ParameterizedTest
    @MethodSource("decisionScenarios")
    public void should_correctly_provide_appropriate_phrasing_when_decision_made(Optional<FtpaDecisionOutcomeType> ljDecision, Optional<FtpaDecisionOutcomeType> rjDecision) {
        Set.of(ApplicantType.RESPONDENT, ApplicantType.APPELLANT)
            .forEach(applicantType -> {

                when(asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(applicantType));

                if (applicantType.getValue().equals("respondent")) {
                    when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(ljDecision);
                    when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(rjDecision);
                } else if (applicantType.getValue().equals("appellant")) {
                    when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(ljDecision);
                    when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(rjDecision);
                }

                Map<String, String> personalisation =
                    appellantFtpaApplicationDecisionPersonalisationEmail.getPersonalisation(asylumCase);

                if (ljDecision.map(decision -> decision.equals(FTPA_GRANTED)).orElse(false)
                    || rjDecision.map(decision -> decision.equals(FTPA_GRANTED)).orElse(false)) {

                    assertEquals("granted", personalisation.get("applicationDecision"));
                }

                if (ljDecision.map(decision -> decision.equals(FTPA_PARTIALLY_GRANTED)).orElse(false)
                    || rjDecision.map(decision -> decision.equals(FTPA_PARTIALLY_GRANTED)).orElse(false)) {

                    assertEquals("partially granted", personalisation.get("applicationDecision"));
                }

                if (ljDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)
                    || rjDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)) {

                    assertEquals("not admitted", personalisation.get("applicationDecision"));
                }

                if (ljDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)
                    || rjDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)) {

                    assertEquals("refused", personalisation.get("applicationDecision"));
                }
            });
    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(ApplicantType.RESPONDENT));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(FTPA_GRANTED));

        Map<String, String> personalisation =
            appellantFtpaApplicationDecisionPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("homeOfficeReferenceNumber", "")
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("linkToService", iaAipFrontendUrl)
            .containsEntry("applicationDecision", "granted")
            .containsEntry("dueDate", "");
    }
}
