package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.notificationhandlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.CMR_RE_LISTING;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email.AppellantCmrRelistingPersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms.AppellantCmrRelistingPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.notificationgenerators.CmrNotificationGeneratorConfiguration;

/**
 * End-to-end check (real RecipientsFinder + real gating logic, only GovNotifyNotificationSender and
 * PersonalisationProvider content-building mocked) that the appellant email handler actually calls
 * sendEmail, the appellant sms handler actually calls sendSms, and that each only fires for its own
 * contact preference, for the CMR_RE_LISTING event.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CmrNonDetainedAppellantNotificationEndToEndTest {

    private static final String EMAIL_TEMPLATE_ID = "emailTemplateId";
    private static final String LEGALLY_REPPED_EMAIL_TEMPLATE_ID = "legallyReppedEmailTemplateId";
    private static final String SMS_TEMPLATE_ID = "smsTemplateId";
    private static final String LEGALLY_REPPED_SMS_TEMPLATE_ID = "legallyReppedSmsTemplateId";
    private static final String APPELLANT_EMAIL = "appellant@example.com";
    private static final String APPELLANT_MOBILE = "+447123456789";

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private GovNotifyNotificationSender notificationSender;
    @Mock
    private NotificationIdAppender notificationIdAppender;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private HearingDetailsFinder hearingDetailsFinder;
    @Mock
    private PersonalisationProvider personalisationProvider;

    private final RecipientsFinder recipientsFinder = new RecipientsFinder();

    private PreSubmitCallbackHandler<AsylumCase> emailHandler;
    private PreSubmitCallbackHandler<AsylumCase> smsHandler;

    @BeforeEach
    void setup() {
        when(callback.getEvent()).thenReturn(CMR_RE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(12345L);

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(EMAIL, String.class)).thenReturn(Optional.of(APPELLANT_EMAIL));
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of(APPELLANT_MOBILE));

        DynamicList inPersonChannel = new DynamicList(
            new Value("INTER", "In Person"),
            List.of(new Value("INTER", "In Person"), new Value("VID", "Video"), new Value("TEL", "Telephone"))
        );
        when(asylumCase.read(CMR_HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(inPersonChannel));
        when(asylumCase.read(CMR_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        when(personalisationProvider.getPersonalisation(callback)).thenReturn(Collections.emptyMap());
        when(hearingDetailsFinder.getCmrHearingCentreName(asylumCase)).thenReturn("Taylor House");

        AppellantCmrRelistingPersonalisationEmail appellantEmailPersonalisation = new AppellantCmrRelistingPersonalisationEmail(
            EMAIL_TEMPLATE_ID,
            LEGALLY_REPPED_EMAIL_TEMPLATE_ID,
            "http://somefrontendurl",
            personalisationProvider,
            customerServicesProvider,
            recipientsFinder,
            hearingDetailsFinder
        );
        initializePrefixes(appellantEmailPersonalisation);
        AppellantCmrRelistingPersonalisationSms appellantSmsPersonalisation = new AppellantCmrRelistingPersonalisationSms(
            SMS_TEMPLATE_ID,
            LEGALLY_REPPED_SMS_TEMPLATE_ID,
            "http://somefrontendurl",
            personalisationProvider,
            recipientsFinder,
            hearingDetailsFinder
        );

        CmrNotificationGeneratorConfiguration generatorConfiguration = new CmrNotificationGeneratorConfiguration();
        List<NotificationGenerator> emailGenerators = generatorConfiguration.nonDetainedCmrRelistingAppellantEmailNotificationGenerator(
            appellantEmailPersonalisation, notificationSender, notificationIdAppender);
        List<NotificationGenerator> smsGenerators = generatorConfiguration.nonDetainedCmrRelistingAppellantSmsNotificationGenerator(
            appellantSmsPersonalisation, notificationSender, notificationIdAppender);

        CmrNotificationHandlerConfiguration handlerConfiguration = new CmrNotificationHandlerConfiguration();
        emailHandler = handlerConfiguration.nonDetainedCmrRelistingAppellantEmailNotificationHandler(emailGenerators);
        smsHandler = handlerConfiguration.nonDetainedCmrRelistingAppellantSmsNotificationHandler(smsGenerators);
    }

    @Test
    void email_preferred_sends_email_and_does_not_send_sms() {
        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class))
            .thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));

        assertThat(emailHandler.canHandle(ABOUT_TO_SUBMIT, callback)).isTrue();
        assertThat(smsHandler.canHandle(ABOUT_TO_SUBMIT, callback)).isFalse();

        emailHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(notificationSender, times(1)).sendEmail(
            eq(LEGALLY_REPPED_EMAIL_TEMPLATE_ID), eq(APPELLANT_EMAIL), any(), anyString(), eq(callback));
        verify(notificationSender, never()).sendSms(anyString(), anyString(), any(), anyString(), eq(callback));
    }

    @Test
    void sms_preferred_sends_sms_and_does_not_send_email() {
        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class))
            .thenReturn(Optional.of(ContactPreference.WANTS_SMS));

        assertThat(smsHandler.canHandle(ABOUT_TO_SUBMIT, callback)).isTrue();
        assertThat(emailHandler.canHandle(ABOUT_TO_SUBMIT, callback)).isFalse();

        smsHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(notificationSender, times(1)).sendSms(
            eq(LEGALLY_REPPED_SMS_TEMPLATE_ID), eq(APPELLANT_MOBILE), any(), anyString(), eq(callback));
        verify(notificationSender, never()).sendEmail(anyString(), anyString(), any(), anyString(), eq(callback));
    }

    @Test
    void neither_preference_set_sends_nothing() {
        when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)).thenReturn(Optional.empty());

        assertThat(emailHandler.canHandle(ABOUT_TO_SUBMIT, callback)).isFalse();
        assertThat(smsHandler.canHandle(ABOUT_TO_SUBMIT, callback)).isFalse();
    }
}
