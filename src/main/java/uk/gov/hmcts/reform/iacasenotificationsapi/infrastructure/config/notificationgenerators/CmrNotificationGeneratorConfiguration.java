package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.notificationgenerators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email.AppellantCmrListingPersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms.AppellantCmrListingPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerCmrListingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam.DetentionEngagementTeamCmrListingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam.DetentionEngagementTeamCmrListingProductionPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeInPersonCmrListingCasePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.EmailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.EmailWithLinkNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.SmsNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Slf4j
@Configuration
public class CmrNotificationGeneratorConfiguration {
    @Bean("detainedInPrisonIrcLegalRepInPersonCmrListingNotificationGenerator")
    public List<NotificationGenerator> detainedLegalRepInPersonCmrListingNotificationGenerator(
        LegalRepresentativeInPersonCmrListingPersonalisation legalRepresentativeInPersonCmrListingPersonalisation,
        CaseOfficerCmrListingPersonalisation caseOfficerCmrListingPersonalisation,
        HomeOfficeInPersonCmrListingCasePersonalisation homeOfficeInPersonCmrListingCasePersonalisation,
        DetentionEngagementTeamCmrListingPersonalisation detentionEngagementTeamCmrListingPersonalisation,
        DetentionEngagementTeamCmrListingProductionPersonalisation detentionEngagementTeamCmrListingProductionPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return newArrayList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeInPersonCmrListingPersonalisation,
                    caseOfficerCmrListingPersonalisation,
                    homeOfficeInPersonCmrListingCasePersonalisation,
                    detentionEngagementTeamCmrListingProductionPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ),
            new EmailWithLinkNotificationGenerator(
                newArrayList(
                    detentionEngagementTeamCmrListingPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("nonDetainedCmrListingHoCoLrNotificationGenerator")
    public List<NotificationGenerator> nonDetainedCmrListingHoCoLrNotificationGenerator(
        LegalRepresentativeInPersonCmrListingPersonalisation legalRepresentativeInPersonCmrListingPersonalisation,
        CaseOfficerCmrListingPersonalisation caseOfficerCmrListingPersonalisation,
        HomeOfficeInPersonCmrListingCasePersonalisation homeOfficeInPersonCmrListingCasePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        List<EmailNotificationPersonalisation> emailPersonalisations = newArrayList(
            legalRepresentativeInPersonCmrListingPersonalisation,
            caseOfficerCmrListingPersonalisation,
            homeOfficeInPersonCmrListingCasePersonalisation
        );

        return newArrayList(
            new EmailNotificationGenerator(
                emailPersonalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("nonDetainedCmrListingAppellantEmailNotificationGenerator")
    public List<NotificationGenerator> nonDetainedCmrListingAppellantEmailNotificationGenerator(
        AppellantCmrListingPersonalisationEmail appellantCmrListingPersonalisationEmail,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return newArrayList(
            new EmailNotificationGenerator(
                newArrayList(appellantCmrListingPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("nonDetainedCmrListingAppellantSmsNotificationGenerator")
    public List<NotificationGenerator> nonDetainedCmrListingAppellantSmsNotificationGenerator(
        AppellantCmrListingPersonalisationSms appellantCmrListingPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return newArrayList(
            new SmsNotificationGenerator(
                newArrayList(appellantCmrListingPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }
}
