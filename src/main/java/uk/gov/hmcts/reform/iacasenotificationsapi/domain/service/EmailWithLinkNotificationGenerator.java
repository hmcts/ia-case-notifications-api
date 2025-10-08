package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.ApplicationContextProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

public class EmailWithLinkNotificationGenerator implements NotificationGenerator {

    protected final List<EmailWithLinkNotificationPersonalisation> personalisationList;
    protected final NotificationIdAppender notificationIdAppender;
    protected final GovNotifyNotificationSender notificationSender;

    public EmailWithLinkNotificationGenerator(
        List<EmailWithLinkNotificationPersonalisation> repPersonalisationList,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        this.personalisationList = repPersonalisationList;
        this.notificationSender = notificationSender;
        this.notificationIdAppender = notificationIdAppender;
    }

    @Override
    public void generate(Callback<AsylumCase> callback) {

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        System.out.println("Entering email generation");

        ApplicationContextProvider.getApplicationContext().getBean(CustomerServicesProvider.class)
            .setCorrectEmail(asylumCase);

        personalisationList.forEach(personalisation -> {
            String referenceId = personalisation.getReferenceId(callback.getCaseDetails().getId());
            List<String> notificationIds = createEmail(personalisation, asylumCase, referenceId, callback);
            notificationIdAppender.appendAll(asylumCase, referenceId, notificationIds);
        });
    }

    protected List<String> createEmail(
        final EmailWithLinkNotificationPersonalisation personalisation,
        final AsylumCase asylumCase,
        final String referenceId,
        final Callback<AsylumCase> callback) {

        EmailWithLinkNotificationPersonalisation emailNotificationPersonalisation = personalisation;
        Set<String> subscriberEmails = emailNotificationPersonalisation.getRecipientsList(asylumCase);

        System.out.println("event in create email: " + callback.getEvent());
        System.out.println("Subscriber emails " + subscriberEmails);

        return subscriberEmails.stream()
            .filter(StringUtils::isNotBlank)
            .map(email ->
                sendEmail(
                    email,
                    emailNotificationPersonalisation,
                    referenceId,
                    callback)).collect(Collectors.toList());
    }

    protected String sendEmail(
        final String email,
        final EmailWithLinkNotificationPersonalisation personalisation,
        final String referenceId,
        final Callback<AsylumCase> callback) {

        System.out.println((" reference id in send email: " + referenceId));
        System.out.println((" template id in send email: " + personalisation.getTemplateId(callback.getCaseDetails().getCaseData())));

        String emailTemplateId = personalisation.getTemplateId() == null
            ?
            personalisation.getTemplateId(callback.getCaseDetails().getCaseData()) : personalisation.getTemplateId();

        System.out.println((" email template id in send email: " + personalisation.getTemplateId(callback.getCaseDetails().getCaseData())));


        return notificationSender.sendEmailWithLink(
                        emailTemplateId,
                        email,
                        personalisation.getPersonalisationForLink(callback),
                        referenceId
                );
    }

}
