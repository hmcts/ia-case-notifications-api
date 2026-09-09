package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NLR_DETAILS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SHOULD_INVITE_NLR_TO_IDAM;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.JOIN_APPEAL_CONFIRMATION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.NLR_DETAILS_UPDATED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.REMOVE_NON_LEGAL_REP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.SEND_INVITE_TO_NON_LEGAL_REP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.SEND_PIP_TO_NON_LEGAL_REP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NlrNotificationHandlerConfigurationTest {

    private static final String NLR_EMAIL = "nonlegalrep@example.com";

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    private NlrNotificationHandlerConfiguration configuration;
    private List<NotificationGenerator> notificationGenerators;

    @BeforeEach
    void setUp() {
        configuration = new NlrNotificationHandlerConfiguration();
        notificationGenerators = emptyList();

        given(callback.getCaseDetails()).willReturn(caseDetails);
        given(caseDetails.getCaseData()).willReturn(asylumCase);
    }

    @Nested
    @DisplayName("Tests for generateSendInviteToNonLegalRepNotificationHandler - "
        + "Sends IDAM invite to Non-Legal Rep when SEND_INVITE_TO_NON_LEGAL_REP or SUBMIT_APPEAL event, "
        + "AIP journey, NLR email present, and SHOULD_INVITE_NLR_TO_IDAM flag is YES")
    class SendInviteToNonLegalRepNotificationHandler {

        private PreSubmitCallbackHandler<AsylumCase> handler;

        @BeforeEach
        void setUp() {
            handler = configuration.generateSendInviteToNonLegalRepNotificationHandler(notificationGenerators);
        }

        @ParameterizedTest
        @EnumSource(value = Event.class)
        void should_not_handle_about_to_start_stage(Event event) {
            given(callback.getEvent()).willReturn(event);

            assertFalse(handler.canHandle(ABOUT_TO_START, callback));
        }

        @Test
        void should_handle_send_invite_to_non_legal_rep_event_when_conditions_met() {
            given(callback.getEvent()).willReturn(SEND_INVITE_TO_NON_LEGAL_REP);
            given(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).willReturn(Optional.of(JourneyType.AIP));
            given(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class))
                .willReturn(Optional.of(NonLegalRepDetails.builder().emailAddress(NLR_EMAIL).build()));
            given(asylumCase.read(SHOULD_INVITE_NLR_TO_IDAM, YesOrNo.class)).willReturn(Optional.of(YesOrNo.YES));

            assertTrue(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @Test
        void should_handle_submit_appeal_event_when_conditions_met() {
            given(callback.getEvent()).willReturn(SUBMIT_APPEAL);
            given(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).willReturn(Optional.of(JourneyType.AIP));
            given(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class))
                .willReturn(Optional.of(NonLegalRepDetails.builder().emailAddress(NLR_EMAIL).build()));
            given(asylumCase.read(SHOULD_INVITE_NLR_TO_IDAM, YesOrNo.class)).willReturn(Optional.of(YesOrNo.YES));

            assertTrue(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @Test
        void should_not_handle_when_not_aip_journey() {
            given(callback.getEvent()).willReturn(SEND_INVITE_TO_NON_LEGAL_REP);
            given(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).willReturn(Optional.of(JourneyType.REP));
            given(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class))
                .willReturn(Optional.of(NonLegalRepDetails.builder().emailAddress(NLR_EMAIL).build()));
            given(asylumCase.read(SHOULD_INVITE_NLR_TO_IDAM, YesOrNo.class)).willReturn(Optional.of(YesOrNo.YES));

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @Test
        void should_not_handle_when_nlr_email_is_missing() {
            given(callback.getEvent()).willReturn(SEND_INVITE_TO_NON_LEGAL_REP);
            given(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).willReturn(Optional.of(JourneyType.AIP));
            given(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).willReturn(Optional.empty());
            given(asylumCase.read(SHOULD_INVITE_NLR_TO_IDAM, YesOrNo.class)).willReturn(Optional.of(YesOrNo.YES));

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @Test
        void should_not_handle_when_nlr_email_is_empty() {
            given(callback.getEvent()).willReturn(SEND_INVITE_TO_NON_LEGAL_REP);
            given(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).willReturn(Optional.of(JourneyType.AIP));
            given(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class))
                .willReturn(Optional.of(NonLegalRepDetails.builder().emailAddress("").build()));
            given(asylumCase.read(SHOULD_INVITE_NLR_TO_IDAM, YesOrNo.class)).willReturn(Optional.of(YesOrNo.YES));

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @Test
        void should_not_handle_when_should_invite_flag_is_no() {
            given(callback.getEvent()).willReturn(SEND_INVITE_TO_NON_LEGAL_REP);
            given(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).willReturn(Optional.of(JourneyType.AIP));
            given(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class))
                .willReturn(Optional.of(NonLegalRepDetails.builder().emailAddress(NLR_EMAIL).build()));
            given(asylumCase.read(SHOULD_INVITE_NLR_TO_IDAM, YesOrNo.class)).willReturn(Optional.of(YesOrNo.NO));

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @Test
        void should_not_handle_when_should_invite_flag_is_missing() {
            given(callback.getEvent()).willReturn(SEND_INVITE_TO_NON_LEGAL_REP);
            given(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).willReturn(Optional.of(JourneyType.AIP));
            given(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class))
                .willReturn(Optional.of(NonLegalRepDetails.builder().emailAddress(NLR_EMAIL).build()));
            given(asylumCase.read(SHOULD_INVITE_NLR_TO_IDAM, YesOrNo.class)).willReturn(Optional.empty());

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @Test
        void should_not_handle_invalid_event() {
            given(callback.getEvent()).willReturn(Event.START_APPEAL);
            given(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).willReturn(Optional.of(JourneyType.AIP));
            given(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class))
                .willReturn(Optional.of(NonLegalRepDetails.builder().emailAddress(NLR_EMAIL).build()));
            given(asylumCase.read(SHOULD_INVITE_NLR_TO_IDAM, YesOrNo.class)).willReturn(Optional.of(YesOrNo.YES));

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }
    }

    @Nested
    @DisplayName("Tests for generateSendPipToNonLegalRepNotificationHandler - "
        + "Sends PIP (Progress in Person) notification to Non-Legal Rep on SEND_PIP_TO_NON_LEGAL_REP event")
    class SendPipToNonLegalRepNotificationHandler {

        private PreSubmitCallbackHandler<AsylumCase> handler;

        @BeforeEach
        void setUp() {
            handler = configuration.generateSendPipToNonLegalRepNotificationHandler(notificationGenerators);
        }

        @ParameterizedTest
        @EnumSource(value = Event.class)
        void should_not_handle_about_to_start_stage(Event event) {
            given(callback.getEvent()).willReturn(event);

            assertFalse(handler.canHandle(ABOUT_TO_START, callback));
        }

        @Test
        void should_handle_send_pip_to_non_legal_rep_event() {
            given(callback.getEvent()).willReturn(SEND_PIP_TO_NON_LEGAL_REP);

            assertTrue(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @ParameterizedTest
        @EnumSource(value = Event.class, names = {"SEND_PIP_TO_NON_LEGAL_REP"}, mode = EnumSource.Mode.EXCLUDE)
        void should_not_handle_other_events(Event event) {
            given(callback.getEvent()).willReturn(event);

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }
    }

    @Nested
    @DisplayName("Tests for generateJoinAppealConfirmationNotificationHandler - "
        + "Sends confirmation notification when Non-Legal Rep joins an appeal on JOIN_APPEAL_CONFIRMATION event")
    class JoinAppealConfirmationNotificationHandler {

        private PreSubmitCallbackHandler<AsylumCase> handler;

        @BeforeEach
        void setUp() {
            handler = configuration.generateJoinAppealConfirmationNotificationHandler(notificationGenerators);
        }

        @ParameterizedTest
        @EnumSource(value = Event.class)
        void should_not_handle_about_to_start_stage(Event event) {
            given(callback.getEvent()).willReturn(event);

            assertFalse(handler.canHandle(ABOUT_TO_START, callback));
        }

        @Test
        void should_handle_join_appeal_confirmation_event() {
            given(callback.getEvent()).willReturn(JOIN_APPEAL_CONFIRMATION);

            assertTrue(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @ParameterizedTest
        @EnumSource(value = Event.class, names = {"JOIN_APPEAL_CONFIRMATION"}, mode = EnumSource.Mode.EXCLUDE)
        void should_not_handle_other_events(Event event) {
            given(callback.getEvent()).willReturn(event);

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }
    }

    @Nested
    @DisplayName("Tests for generateNonLegalRepRemovedNotificationHandler - "
        + "Sends notification when Non-Legal Rep is removed from an appeal on REMOVE_NON_LEGAL_REP event")
    class NonLegalRepRemovedNotificationHandler {

        private PreSubmitCallbackHandler<AsylumCase> handler;

        @BeforeEach
        void setUp() {
            handler = configuration.generateNonLegalRepRemovedNotificationHandler(notificationGenerators);
        }

        @ParameterizedTest
        @EnumSource(value = Event.class)
        void should_not_handle_about_to_start_stage(Event event) {
            given(callback.getEvent()).willReturn(event);

            assertFalse(handler.canHandle(ABOUT_TO_START, callback));
        }

        @Test
        void should_handle_remove_non_legal_rep_event() {
            given(callback.getEvent()).willReturn(REMOVE_NON_LEGAL_REP);

            assertTrue(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @ParameterizedTest
        @EnumSource(value = Event.class, names = {"REMOVE_NON_LEGAL_REP"}, mode = EnumSource.Mode.EXCLUDE)
        void should_not_handle_other_events(Event event) {
            given(callback.getEvent()).willReturn(event);

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }
    }

    @Nested
    @DisplayName("Tests for generateNlrPhoneNumberSubmittedNotificationHandler - "
        + "Sends notification when Non-Legal Rep updates their phone number on NLR_DETAILS_UPDATED event, "
        + "AIP journey, and NLR email present")
    class NlrPhoneNumberSubmittedNotificationHandler {

        private PreSubmitCallbackHandler<AsylumCase> handler;

        @BeforeEach
        void setUp() {
            handler = configuration.generateNlrPhoneNumberSubmittedNotificationHandler(notificationGenerators);
        }

        @ParameterizedTest
        @EnumSource(value = Event.class)
        void should_not_handle_about_to_start_stage(Event event) {
            given(callback.getEvent()).willReturn(event);

            assertFalse(handler.canHandle(ABOUT_TO_START, callback));
        }

        @Test
        void should_handle_nlr_details_updated_event_when_conditions_met() {
            given(callback.getEvent()).willReturn(NLR_DETAILS_UPDATED);
            given(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).willReturn(Optional.of(JourneyType.AIP));
            given(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class))
                .willReturn(Optional.of(NonLegalRepDetails.builder().emailAddress(NLR_EMAIL).build()));

            assertTrue(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @Test
        void should_not_handle_when_not_aip_journey() {
            given(callback.getEvent()).willReturn(NLR_DETAILS_UPDATED);
            given(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).willReturn(Optional.of(JourneyType.REP));
            given(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class))
                .willReturn(Optional.of(NonLegalRepDetails.builder().emailAddress(NLR_EMAIL).build()));

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @Test
        void should_not_handle_when_nlr_email_is_missing() {
            given(callback.getEvent()).willReturn(NLR_DETAILS_UPDATED);
            given(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).willReturn(Optional.of(JourneyType.AIP));
            given(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).willReturn(Optional.empty());

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @Test
        void should_not_handle_when_nlr_email_is_empty() {
            given(callback.getEvent()).willReturn(NLR_DETAILS_UPDATED);
            given(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).willReturn(Optional.of(JourneyType.AIP));
            given(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class))
                .willReturn(Optional.of(NonLegalRepDetails.builder().emailAddress("").build()));

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }

        @Test
        void should_not_handle_invalid_event() {
            given(callback.getEvent()).willReturn(Event.START_APPEAL);
            given(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).willReturn(Optional.of(JourneyType.AIP));
            given(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class))
                .willReturn(Optional.of(NonLegalRepDetails.builder().emailAddress(NLR_EMAIL).build()));

            assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));
        }
    }
}
