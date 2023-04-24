package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBSCRIPTIONS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_GRANTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_REFUSED;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantRespondentFtpaApplicationDecisionPersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;

    private Long caseId = 12345L;
    private String iaAipFrontendUrl = "frontendHyperlink";
    private String referenceNumber = "someReferenceNumber";
    private String grantedPartiallyGrantedEmailTemplateId = "grantedPartiallyGrantedEmailTemplateId";
    private String notAdmittedEmailTemplateId = "notAdmittedEmailTemplateId";
    private String refusedEmailTemplateId = "refusedEmailTemplateId";
    private String mockedAppellantMobilePhone = "07123456789";

    private AppellantRespondentFtpaApplicationDecisionPersonalisationSms appellantRespondentFtpaApplicationDecisionPersonalisationSms;

    @BeforeEach
    public void setup() {
        appellantRespondentFtpaApplicationDecisionPersonalisationSms = new AppellantRespondentFtpaApplicationDecisionPersonalisationSms(
            grantedPartiallyGrantedEmailTemplateId,
            notAdmittedEmailTemplateId,
            refusedEmailTemplateId,
            iaAipFrontendUrl,
            recipientsFinder);
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
    public void should_return_given_template_id(Optional<FtpaDecisionOutcomeType> ljDecision, Optional<FtpaDecisionOutcomeType> rjDecision) {
        Mockito.when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(ljDecision);
        Mockito.when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(rjDecision);

        if (ljDecision.map(decision -> decision.equals(FTPA_GRANTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_GRANTED)).orElse(false)
            || ljDecision.map(decision -> decision.equals(FTPA_PARTIALLY_GRANTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_PARTIALLY_GRANTED)).orElse(false)) {

            assertEquals(grantedPartiallyGrantedEmailTemplateId, appellantRespondentFtpaApplicationDecisionPersonalisationSms.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_NOT_ADMITTED)).orElse(false)) {

            assertEquals(notAdmittedEmailTemplateId, appellantRespondentFtpaApplicationDecisionPersonalisationSms.getTemplateId(asylumCase));
        }

        if (ljDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)
            || rjDecision.map(decision -> decision.equals(FTPA_REFUSED)).orElse(false)) {

            assertEquals(refusedEmailTemplateId, appellantRespondentFtpaApplicationDecisionPersonalisationSms.getTemplateId(asylumCase));
        }
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_RESPONDENT_FTPA_APPLICATION_DECISION_TO_APPELLANT_SMS",
            appellantRespondentFtpaApplicationDecisionPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_phone_number_list_from_subscribers_in_asylum_case() {

        Subscriber subscriber = new Subscriber(
            SubscriberType.APPELLANT, //subscriberType
            "", //email
            YesOrNo.NO, // wants email
            mockedAppellantMobilePhone, //mobileNumber
            YesOrNo.YES // wants sms
        );

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS)).thenCallRealMethod();
        when(asylumCase.read(SUBSCRIPTIONS))
            .thenReturn(Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber))));

        assertTrue(appellantRespondentFtpaApplicationDecisionPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS)).thenCallRealMethod();

        assertThatThrownBy(() -> appellantRespondentFtpaApplicationDecisionPersonalisationSms.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }


    @Test
    public void should_return_personalisation() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(referenceNumber));
        Map<String, String> personalisation =
            appellantRespondentFtpaApplicationDecisionPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(referenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToService"));
    }
}
