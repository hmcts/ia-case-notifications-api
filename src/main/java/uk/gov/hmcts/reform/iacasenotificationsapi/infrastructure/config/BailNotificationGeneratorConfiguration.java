package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.hearingcentre.email.HearingCentreSubmitApplicationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email.HomeOfficeBailApplicationSubmittedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailApplicationSubmittedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailEmailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailNotificationIdAppender;

@Configuration
public class BailNotificationGeneratorConfiguration {


    @Bean("submitApplicationHearingCentreNotificationGenerator")
    public List<BailNotificationGenerator> submitApplicationHearingCentreNotificationGenerator(
        HearingCentreSubmitApplicationPersonalisation hearingCentreSubmitApplicationPersonalisation,
        LegalRepresentativeBailApplicationSubmittedPersonalisation legalRepresentativeBailApplicationSubmittedPersonalisation,
        HomeOfficeBailApplicationSubmittedPersonalisation homeOfficeBailApplicationSubmittedPersonalisation,
        NotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailEmailNotificationGenerator(
                newArrayList(hearingCentreSubmitApplicationPersonalisation,
                    legalRepresentativeBailApplicationSubmittedPersonalisation,
                    homeOfficeBailApplicationSubmittedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }
}
