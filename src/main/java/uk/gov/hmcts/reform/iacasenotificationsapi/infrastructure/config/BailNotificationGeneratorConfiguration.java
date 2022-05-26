package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.adminofficer.email.AdminOfficerBailSummaryUploadedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms.ApplicantBailApplicationEndedPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms.ApplicantBailApplicationSubmittedPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms.ApplicantBailSignedDecisionNoticeUploadedPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.hearingcentre.email.HearingCentreSubmitApplicationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email.HomeOfficeBailApplicationEndedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email.HomeOfficeBailApplicationSubmittedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email.HomeOfficeBailDocumentUploadedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email.HomeOfficeBailChangeDirectionDueDatePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email.HomeOfficeBailSignedDecisionNoticeUploadedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailEmailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailNotificationIdAppender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.BailSmsNotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.BailGovNotifyNotificationSender;

@Configuration
public class BailNotificationGeneratorConfiguration {


    @Bean("submitApplicationNotificationGenerator")
    public List<BailNotificationGenerator> submitApplicationNotificationGenerator(
        HearingCentreSubmitApplicationPersonalisation hearingCentreSubmitApplicationPersonalisation,
        LegalRepresentativeBailApplicationSubmittedPersonalisation legalRepresentativeBailApplicationSubmittedPersonalisation,
        HomeOfficeBailApplicationSubmittedPersonalisation homeOfficeBailApplicationSubmittedPersonalisation,
        ApplicantBailApplicationSubmittedPersonalisationSms applicantBailApplicationSubmittedPersonalisationSms,
        BailGovNotifyNotificationSender notificationSender,
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
        BailGovNotifyNotificationSender notificationSender,
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
            BailGovNotifyNotificationSender notificationSender,
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
    public List<BailNotificationGenerator> uploadSummaryWithoutLrNotificationGenerator(
            AdminOfficerBailSummaryUploadedPersonalisation adminOfficerBailSummaryUploadedPersonalisation,
            BailGovNotifyNotificationSender notificationSender,
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
        BailGovNotifyNotificationSender notificationSender,
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
        BailGovNotifyNotificationSender notificationSender,
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

    @Bean("endApplicationNotificationGenerator")
    public List<BailNotificationGenerator> endApplicationNotificationGenerator(
        ApplicantBailApplicationEndedPersonalisationSms applicantBailApplicationEndedPersonalisationSms,
        HomeOfficeBailApplicationEndedPersonalisation homeOfficeBailApplicationEndedPersonalisation,
        LegalRepresentativeBailApplicationEndedPersonalisation legalRepresentativeBailApplicationEndedPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailSmsNotificationGenerator(
                newArrayList(applicantBailApplicationEndedPersonalisationSms),
                notificationSender,
                notificationIdAppender
            ),
            new BailEmailNotificationGenerator(
                newArrayList(homeOfficeBailApplicationEndedPersonalisation,
                    legalRepresentativeBailApplicationEndedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("endApplicationWithoutLrNotificationGenerator")
    public List<BailNotificationGenerator> endApplicationWithoutLrNotificationGenerator(
        ApplicantBailApplicationEndedPersonalisationSms applicantBailApplicationEndedPersonalisationSms,
        HomeOfficeBailApplicationEndedPersonalisation homeOfficeBailApplicationEndedPersonalisation,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new BailSmsNotificationGenerator(
                newArrayList(applicantBailApplicationEndedPersonalisationSms),
                notificationSender,
                notificationIdAppender
            ),
            new BailEmailNotificationGenerator(
                newArrayList(homeOfficeBailApplicationEndedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadDocumentNotificationGenerator")
    public List<BailNotificationGenerator> uploadDocumentNotificationGenerator(
            HomeOfficeBailDocumentUploadedPersonalisation homeOfficeBailDocumentUploadedPersonalisation,
            LegalRepresentativeBailDocumentUploadedPersonalisation legalRepresentativeBailDocumentUploadedPersonalisation,
            BailGovNotifyNotificationSender notificationSender,
            BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new BailEmailNotificationGenerator(
                        newArrayList(homeOfficeBailDocumentUploadedPersonalisation,
                        legalRepresentativeBailDocumentUploadedPersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("uploadDocumentWithoutLrNotificationGenerator")
    public List<BailNotificationGenerator> uploadDocumentWithoutLrNotificationGenerator(
            HomeOfficeBailDocumentUploadedPersonalisation homeOfficeBailDocumentUploadedPersonalisation,
            BailGovNotifyNotificationSender notificationSender,
            BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new BailEmailNotificationGenerator(
                        newArrayList(homeOfficeBailDocumentUploadedPersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("changeDirectionDueDateNotificationGenerator")
    public List<BailNotificationGenerator> changeDirectionDueDateNotificationGenerator(
            HomeOfficeBailDocumentUploadedPersonalisation homeOfficeBailChangeDirectionDueDatePersonalisation,
            LegalRepresentativeBailDocumentUploadedPersonalisation legalRepresentativeChangeDirectionDueDatePersonalisation,
            BailGovNotifyNotificationSender notificationSender,
            BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new BailEmailNotificationGenerator(
                        newArrayList(homeOfficeBailChangeDirectionDueDatePersonalisation,
                                legalRepresentativeChangeDirectionDueDatePersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }

    @Bean("changeDirectionDueDateWithoutLrNotificationGenerator")
    public List<BailNotificationGenerator> changeDirectionDueDateWithoutLrNotificationGenerator(
            HomeOfficeBailDocumentUploadedPersonalisation homeOfficeBailChangeDirectionDueDatePersonalisation,
            BailGovNotifyNotificationSender notificationSender,
            BailNotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new BailEmailNotificationGenerator(
                        newArrayList(homeOfficeBailChangeDirectionDueDatePersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }
}
