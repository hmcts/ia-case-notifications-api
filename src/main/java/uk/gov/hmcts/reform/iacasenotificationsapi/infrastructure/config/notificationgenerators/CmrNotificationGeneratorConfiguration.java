package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.notificationgenerators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.EmailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;

@Slf4j
@Configuration
public class CmrNotificationGeneratorConfiguration {
    @Bean("detainedLegalRepInPersonCmrListingNotificationGenerator1")
    public List<NotificationGenerator> detainedLegalRepInPersonCmrListingNotificationGenerator(
        LegalRepresentativeInPersonCmrListCasePersonalisation legalRepresentativeInPersonCmrListCasePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return singletonList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeInPersonCmrListCasePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }
}
