package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep.JoinAppealConfirmationAppellantPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep.JoinAppealConfirmationAppellantPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep.JoinAppealConfirmationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep.NlrDetailsUpdatedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep.RemoveNonLegalRepConfirmationAppellantPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep.RemoveNonLegalRepConfirmationAppellantPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep.RemoveNonLegalRepConfirmationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep.SendInviteToNonLegalRepPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep.SendPipToNonLegalRepPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.EmailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.SmsNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

@Configuration
public class NlrNotificationGeneratorConfiguration {

    @Bean("generateSendInviteToNonLegalRepNotificationGenerator")
    public List<NotificationGenerator> generateSendInviteToNonLegalRepNotificationGenerator(
        SendInviteToNonLegalRepPersonalisation sendInviteToNonLegalRepPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return List.of(
            new EmailNotificationGenerator(
                newArrayList(sendInviteToNonLegalRepPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("generateSendPipToNonLegalRepNotificationGenerator")
    public List<NotificationGenerator> generateSendPipToNonLegalRepNotificationGenerator(
        SendPipToNonLegalRepPersonalisation sendPipToNonLegalRepPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return List.of(
            new EmailNotificationGenerator(
                newArrayList(sendPipToNonLegalRepPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("generateJoinAppealConfirmationNotificationGenerator")
    public List<NotificationGenerator> generateJoinAppealConfirmationNotificationGenerator(
        JoinAppealConfirmationPersonalisation joinAppealConfirmationPersonalisation,
        JoinAppealConfirmationAppellantPersonalisation joinAppealConfirmationAppellantPersonalisation,
        JoinAppealConfirmationAppellantPersonalisationSms joinAppealConfirmationAppellantPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(joinAppealConfirmationPersonalisation, joinAppealConfirmationAppellantPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(joinAppealConfirmationAppellantPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("generateNonLegalRepRemovedNotificationGenerator")
    public List<NotificationGenerator> generateNonLegalRepRemovedNotificationGenerator(
        RemoveNonLegalRepConfirmationPersonalisation removeNonLegalRepConfirmationPersonalisation,
        RemoveNonLegalRepConfirmationAppellantPersonalisation removeNonLegalRepConfirmationAppellantPersonalisation,
        RemoveNonLegalRepConfirmationAppellantPersonalisationSms removeNonLegalRepConfirmationAppellantPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(removeNonLegalRepConfirmationPersonalisation, removeNonLegalRepConfirmationAppellantPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(removeNonLegalRepConfirmationAppellantPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("generateNlrPhoneNumberSubmittedNotificationGenerator")
    public List<NotificationGenerator> generateNlrPhoneNumberSubmittedNotificationGenerator(
        NlrDetailsUpdatedPersonalisation nlrDetailsUpdatedPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return List.of(
            new EmailNotificationGenerator(
                newArrayList(nlrDetailsUpdatedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }
}
