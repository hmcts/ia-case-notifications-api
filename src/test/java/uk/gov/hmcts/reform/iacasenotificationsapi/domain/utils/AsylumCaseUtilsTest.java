package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;


@ExtendWith(MockitoExtension.class)
public class AsylumCaseUtilsTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Document document;

    private final String legalOfficerAddendumUploadedByLabel = "TCW";
    private final String legalOfficerAddendumUploadSuppliedByLabel = "The respondent";
    private final IdValue<DocumentWithMetadata> addendumOne = new IdValue<>(
            "1",
            new DocumentWithMetadata(
                    document,
                    "Some description",
                    "2018-12-25",
                    DocumentTag.ADDENDUM_EVIDENCE,
                    legalOfficerAddendumUploadSuppliedByLabel,
                    legalOfficerAddendumUploadedByLabel
            )
    );

    private final IdValue<DocumentWithMetadata> addendumTwo = new IdValue<>(
            "2",
            new DocumentWithMetadata(
                    document,
                    "Some description",
                    "2018-12-26", DocumentTag.ADDENDUM_EVIDENCE,
                    legalOfficerAddendumUploadSuppliedByLabel,
                    legalOfficerAddendumUploadedByLabel
            )
    );



    @Test
    void should_return_correct_value_for_det() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.isAppellantInDetention(asylumCase));
    }

    @Test
    void should_return_correct_value_for_ada() {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase));
    }

    @Test
    void should_return_correct_value_for_aaa() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));
        assertTrue(AsylumCaseUtils.isAgeAssessmentAppeal(asylumCase));
    }

    @Test
    void isAdmin_should_return_true() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.isInternalCase(asylumCase));
    }

    @Test
    void isAdmin_should_return_false() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(AsylumCaseUtils.isInternalCase(asylumCase));
    }

    @Test
    void isAipJourney_should_return_true() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertTrue(AsylumCaseUtils.isAipJourney(asylumCase));
    }

    @Test
    void isAipJourney_should_return_false() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        assertFalse(AsylumCaseUtils.isAipJourney(asylumCase));
    }

    @Test
    void getFtpaDecisionOutcomeType_should_return_granted() {
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_GRANTED));
        assertEquals(FtpaDecisionOutcomeType.FTPA_GRANTED, AsylumCaseUtils.getFtpaDecisionOutcomeType(asylumCase).orElse(null));
    }

    @Test
    void getFtpaDecisionOutcomeType_should_return_refused() {
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_REFUSED));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.empty());
        assertEquals(FtpaDecisionOutcomeType.FTPA_REFUSED, AsylumCaseUtils.getFtpaDecisionOutcomeType(asylumCase).orElse(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"birmingham", "", "harmondsworth"})
    void isListed_should_return_correct_value(String hearingCentre) {
        Optional<HearingCentre> mayBeListCaseHearingCenter = HearingCentre.from(hearingCentre);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(mayBeListCaseHearingCenter);
        assertEquals(mayBeListCaseHearingCenter.isPresent(), AsylumCaseUtils.isAppealListed(asylumCase));
    }

    @Test
    void should_get_addendum_document_when_present() {
        List<IdValue<DocumentWithMetadata>> addendumDocuments = new ArrayList<>();
        addendumDocuments.add(addendumOne);
        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(addendumDocuments));

        assertEquals(addendumDocuments, AsylumCaseUtils.getAddendumEvidenceDocuments(asylumCase));
        assertEquals(Optional.of(addendumOne), AsylumCaseUtils.getLatestAddendumEvidenceDocument(asylumCase));
    }

    @Test
    void should_get_addendum_documents_when_more_than_one_exists() {
        List<IdValue<DocumentWithMetadata>> addendumDocuments = new ArrayList<>();
        addendumDocuments.add(addendumOne);
        addendumDocuments.add(addendumTwo);
        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(addendumDocuments));

        assertEquals(addendumDocuments, AsylumCaseUtils.getAddendumEvidenceDocuments(asylumCase));
        assertEquals(2, AsylumCaseUtils.getAddendumEvidenceDocuments(asylumCase).size());
    }

    @Test
    void should_return_empty_list_when_no_addendum_evidence_documents_present() {
        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.empty());

        assertEquals(Collections.emptyList(), AsylumCaseUtils.getAddendumEvidenceDocuments(asylumCase));
        assertEquals(Optional.empty(), AsylumCaseUtils.getLatestAddendumEvidenceDocument(asylumCase));
    }

    @ParameterizedTest
    @MethodSource("respondentEmails")
    void should_return_correct_boolean_value(List<IdValue<ApplyForCosts>> applyForCostsList) {
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        if (applyForCostsList.get(0).getValue().getRespondentToCostsOrder().equals("Legal representative")) {
            assertTrue(AsylumCaseUtils.isHomeOfficeApplicant(asylumCase));
        } else {
            assertFalse(AsylumCaseUtils.isHomeOfficeApplicant(asylumCase));
        }
    }

    @Test
    void should_throw_when_applies_for_costs_are_not_present() {
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> AsylumCaseUtils.retrieveLatestApplyForCosts(asylumCase))
            .hasMessage("Applies for costs are not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_correct_type_applicant_is_not_present() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(new IdValue<>("2", new ApplyForCosts("Wasted costs", "Home office", "Admin Officer")));

        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertThatThrownBy(() -> AsylumCaseUtils.isHomeOfficeApplicant(asylumCase))
            .hasMessage("Correct applicant type is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    static Stream<Arguments> respondentEmails() {
        return Stream.of(
            Arguments.of(List.of(new IdValue<>("1", new ApplyForCosts("Wasted costs", "Legal representative", "Home office")))),
            Arguments.of(List.of(new IdValue<>("2", new ApplyForCosts("Wasted costs", "Home office", "Legal representative"))))
        );
    }
}
