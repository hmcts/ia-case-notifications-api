package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder.NO_EMAIL_ADDRESS_DECISION_WITHOUT_HEARING;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.ApplicationContextProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.LegalRepresentativeEditListingNoChangePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.LegalRepresentativeEditListingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EditListingEmailNotificationGeneratorTest {

    @Mock
    static ApplicationContext applicationContext;
    @Mock
    static CustomerServicesProvider customerServicesProvider;
    private final String templateId1 = "templateId1";
    private final String templateId2 = "templateId1";
    private final String refId1 = "refId1";
    private final String refId2 = "refId2";
    private final String emailAddress1 = "email1@example.com";
    private final String emailAddress2 = "email2@example.com";
    private final Map<String, String> personalizationMap1 = emptyMap();
    private final Map<String, String> personalizationMap2 = emptyMap();
    private final List<IdValue<String>> notificationsSent = newArrayList();
    private final String notificationId1 = "notificationId1";
    private final String notificationId2 = "notificationId2";
    @Mock
    LegalRepresentativeEditListingNoChangePersonalisation editListingNoChangeEmailNotificationPersonalisation;
    @Mock
    LegalRepresentativeEditListingPersonalisation editListingChangeEmailNotificationPersonalisation1;
    @Mock
    GovNotifyNotificationSender notificationSender;
    @Spy
    NotificationIdAppender notificationIdAppender;
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    AsylumCase asylumCase;
    @Mock
    AsylumCase asylumCaseBefore;
    MockedStatic<ApplicationContextProvider> mocked;
    private List<EmailNotificationPersonalisation> repEmailNotificationPersonalisationList;
    private NotificationGenerator notificationGenerator;

    @BeforeEach
    public void setup() {
        mocked = mockStatic(ApplicationContextProvider.class);
        mocked.when(ApplicationContextProvider::getApplicationContext).thenReturn(applicationContext);
        when(applicationContext.getBean(CustomerServicesProvider.class)).thenReturn(customerServicesProvider);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);
        Long caseId = 12345L;
        when(caseDetails.getId()).thenReturn(caseId);

        when(asylumCase.read(AsylumCaseDefinition.NOTIFICATIONS_SENT)).thenReturn(Optional.of(notificationsSent));

        when(editListingNoChangeEmailNotificationPersonalisation.getReferenceId(caseId)).thenReturn(refId1);
        when(editListingChangeEmailNotificationPersonalisation1.getReferenceId(caseId)).thenReturn(refId2);

        when(editListingNoChangeEmailNotificationPersonalisation.getTemplateId()).thenReturn(templateId1);
        when(editListingChangeEmailNotificationPersonalisation1.getTemplateId()).thenReturn(templateId2);

        when(editListingNoChangeEmailNotificationPersonalisation.getPersonalisation(callback))
            .thenReturn(personalizationMap1);
        when(editListingChangeEmailNotificationPersonalisation1.getPersonalisation(callback))
            .thenReturn(personalizationMap2);

        when(notificationSender.sendEmail(templateId1, emailAddress1, personalizationMap1, refId1, callback))
            .thenReturn(notificationId1);
        when(notificationSender.sendEmail(templateId2, emailAddress2, personalizationMap2, refId2, callback))
            .thenReturn(notificationId2);

        when(notificationIdAppender.append(notificationsSent, refId1, notificationId1)).thenReturn(notificationsSent);
        when(notificationIdAppender.append(notificationsSent, refId2, notificationId2)).thenReturn(notificationsSent);

        repEmailNotificationPersonalisationList = newArrayList(editListingNoChangeEmailNotificationPersonalisation,
            editListingChangeEmailNotificationPersonalisation1);

    }

    @AfterEach
    public void cleanup() {
        mocked.close();
    }

    @Test
    public void should_send_no_change_notification_when_edit_listing_is_unchanged() {
        notificationGenerator =
            new EditListingEmailNotificationGenerator(repEmailNotificationPersonalisationList, notificationSender,
                notificationIdAppender);

        when(editListingNoChangeEmailNotificationPersonalisation.getRecipientsList(asylumCase))
            .thenReturn(singleton(emailAddress1));

        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        final String listingDateTime = "2020-02-06T13:51:29.369";
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of(listingDateTime));
        when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of(listingDateTime));

        notificationGenerator.generate(callback);

        verify(notificationSender).sendEmail(templateId1, emailAddress1, personalizationMap1, refId1, callback);
        verify(notificationSender, never()).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2, callback);

        verify(notificationIdAppender).appendAllAsylum(asylumCase, refId1, Collections.singletonList(notificationId1));
        verify(notificationIdAppender).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender, never())
            .appendAllAsylum(asylumCase, refId2, Collections.singletonList(notificationId2));
        verify(notificationIdAppender, never()).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(1)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_send_change_notification_when_edit_listing_hearing_centre_is_changed() {
        notificationGenerator =
            new EditListingEmailNotificationGenerator(repEmailNotificationPersonalisationList, notificationSender,
                notificationIdAppender);

        when(editListingChangeEmailNotificationPersonalisation1.getRecipientsList(asylumCase))
            .thenReturn(singleton(emailAddress2));

        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));

        final String listingDateTime = "2020-02-06T13:51:29.369";
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of(listingDateTime));
        when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of(listingDateTime));

        notificationGenerator.generate(callback);

        verify(notificationSender, never()).sendEmail(templateId1, emailAddress1, personalizationMap1, refId1, callback);
        verify(notificationSender).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2, callback);

        verify(notificationIdAppender, never())
            .appendAllAsylum(asylumCase, refId1, Collections.singletonList(notificationId1));
        verify(notificationIdAppender, never()).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAllAsylum(asylumCase, refId2, Collections.singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(1)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_send_change_notification_when_edit_listing_hearing_date_is_changed() {
        notificationGenerator =
            new EditListingEmailNotificationGenerator(repEmailNotificationPersonalisationList, notificationSender,
                notificationIdAppender);

        when(editListingChangeEmailNotificationPersonalisation1.getRecipientsList(asylumCase))
            .thenReturn(singleton(emailAddress2));

        final String listingDateTime = "2020-02-06T13:51:29.369";
        final String listingDateTimeBefore = "2020-02-04T13:51:29.369";
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of(listingDateTime));
        when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of(listingDateTimeBefore));

        notificationGenerator.generate(callback);

        verify(notificationSender, never()).sendEmail(templateId1, emailAddress1, personalizationMap1, refId1, callback);
        verify(notificationSender).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2, callback);

        verify(notificationIdAppender, never())
            .appendAllAsylum(asylumCase, refId1, Collections.singletonList(notificationId1));
        verify(notificationIdAppender, never()).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAllAsylum(asylumCase, refId2, Collections.singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(1)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_send_change_notification_when_edit_listing_hearing_date_time_is_changed() {
        notificationGenerator =
            new EditListingEmailNotificationGenerator(repEmailNotificationPersonalisationList, notificationSender,
                notificationIdAppender);

        when(editListingChangeEmailNotificationPersonalisation1.getRecipientsList(asylumCase))
            .thenReturn(singleton(emailAddress2));

        final String listingDateTime = "2020-02-06T13:51:29.111";
        final String listingDateTimeBefore = "2020-02-06T13:51:39.999";
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of(listingDateTime));
        when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of(listingDateTimeBefore));

        notificationGenerator.generate(callback);

        verify(notificationSender, never()).sendEmail(templateId1, emailAddress1, personalizationMap1, refId1, callback);
        verify(notificationSender).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2, callback);

        verify(notificationIdAppender, never())
            .appendAllAsylum(asylumCase, refId1, Collections.singletonList(notificationId1));
        verify(notificationIdAppender, never()).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAllAsylum(asylumCase, refId2, Collections.singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(1)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_not_send_notification_when_email_personalisation_list_empty() {
        notificationGenerator = new EmailNotificationGenerator(emptyList(), notificationSender, notificationIdAppender);
        notificationGenerator.generate(callback);

        verifyNoInteractions(notificationSender);
        verifyNoInteractions(notificationIdAppender);

        verify(asylumCase, times(0)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_not_send_notification_when_invalid_email_address() {
        notificationGenerator =
            new EditListingEmailNotificationGenerator(repEmailNotificationPersonalisationList, notificationSender,
                notificationIdAppender);

        when(editListingNoChangeEmailNotificationPersonalisation.getRecipientsList(asylumCase))
            .thenReturn(singleton(NO_EMAIL_ADDRESS_DECISION_WITHOUT_HEARING));

        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        final String listingDateTime = "2020-02-06T13:51:29.369";
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of(listingDateTime));
        when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of(listingDateTime));

        notificationGenerator.generate(callback);

        verifyNoInteractions(notificationSender);
        verify(notificationIdAppender).appendAllAsylum(asylumCase, refId1, emptyList());
        verifyNoMoreInteractions(notificationIdAppender);

        verify(asylumCase, times(1)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_send_change_notification_when_is_remote_hearing_is_changed() {
        notificationGenerator =
            new EditListingEmailNotificationGenerator(repEmailNotificationPersonalisationList, notificationSender,
                notificationIdAppender);

        when(editListingChangeEmailNotificationPersonalisation1.getRecipientsList(asylumCase))
            .thenReturn(singleton(emailAddress2));

        when(asylumCase.read(AsylumCaseDefinition.IS_REMOTE_HEARING, YesOrNo.class))
            .thenReturn(Optional.of(YES));
        when(asylumCaseBefore.read(AsylumCaseDefinition.IS_REMOTE_HEARING, YesOrNo.class))
            .thenReturn(Optional.of(NO));

        notificationGenerator.generate(callback);

        verify(notificationSender, never()).sendEmail(templateId1, emailAddress1, personalizationMap1, refId1, callback);
        verify(notificationSender).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2, callback);

        verify(notificationIdAppender, never())
            .appendAllAsylum(asylumCase, refId1, Collections.singletonList(notificationId1));
        verify(notificationIdAppender, never()).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAllAsylum(asylumCase, refId2, Collections.singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(1)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }
}
