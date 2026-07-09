package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.notificationgenerators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerCmrListingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam.DetentionEngagementTeamCmrListingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeInPersonCmrListingCasePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.EmailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.EmailWithLinkNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;
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
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return newArrayList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeInPersonCmrListingPersonalisation,
                    caseOfficerCmrListingPersonalisation,
                    homeOfficeInPersonCmrListingCasePersonalisation
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
}
