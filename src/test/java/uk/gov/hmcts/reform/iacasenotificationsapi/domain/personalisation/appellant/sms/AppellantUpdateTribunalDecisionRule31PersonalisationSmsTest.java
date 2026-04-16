package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.TYPES_OF_UPDATE_TRIBUNAL_DECISION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.UPDATED_APPEAL_DECISION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK;

import java.util.Collections;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantUpdateTribunalDecisionRule31PersonalisationSmsTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    private final String updateTribunalDecisionRule31DecisionTemplateId = "decisionTemplateId";
    private final String updateTribunalDecisionRule31DocumentTemplateId = "documentTemplateId";
    private final String updateTribunalDecisionRule31BothTemplateId  = "bothTemplateId";
    private final long mockedAppealReferenceNumber = 1236;
    private final String iaAipFrontendUrl = "http://localhost";
    private AppellantUpdateTribunalDecisionRule31PersonalisationSms appellantUpdateTribunalDecisionRule31PersonalisationSms;
    private final DynamicList dynamicAllowedDecisionList = new DynamicList(
            new Value("allowed", "Yes, change decision to Allowed"),
            newArrayList()
    );
    private final DynamicList dynamicDismissedDecisionList = new DynamicList(
            new Value("dismissed", "No"),
            newArrayList()
    );

    @BeforeEach
    void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getId()).thenReturn(mockedAppealReferenceNumber);

        String mockedAppellantGivenNames = "someAppellantGivenNames";
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        String mockedAppellantFamilyName = "someAppellantFamilyName";
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(String.valueOf(mockedAppealReferenceNumber)));

        appellantUpdateTribunalDecisionRule31PersonalisationSms =
                new AppellantUpdateTribunalDecisionRule31PersonalisationSms(
                        updateTribunalDecisionRule31DecisionTemplateId,
                        updateTribunalDecisionRule31DocumentTemplateId,
                        updateTribunalDecisionRule31BothTemplateId,
                        recipientsFinder,
                        iaAipFrontendUrl);
    }

    @Test
    void should_return_given_template_id() {
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicDismissedDecisionList));
        assertEquals(updateTribunalDecisionRule31DocumentTemplateId, appellantUpdateTribunalDecisionRule31PersonalisationSms.getTemplateId(asylumCase));

        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicAllowedDecisionList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        assertEquals(updateTribunalDecisionRule31BothTemplateId, appellantUpdateTribunalDecisionRule31PersonalisationSms.getTemplateId(asylumCase));

        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
        assertEquals(updateTribunalDecisionRule31DecisionTemplateId, appellantUpdateTribunalDecisionRule31PersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_APPELLANT_UPDATE_TRIBUNAL_DECISION_RULE_31_SMS",
            appellantUpdateTribunalDecisionRule31PersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_recipients_when_case_is_null() {
        when(recipientsFinder.findAll(null, NotificationType.SMS))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        NullPointerException exception =
assertThrows(NullPointerException.class, () -> appellantUpdateTribunalDecisionRule31PersonalisationSms.getRecipientsList(null))
            ;
assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {
        String mockedAppellantMobilePhone = "07123456789";
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantUpdateTribunalDecisionRule31PersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
assertThrows(NullPointerException.class, () -> appellantUpdateTribunalDecisionRule31PersonalisationSms.getPersonalisation((Callback<AsylumCase>) null))
            ;
assertEquals("callback must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_information_given_decision_updated(YesOrNo outOfCountry) {
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicAllowedDecisionList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
                .thenReturn(Optional.of(outOfCountry));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of("Allowed"));
        Map<String, String> personalisation = appellantUpdateTribunalDecisionRule31PersonalisationSms.getPersonalisation(callback);
        String dismissed = "Dismissed";
        String allowed = "Allowed";
        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", String.valueOf(mockedAppealReferenceNumber))
            .containsEntry("linkToService", iaAipFrontendUrl)
            .containsEntry("oldDecision", dismissed)
            .containsEntry("newDecision", allowed);
        if (outOfCountry.equals(YesOrNo.YES)) {
            String days28 = "28 days";
            assertEquals(days28, personalisation.get("period"));
        } else {
            String days14 = "14 days";
            assertEquals(days14, personalisation.get("period"));
        }
    }

    @Test
    void should_return_personalisation_when_all_information_given_decision_not_updated() {
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicDismissedDecisionList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of("Allowed"));
        Map<String, String> personalisation = appellantUpdateTribunalDecisionRule31PersonalisationSms.getPersonalisation(callback);
        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", String.valueOf(mockedAppealReferenceNumber))
            .containsEntry("linkToService", iaAipFrontendUrl);
        assertNull(personalisation.get("oldDecision"));
        assertNull(personalisation.get("newDecision"));
        assertNull(personalisation.get("period"));
    }

}