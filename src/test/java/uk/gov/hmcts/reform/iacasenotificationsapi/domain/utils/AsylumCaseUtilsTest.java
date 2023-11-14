package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isLegalRepEjp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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

    private final String legalRepEmailEjp = "legalRep@example.com";




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
    @Test
    public void testIsLegalRepEjp() {

        Mockito.when(asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class)).thenReturn(Optional.of(legalRepEmailEjp));
        assertTrue(isLegalRepEjp(asylumCase));
    }
    @Test
    public void testIsNotLegalRepEjp() {
        Mockito.when(asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class)).thenReturn(Optional.empty());
        assertFalse(isLegalRepEjp(asylumCase));
    }
}
