package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.adminofficer.email.AdminOfficerBailSummaryUploadedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms.ApplicantBailApplicationSubmittedPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms.ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.hearingcentre.email.HearingCentreSubmitApplicationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email.HomeOfficeBailApplicationSubmittedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email.HomeOfficeBailSignedDecisionNoticeUploadedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailApplicationSubmittedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailSummaryUploadedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailEmailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailNotificationIdAppender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailSmsNotificationGenerator;

@Configuration
public class BailNotificationGeneratorConfiguration {


    @Bean("submitApplicationNotificationGenerator")
    public List<BailNotificationGenerator> submitApplicationNotificationGenerator(
        HearingCentreSubmitApplicationPersonalisation hearingCentreSubmitApplicationPersonalisation,
        LegalRepresentativeBailApplicationSubmittedPersonalisation legalRepresentativeBailApplicationSubmittedPersonalisation,
        HomeOfficeBailApplicationSubmittedPersonalisation homeOfficeBailApplicationSubmittedPersonalisation,
        ApplicantBailApplicationSubmittedPersonalisationSms applicantBailApplicationSubmittedPersonalisationSms,
        NotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailEmailNotificationGenerator(
                newArrayList(hearingCentreSubmitApplicationPersonalisation,
                    legalRepresentativeBailApplicationSubmittedPersonalisation,
                    homeOfficeBailApplicationSubmittedPersonalisation),
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

    @Bean("submitApplicationWithoutLegalRepNotificationGenerator")
    public List<BailNotificationGenerator> submitApplicationWithoutLegalRepNotificationGenerator(
        HearingCentreSubmitApplicationPersonalisation hearingCentreSubmitApplicationPersonalisation,
        HomeOfficeBailApplicationSubmittedPersonalisation homeOfficeBailApplicationSubmittedPersonalisation,
        ApplicantBailApplicationSubmittedPersonalisationSms applicantBailApplicationSubmittedPersonalisationSms,
        NotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailEmailNotificationGenerator(
                newArrayList(hearingCentreSubmitApplicationPersonalisation,
                    homeOfficeBailApplicationSubmittedPersonalisation),
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
            AdminOfficerBailSummaryUploadedPersonalisation adminOfficerBailSummaryUploadedPersonalisation,
            LegalRepresentativeBailSummaryUploadedPersonalisation legalRepresentativeBailSummaryUploadedPersonalisation,
            NotificationSender notificationSender,
            BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new BailEmailNotificationGenerator(
                        newArrayList(adminOfficerBailSummaryUploadedPersonalisation,
                                legalRepresentativeBailSummaryUploadedPersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("uploadSummaryWithoutLrNotificationGenerator")
    public List<BailNotificationGenerator> uploadSignedDecisionNoticeNotificationGenerator(
            AdminOfficerBailSummaryUploadedPersonalisation adminOfficerBailSummaryUploadedPersonalisation,
            NotificationSender notificationSender,
            BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new BailEmailNotificationGenerator(
                        newArrayList(adminOfficerBailSummaryUploadedPersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("uploadSignedDecisionNoticeNotificationGenerator")
    public List<BailNotificationGenerator> uploadSignedDecisionNoticeNotificationGenerator(
        ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms applicantBailSignedDecisionNoticeUploadedPersonalisationSms,
        HomeOfficeBailSignedDecisionNoticeUploadedPersonalisation homeOfficeBailSignedDecisionNoticeUploadedPersonalisation,
        LegalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation legalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation,
        NotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailSmsNotificationGenerator(
                newArrayList(applicantBailSignedDecisionNoticeUploadedPersonalisationSms),
                notificationSender,
                notificationIdAppender
            ),
            new BailEmailNotificationGenerator(
                newArrayList(homeOfficeBailSignedDecisionNoticeUploadedPersonalisation,
                    legalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadSignedDecisionNoticeWithoutLrNotificationGenerator")
    public List<BailNotificationGenerator> uploadSignedDecisionNoticeWithoutLrNotificationGenerator(
        ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms applicantBailSignedDecisionNoticeUploadedPersonalisationSms,
        HomeOfficeBailSignedDecisionNoticeUploadedPersonalisation homeOfficeBailSignedDecisionNoticeUploadedPersonalisation,
        NotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailSmsNotificationGenerator(
                newArrayList(applicantBailSignedDecisionNoticeUploadedPersonalisationSms),
                notificationSender,
                notificationIdAppender
            ),
            new BailEmailNotificationGenerator(
                newArrayList(homeOfficeBailSignedDecisionNoticeUploadedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }
}
