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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
public class AsylumCaseUtilsTest {

    @Mock
    private AsylumCase asylumCase;

    @Test
    public void appellantInDetention_should_return_true() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.isAppellantInDetention(asylumCase));
    }

    @Test
    void appellantInDetention_should_return_false() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(AsylumCaseUtils.isAppellantInDetention(asylumCase));
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
    void submissionOutOfTime_should_return_true() {
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.isSubmissionOutOfTime(asylumCase));
    }

    @Test
    void submissionOutOfTime_should_return_false() {
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(AsylumCaseUtils.isSubmissionOutOfTime(asylumCase));
    }
}