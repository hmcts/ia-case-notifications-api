package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms.ApplicantBailApplicationSubmittedPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.adminofficer.email.AdminOfficerBailSummaryUploadedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.hearingcentre.email.HearingCentreSubmitApplicationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailSummaryUploadedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailEmailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailNotificationIdAppender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.*;

@Configuration
public class BailNotificationGeneratorConfiguration {


    @Bean("submitApplicationNotificationGenerator")
    public List<BailNotificationGenerator> submitApplicationNotificationGenerator(
        HearingCentreSubmitApplicationPersonalisation hearingCentreSubmitApplicationPersonalisation,
        ApplicantBailApplicationSubmittedPersonalisationSms applicantBailApplicationSubmittedPersonalisationSms,
        NotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailEmailNotificationGenerator(
                newArrayList(hearingCentreSubmitApplicationPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new BailSmsNotificationGenerator(
                    newArrayList(applicantBailApplicationSubmittedPersonalisationSms),
                    notificationSender,
                    notificationIdAppender
            )
        );
    }

    @Bean("uploadSummaryNotificationGenerator")
    public List<BailNotificationGenerator> uploadSummaryNotificationGenerator(
            LegalRepresentativeBailSummaryUploadedPersonalisation legalRepresentativeBailSummaryUploadedPersonalisation,
            AdminOfficerBailSummaryUploadedPersonalisation adminOfficerBailSummaryUploadedPersonalisation,
            NotificationSender notificationSender,
            BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new BailEmailNotificationGenerator(
                        newArrayList(legalRepresentativeBailSummaryUploadedPersonalisation,
                                adminOfficerBailSummaryUploadedPersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }
}
