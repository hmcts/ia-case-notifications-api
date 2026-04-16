package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre.BIRMINGHAM;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeUpdateTribunalDecisionRule31PersonalisationEmailTest {
    private final Long caseId = 12345L;
    private final String legalRepresentativeUpdateTribunalDecisionRule31EmailTemplateId = "legalRepresentativeUpdateTribunalDecisionRule31EmailTemplateId";
    private final String exUiFrontendUrl = "http://localhost";
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String legalRepReferenceNumber = "someLRReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    private LegalRepresentativeUpdateTribunalDecisionRule31PersonalisationEmail legalRepresentativeUpdateTribunalDecisionRule31PersonalisationEmail;

    @BeforeEach
    public void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);

        String appealReferenceNumber = "someReferenceNumber";
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(BIRMINGHAM));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        legalRepresentativeUpdateTribunalDecisionRule31PersonalisationEmail = new LegalRepresentativeUpdateTribunalDecisionRule31PersonalisationEmail(
            legalRepresentativeUpdateTribunalDecisionRule31EmailTemplateId,
            exUiFrontendUrl,
            customerServicesProvider
        );
    }

    @Test
    void should_return_given_template_id() {

        assertEquals(legalRepresentativeUpdateTribunalDecisionRule31EmailTemplateId, legalRepresentativeUpdateTribunalDecisionRule31PersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_LEGAL_REPRESENTATIVE_UPDATE_TRIBUNAL_DECISION_RULE_31_EMAIL",
            legalRepresentativeUpdateTribunalDecisionRule31PersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> legalRepresentativeUpdateTribunalDecisionRule31PersonalisationEmail.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_first_check_when_all_information_given() {

        DynamicList dynamicList = new DynamicList(new Value("dismissed", "Yes, change decision to Dismissed"),
            List.of(
                new Value("DISMISSED", "Yes, change decision to Dismissed"),
                new Value("ALLOWED", "No")));

        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Map<String, String> personalisation = legalRepresentativeUpdateTribunalDecisionRule31PersonalisationEmail.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", mockedAppealReferenceNumber)
            .containsEntry("legalRepReferenceNumber", legalRepReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToService", exUiFrontendUrl)
            .containsEntry("firstBulletPoint", "the appeal decision has been changed")
            .containsEntry("bothChanges", "no");
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

    }

    @Test
    void should_return_personalisation_second_check_when_all_information_given() {

        DynamicList dynamicList = new DynamicList(new Value("DISMISSED", "No"),
            List.of(
                new Value("ALLOWED", "Yes, change decision to Dismissed"),
                new Value("DISMISSED", "No")));

        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        Map<String, String> personalisation = legalRepresentativeUpdateTribunalDecisionRule31PersonalisationEmail.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", mockedAppealReferenceNumber)
            .containsEntry("legalRepReferenceNumber", legalRepReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToService", exUiFrontendUrl)
            .containsEntry("firstBulletPoint", "a new Decision and Reasons document is available to view in the documents tab")
            .containsEntry("bothChanges", "no");
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

    }

    @Test
    void should_return_personalisation_both_checks_when_all_information_given() {

        DynamicList dynamicList = new DynamicList(new Value("allowed", "Yes, change decision to Dismissed"),
            List.of(
                new Value("ALLOWED", "Yes, change decision to Dismissed"),
                new Value("DISMISSED", "No")));

        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        Map<String, String> personalisation = legalRepresentativeUpdateTribunalDecisionRule31PersonalisationEmail.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", mockedAppealReferenceNumber)
            .containsEntry("legalRepReferenceNumber", legalRepReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToService", exUiFrontendUrl)
            .containsEntry("firstBulletPoint", "the appeal decision has been changed")
            .containsEntry("bothChanges", "yes");
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

    }

}