package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.SEND_DECISION_AND_REASONS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppealOutcomeAdminNotificationHandlerTest {
    private PreSubmitCallbackHandler<AsylumCase> asylumCasePreSubmitCallbackHandler;

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @BeforeEach
    void setUp() {
        NotificationHandlerConfiguration notificationHandlerConfiguration = new NotificationHandlerConfiguration();
        List<NotificationGenerator> notificationGenerators = emptyList();
        asylumCasePreSubmitCallbackHandler =
                notificationHandlerConfiguration.appealOutcomeAdminNotificationHandler(notificationGenerators);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void should_not_handle_stage_about_to_start(Event event) {
        // given
        given(callback.getEvent()).willReturn(event);

        // when
        boolean res = asylumCasePreSubmitCallbackHandler.canHandle(ABOUT_TO_START, callback);

        // then
        assertThat(res).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void should_handle_stage_about_to_submit(Event event) {
        // given
        given(callback.getEvent()).willReturn(event);

        // when
        boolean res = asylumCasePreSubmitCallbackHandler.canHandle(ABOUT_TO_SUBMIT, callback);

        // then
        assertThat(res)
                .isEqualTo(event == SEND_DECISION_AND_REASONS);
    }
}