package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.REP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BaseNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;

public class EmailNotificationGenerator implements NotificationGenerator {

    private final List<EmailNotificationPersonalisation> repPersonalisationList;
    private final List<EmailNotificationPersonalisation> aipPersonalisationList;
    private final NotificationIdAppender notificationIdAppender;
    private final NotificationSender notificationSender;

    public EmailNotificationGenerator(List<EmailNotificationPersonalisation> repPersonalisationList,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {
        this(
            repPersonalisationList,
            Collections.emptyList(),
            notificationSender,
            notificationIdAppender);
    }

    public EmailNotificationGenerator(
        List<EmailNotificationPersonalisation> repPersonalisationList,
        List<EmailNotificationPersonalisation> aipPersonalisationList,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        this.repPersonalisationList = repPersonalisationList;
        this.aipPersonalisationList = aipPersonalisationList;
        this.notificationSender = notificationSender;
        this.notificationIdAppender = notificationIdAppender;
    }

    @Override
    public void generate(Callback<AsylumCase> callback) {

        JourneyType journeyType =
            callback.getCaseDetails().getCaseData()
                .read(JOURNEY_TYPE, JourneyType.class)
                .orElse(REP);

        switch (journeyType) {
            case AIP:
                generateNotifications(aipPersonalisationList, callback);
                break;
            case REP:
            default:
                generateNotifications(repPersonalisationList, callback);
                break;
        }
    }

    private void generateNotifications(
        final List<EmailNotificationPersonalisation> personalisationList,
        final Callback<AsylumCase> callback) {

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

        EmailNotificationPersonalisation emailNotificationPersonalisation = (EmailNotificationPersonalisation) personalisation;
        Set<String> subscriberEmails = emailNotificationPersonalisation.getRecipientsList(asylumCase);

        notificationIds.addAll(
            subscriberEmails.stream()
                .map(email ->
                    sendEmail(
                        email,
                        emailNotificationPersonalisation,
                        referenceId,
                        callback))
                .collect(Collectors.toList())
        );

        return notificationIds;
    }

    private String sendEmail(
        final String email,
        final EmailNotificationPersonalisation personalisation,
        final String referenceId,
        final Callback<AsylumCase> callback) {

        String emailTemplateId = personalisation.getTemplateId() == null
            ?
            personalisation.getTemplateId(callback.getCaseDetails().getCaseData()) : personalisation.getTemplateId();

        return notificationSender.sendEmail(
            emailTemplateId,
            email,
            personalisation.getPersonalisation(callback),
            referenceId
        );
    }

}
