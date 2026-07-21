package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.notificationhandlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CMR_HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.CMR_LISTING;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.CMR_RE_LISTING;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;

/**
 * Gating logic for AiP-manual CMR (re)listing handlers: listing keeps its own handler/templates,
 * while re-listing routes appellant notification through the postal (precompiled letter) generator
 * instead of email/sms, and neither fires for paper hearings.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CmrRelistingAipManualNotificationHandlerTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private NotificationGenerator notificationGenerator;

    private List<NotificationGenerator> notificationGenerators;

    private final CmrNotificationHandlerConfiguration handlerConfiguration = new CmrNotificationHandlerConfiguration();

    @BeforeEach
    void setup() {
        notificationGenerators = Collections.singletonList(notificationGenerator);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
    }

    private void setHearingChannel(String code) {
        DynamicList channel = new DynamicList(
            new Value(code, code),
            List.of(new Value("INTER", "In Person"), new Value("VID", "Video"), new Value("TEL", "Telephone"), new Value("NA", "Not in Attendance"))
        );
        when(asylumCase.read(CMR_HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(channel));
    }

    @Test
    void listing_handler_only_fires_for_cmr_listing_event() {
        PreSubmitCallbackHandler<AsylumCase> listingHandler =
            handlerConfiguration.cmrListingAipManualNotificationHandler(notificationGenerators);

        setHearingChannel("INTER");

        when(callback.getEvent()).thenReturn(CMR_LISTING);
        assertThat(listingHandler.canHandle(ABOUT_TO_SUBMIT, callback)).isTrue();

        when(callback.getEvent()).thenReturn(CMR_RE_LISTING);
        assertThat(listingHandler.canHandle(ABOUT_TO_SUBMIT, callback)).isFalse();
    }

    @Test
    void relisting_appellant_postal_handler_fires_for_in_person_and_remote_but_not_paper() {
        PreSubmitCallbackHandler<AsylumCase> relistingPostalHandler =
            handlerConfiguration.cmrRelistingAipManualAppellantPostalNotificationHandler(notificationGenerators);

        when(callback.getEvent()).thenReturn(CMR_RE_LISTING);

        setHearingChannel("INTER");
        assertThat(relistingPostalHandler.canHandle(ABOUT_TO_SUBMIT, callback)).isTrue();

        setHearingChannel("VID");
        assertThat(relistingPostalHandler.canHandle(ABOUT_TO_SUBMIT, callback)).isTrue();

        setHearingChannel("NA");
        assertThat(relistingPostalHandler.canHandle(ABOUT_TO_SUBMIT, callback)).isFalse();
    }

    @Test
    void relisting_appellant_postal_handler_does_not_fire_for_cmr_listing_event() {
        PreSubmitCallbackHandler<AsylumCase> relistingPostalHandler =
            handlerConfiguration.cmrRelistingAipManualAppellantPostalNotificationHandler(notificationGenerators);

        setHearingChannel("INTER");
        when(callback.getEvent()).thenReturn(CMR_LISTING);

        assertThat(relistingPostalHandler.canHandle(ABOUT_TO_SUBMIT, callback)).isFalse();
    }
}
