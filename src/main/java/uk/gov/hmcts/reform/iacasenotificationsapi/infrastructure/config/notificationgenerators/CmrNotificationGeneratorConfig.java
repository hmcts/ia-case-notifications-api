package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.notificationgenerators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email.AppellantCmrHearingCancelledPersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms.AppellantCmrHearingCancelledPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerAipCmrHearingCancelledPersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeAipCmrHearingCancelledPersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.EmailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.SmsNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;


@Slf4j
@Configuration
public class CmrNotificationGeneratorConfig {

    @Bean("cmrHearingCancelledNotificationGenerator")
    public List<NotificationGenerator> cmrHearingCancelledNotificationGenerator(
            AppellantCmrHearingCancelledPersonalisationEmail appellantCmrHearingCancelledPersonalisationEmail,
            AppellantCmrHearingCancelledPersonalisationSms appellantCmrHearingCancelledPersonalisationSms,
            CaseOfficerAipCmrHearingCancelledPersonalisationEmail caseOfficerAipCmrHearingCancelledPersonalisationEmail,
            HomeOfficeAipCmrHearingCancelledPersonalisationEmail homeOfficeAipCmrHearingCancelledPersonalisationEmail,
            GovNotifyNotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender
    ) {

        return newArrayList(
                new EmailNotificationGenerator(
                        newArrayList(
                                appellantCmrHearingCancelledPersonalisationEmail,
                                caseOfficerAipCmrHearingCancelledPersonalisationEmail,
                                homeOfficeAipCmrHearingCancelledPersonalisationEmail
                        ),
                        notificationSender,
                        notificationIdAppender
                ),
                new SmsNotificationGenerator(
                        newArrayList(
                                appellantCmrHearingCancelledPersonalisationSms
                        ),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }
}