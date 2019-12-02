package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BaseNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;

public class SmsNotificationGenerator implements NotificationGenerator {

    private final List<SmsNotificationPersonalisation> personalisationList;
    private final NotificationIdAppender notificationIdAppender;
    private final NotificationSender notificationSender;

    public SmsNotificationGenerator(
        List<SmsNotificationPersonalisation> aipPersonalisationList,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        this.personalisationList = aipPersonalisationList;
        this.notificationSender = notificationSender;
        this.notificationIdAppender = notificationIdAppender;
    }

    @Override
    public void generate(Callback<AsylumCase> callback) {

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        personalisationList.forEach(personalisation -> {
            String referenceId = personalisation.getReferenceId(callback.getCaseDetails().getId());
            List<String> notificationIds = create(personalisation, asylumCase, referenceId, callback);
            appendToSentNotifications(asylumCase, referenceId, notificationIds);
        });
    }


    private void appendToSentNotifications(
        final AsylumCase asylumCase,
        final String referenceId,
        final List<String> notificationIds) {
        Optional<List<IdValue<String>>> maybeNotificationSent =
            asylumCase.read(NOTIFICATIONS_SENT);

        List<IdValue<String>> notificationsSent =
            maybeNotificationSent
                .orElseGet(ArrayList::new);

        notificationIds.forEach(notificationId ->
            asylumCase.write(NOTIFICATIONS_SENT,
                notificationIdAppender.append(
                    notificationsSent,
                    referenceId,
                    notificationId
                )
            )
        );
    }

    private List<String> create(
        final BaseNotificationPersonalisation personalisation,
        final AsylumCase asylumCase,
        final String referenceId,
        final Callback<AsylumCase> callback) {
        List<String> notificationIds = new ArrayList<>();

        SmsNotificationPersonalisation smsNotificationPersonalisation = (SmsNotificationPersonalisation) personalisation;

        Set<String> phoneNumbers = smsNotificationPersonalisation.getRecipientsList(asylumCase);

        notificationIds.addAll(
            phoneNumbers.stream()
                .map(phoneNumber ->
                    sendSms(
                        phoneNumber,
                        smsNotificationPersonalisation,
                        referenceId,
                        callback))
                .collect(Collectors.toList())
        );

        return notificationIds;
    }

    private String sendSms(
        final String mobileNumber,
        final SmsNotificationPersonalisation personalisation,
        final String referenceId,
        final Callback<AsylumCase> callback) {

        return notificationSender.sendSms(
            personalisation.getTemplateId(),
            mobileNumber,
            personalisation.getPersonalisation(callback),
            referenceId
        );
    }
}
