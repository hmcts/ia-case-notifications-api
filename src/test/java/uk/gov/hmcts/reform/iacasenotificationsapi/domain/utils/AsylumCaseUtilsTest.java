package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
public class AsylumCaseUtilsTest {

    @Mock
    private AsylumCase asylumCase;

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
}
