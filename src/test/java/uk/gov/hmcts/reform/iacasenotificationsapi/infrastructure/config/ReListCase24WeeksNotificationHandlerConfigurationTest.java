package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReListCase24WeeksNotificationHandlerConfigurationTest {

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private AsylumCase asylumCaseBefore;

    private NotificationHandlerConfiguration configuration;
    private List<NotificationGenerator> notificationGenerators;

    @BeforeEach
    void setUp() {
        configuration = new NotificationHandlerConfiguration();
        notificationGenerators = emptyList();

        given(callback.getCaseDetails()).willReturn(caseDetails);
        given(caseDetails.getCaseData()).willReturn(asylumCase);
        given(callback.getCaseDetailsBefore()).willReturn(Optional.of(caseDetailsBefore));
        given(caseDetailsBefore.getCaseData()).willReturn(asylumCaseBefore);
    }

    @Nested
    @DisplayName("Tests for reListCase24WeeksNotificationHandler — "
        + "Sends Case Relisted email to Home Office on LIST_CASE event "
        + "for 24-week STF cases that are being re-listed (not first listing)")
    class ReListCase24WeeksNotificationHandler {

        private PreSubmitCallbackHandler<AsylumCase> handler;

        @BeforeEach
        void setUp() {
            handler = configuration.reListCase24WeeksNotificationHandler(notificationGenerators);
        }

        @Test
        void should_handle_when_all_conditions_met() {
            given(callback.getEvent()).willReturn(LIST_CASE);
            given(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
                .willReturn(Optional.of(YES));
            given(asylumCaseBefore.read(ARIA_LISTING_REFERENCE, String.class))
                .willReturn(Optional.of("LP/12345/2026"));

            assertTrue(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @ParameterizedTest
        @EnumSource(value = Event.class)
        void should_not_handle_about_to_start_stage(Event event) {
            given(callback.getEvent()).willReturn(event);

            assertFalse(handler.canHandle(ABOUT_TO_START, callback));
        }

        @ParameterizedTest
        @EnumSource(value = Event.class, names = {"LIST_CASE"}, mode = EnumSource.Mode.EXCLUDE)
        void should_not_handle_other_events(Event event) {
            given(callback.getEvent()).willReturn(event);
            given(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
                .willReturn(Optional.of(YES));
            given(asylumCaseBefore.read(ARIA_LISTING_REFERENCE, String.class))
                .willReturn(Optional.of("LP/12345/2026"));

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @Test
        void should_not_handle_when_not_a_24_week_case() {
            given(callback.getEvent()).willReturn(LIST_CASE);
            given(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
                .willReturn(Optional.empty());
            given(asylumCaseBefore.read(ARIA_LISTING_REFERENCE, String.class))
                .willReturn(Optional.of("LP/12345/2026"));

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @Test
        void should_not_handle_on_first_listing_when_aria_listing_reference_absent() {
            given(callback.getEvent()).willReturn(LIST_CASE);
            given(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
                .willReturn(Optional.of(YES));
            given(asylumCaseBefore.read(ARIA_LISTING_REFERENCE, String.class))
                .willReturn(Optional.empty());

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @Test
        void should_not_handle_on_first_listing_when_aria_listing_reference_blank() {
            given(callback.getEvent()).willReturn(LIST_CASE);
            given(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
                .willReturn(Optional.of(YES));
            given(asylumCaseBefore.read(ARIA_LISTING_REFERENCE, String.class))
                .willReturn(Optional.of(""));

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @Test
        void should_not_handle_when_no_before_state() {
            given(callback.getEvent()).willReturn(LIST_CASE);
            given(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
                .willReturn(Optional.of(YES));
            given(callback.getCaseDetailsBefore()).willReturn(Optional.empty());

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }
    }
}
