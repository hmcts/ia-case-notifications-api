package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.*;

@Configuration
public class NotificationGeneratorConfiguration {

    @Bean("reListCaseNotificationGenerator")
    public List<NotificationGenerator> reListCaseNotificationGenerator(
        AdminOfficerReListCasePersonalisation adminOfficerReListCasePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(adminOfficerReListCasePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestCaseEditNotificationGenerator")
    public List<NotificationGenerator> requestCaseEditNotificationGenerator(
        LegalRepresentativeRequestCaseEditPersonalisation legalRepresentativeRequestCaseEditPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                Collections.singletonList(legalRepresentativeRequestCaseEditPersonalisation),
                notificationSender,
                notificationIdAppender)
        );
    }

    @Bean("endAppealNotificationGenerator")
    public List<NotificationGenerator> endAppealNotificationGenerator(
        HomeOfficeEndAppealPersonalisation homeOfficeEndAppealPersonalisation,
        LegalRepresentativeEndAppealPersonalisation legalRepresentativeEndAppealPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeEndAppealPersonalisation, legalRepresentativeEndAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appealOutcomeNotificationGenerator")
    public List<NotificationGenerator> appealOutcomeNotificationGenerator(
        HomeOfficeAppealOutcomePersonalisation homeOfficeAppealOutcomePersonalisation,
        LegalRepresentativeAppealOutcomePersonalisation legalRepresentativeAppealOutcomePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeAppealOutcomePersonalisation, legalRepresentativeAppealOutcomePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentChangeDirectionDueDateNotificationGenerator")
    public List<NotificationGenerator> respondentChangeDirectionDueDateNotificationGenerator(
        RespondentChangeDirectionDueDatePersonalisation respondentChangeDirectionDueDatePersonalisation,
        LegalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentChangeDirectionDueDatePersonalisation, legalRepresentativeChangeDirectionDueDateOfHomeOfficePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("legalRepChangeDirectionDueDateNotificationGenerator")
    public List<NotificationGenerator> legalRepChangeDirectionDueDateNotificationGenerator(
        LegalRepresentativeChangeDirectionDueDatePersonalisation legalRepresentativeChangeDirectionDueDatePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeChangeDirectionDueDatePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("listCaseNotificationGenerator")
    public List<NotificationGenerator> listCaseNotificationGenerator(
        CaseOfficerListCasePersonalisation caseOfficerListCasePersonalisation,
        LegalRepresentativeListCasePersonalisation legalRepresentativeListCasePersonalisation,
        HomeOfficeListCasePersonalisation homeOfficeListCasePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerListCasePersonalisation, legalRepresentativeListCasePersonalisation, homeOfficeListCasePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealAipNotificationGenerator")
    public List<NotificationGenerator> submitAppealAipNotificationGenerator(
        CaseOfficerSubmitAppealPersonalisation caseOfficerSubmitAppealPersonalisation,
        AppellantSubmitAppealPersonalisationSms appellantSubmitAppealPersonalisationSms,
        AppellantSubmitAppealPersonalisationEmail appellantSubmitAppealPersonalisationEmail,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantSubmitAppealPersonalisationEmail, caseOfficerSubmitAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantSubmitAppealPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealRepNotificationGenerator")
    public List<NotificationGenerator> submitAppealRepNotificationGenerator(
        CaseOfficerSubmitAppealPersonalisation caseOfficerSubmitAppealPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerSubmitAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitAppealHoNotificationGenerator")
    public List<NotificationGenerator> submitAppealHoNotificationGenerator(
            HomeOfficeSubmitAppealPersonalisation homeOfficeSubmitAppealPersonalisation,
            NotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
                new EmailNotificationGenerator(
                        newArrayList(homeOfficeSubmitAppealPersonalisation),
                        notificationSender,
                        notificationIdAppender
                )
        );
    }


    @Bean("submitAppealOutOfTimeAipNotificationGenerator")
    public List<NotificationGenerator> submitAppealOutOfTimeAipNotificationGenerator(
        CaseOfficerSubmitAppealPersonalisation caseOfficerSubmitAppealPersonalisation,
        AppellantSubmitAppealOutOfTimePersonalisationSms appellantSubmitAppealOutOfTimePersonalisationSms,
        AppellantSubmitAppealOutOfTimePersonalisationEmail appellantSubmitAppealOutOfTimePersonalisationEmail,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantSubmitAppealOutOfTimePersonalisationEmail, caseOfficerSubmitAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantSubmitAppealOutOfTimePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitCaseNotificationGenerator")
    public List<NotificationGenerator> submitCaseNotificationGenerator(
        CaseOfficerSubmitCasePersonalisation caseOfficerSubmitCasePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerSubmitCasePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadRespondentNotificationGenerator")
    public List<NotificationGenerator> uploadRespondentNotificationGenerator(
        LegalRepresentativeUploadRespondentEvidencePersonalisation legalRepresentativeUploadRespondentEvidencePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeUploadRespondentEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestReasonsForAppealAipNotificationGenerator")
    public List<NotificationGenerator> requestReasonsForAppealAipNotificationGenerator(
        AppellantRequestReasonsForAppealPersonalisationEmail appellantRequestReasonsForAppealPersonalisationEmail,
        AppellantRequestReasonsForAppealPersonalisationSms appellantRequestReasonsForAppealPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantRequestReasonsForAppealPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantRequestReasonsForAppealPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitReasonsForAppealAipNotificationGenerator")
    public List<NotificationGenerator> submitReasonsForAppealAipNotificationGenerator(
        CaseOfficerReasonForAppealSubmittedPersonalisation caseOfficerReasonForAppealSubmittedPersonalisation,
        AppellantSubmitReasonsForAppealPersonalisationEmail appellantSubmitReasonsForAppealPersonalisationEmail,
        AppellantSubmitReasonsForAppealPersonalisationSms appellantSubmitReasonsForAppealPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerReasonForAppealSubmittedPersonalisation, appellantSubmitReasonsForAppealPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantSubmitReasonsForAppealPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("hearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> hearingRequirementsNotificationGenerator(
        LegalRepresentativeHearingRequirementsPersonalisation legalRepresentativeHearingRequirementsPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> requestHearingRequirementsNotificationGenerator(
        LegalRepresentativeRequestHearingRequirementsPersonalisation legalRepresentativeRequestHearingRequirementsPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeRequestHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("addAppealNotificationGenerator")
    public List<NotificationGenerator> addAppealNotificationGenerator(
        LegalRepresentativeAddAppealPersonalisation legalRepresentativeAddAppealPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeAddAppealPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentReviewNotificationGenerator")
    public List<NotificationGenerator> respondentReviewNotificationGenerator(
        RespondentDirectionPersonalisation respondentDirectionPersonalisation,
        LegalRepresentativeRespondentReviewPersonalisation legalRepresentativeRespondentReviewPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    respondentDirectionPersonalisation,
                    legalRepresentativeRespondentReviewPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }


    @Bean("respondentEvidenceAipNotificationGenerator")
    public List<NotificationGenerator> respondentEvidenceAipNotificationGenerator(
        RespondentEvidenceDirectionPersonalisation respondentEvidenceDirectionPersonalisation,
        AppellantRequestRespondentEvidencePersonalisationEmail appellantRequestRespondentEvidencePersonalisationEmail,
        AppellantRequestRespondentEvidencePersonalisationSms appellantRequestRespondentEvidencePersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentEvidenceDirectionPersonalisation, appellantRequestRespondentEvidencePersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantRequestRespondentEvidencePersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentEvidenceRepNotificationGenerator")
    public List<NotificationGenerator> respondentEvidenceRepNotificationGenerator(
        RespondentEvidenceDirectionPersonalisation respondentEvidenceDirectionPersonalisation,
        LegalRepresentativeRequestHomeOfficeBundlePersonalisation legalRepresentativeRequestHomeOfficeBundlePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    respondentEvidenceDirectionPersonalisation,
                    legalRepresentativeRequestHomeOfficeBundlePersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentDirectionNotificationGenerator")
    public List<NotificationGenerator> respondentDirectionNotificationGenerator(
        RespondentNonStandardDirectionPersonalisation respondentNonStandardDirectionPersonalisation,
        LegalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(respondentNonStandardDirectionPersonalisation, legalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("legalRepDirectionNotificationGenerator")
    public List<NotificationGenerator> legalRepDirectionNotificationGenerator(
        LegalRepresentativeNonStandardDirectionPersonalisation legalRepresentativeNonStandardDirectionPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeNonStandardDirectionPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("recordApplicationNotificationGenerator")
    public List<NotificationGenerator> recordApplicationNotificationGenerator(
        HomeOfficeRecordApplicationPersonalisation homeOfficeRecordApplicationPersonalisation,
        LegalRepresentativeRecordApplicationPersonalisation legalRepresentativeRecordApplicationPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeRecordApplicationPersonalisation, legalRepresentativeRecordApplicationPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("editCaseListingNotificationGenerator")
    public List<NotificationGenerator> editCaseListingNotificationGenerator(
        CaseOfficerEditListingPersonalisation caseOfficerEditListingPersonalisation,
        HomeOfficeEditListingPersonalisation homeOfficeEditListingPersonalisation,
        LegalRepresentativeEditListingPersonalisation legalRepresentativeEditListingPersonalisation,
        LegalRepresentativeEditListingNoChangePersonalisation legalRepresentativeEditListingNoChangePersonalisation,
        HomeOfficeEditListingNoChangePersonalisation homeOfficeEditListingNoChangePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EditListingEmailNotificationGenerator(
                newArrayList(caseOfficerEditListingPersonalisation, homeOfficeEditListingPersonalisation, legalRepresentativeEditListingPersonalisation, legalRepresentativeEditListingNoChangePersonalisation, homeOfficeEditListingNoChangePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadHomeOfficeAppealResponseNotificationGenerator")
    public List<NotificationGenerator> uploadHomeOfficeAppealResponseNotificationGenerator(
        CaseOfficerHomeOfficeResponseUploadedPersonalisation caseOfficerHomeOfficeResponseUploadedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerHomeOfficeResponseUploadedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestCaseBuildingNotificationGenerator")
    public List<NotificationGenerator> requestCaseBuildingNotificationGenerator(
        LegalRepresentativeRequestCaseBuildingPersonalisation legalRepresentativeRequestCaseBuildingPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeRequestCaseBuildingPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestResponseReviewNotificationGenerator")
    public List<NotificationGenerator> requestResponseReviewNotificationGenerator(
        LegalRepresentativeRequestResponseReviewPersonalisation legalRepresentativeRequestResponseReviewPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeRequestResponseReviewPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("respondentEvidenceSubmitted")
    public List<NotificationGenerator> respondentEvidenceSubmitted(
        CaseOfficerRespondentEvidenceSubmittedPersonalisation caseOfficerRespondentEvidenceSubmittedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerRespondentEvidenceSubmittedPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("hearingBundleReadyNotificationGenerator")
    public List<NotificationGenerator> hearingBundleReadyNotificationGenerator(
        HomeOfficeHearingBundleReadyPersonalisation homeOfficeHearingBundleReadyPersonalisation,
        LegalRepresentativeHearingBundleReadyPersonalisation legalRepresentativeHearingBundleReadyPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeHearingBundleReadyPersonalisation, legalRepresentativeHearingBundleReadyPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submittedHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> submittedHearingRequirementsNotificationGenerator(
        CaseOfficerSubmittedHearingRequirementsPersonalisation caseOfficerSubmittedHearingRequirementsPersonalisation,
        LegalRepresentativeSubmittedHearingRequirementsPersonalisation legalRepresentativeSubmittedHearingRequirementsPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerSubmittedHearingRequirementsPersonalisation, legalRepresentativeSubmittedHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("adjustedHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> adjustedHearingRequirementsNotificationGenerator(
        AdminOfficerReviewHearingRequirementsPersonalisation adminOfficerReviewHearingRequirementsPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(adminOfficerReviewHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("withoutHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> withoutHearingRequirementsNotificationGenerator(
        AdminOfficerWithoutHearingRequirementsPersonalisation adminOfficerWithoutHearingRequirementsPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(adminOfficerWithoutHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAdditionalEvidence")
    public List<NotificationGenerator> uploadAdditionalEvidence(
        CaseOfficerUploadAdditionalEvidencePersonalisation caseOfficerUploadAdditionalEvidencePersonalisation,
        HomeOfficeUploadAdditionalEvidencePersonalisation homeOfficeUploadAdditionalEvidencePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerUploadAdditionalEvidencePersonalisation, homeOfficeUploadAdditionalEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAdditionalEvidenceHomeOffice")
    public List<NotificationGenerator> uploadAdditionalEvidenceHomeOffice(
        CaseOfficerUploadAdditionalEvidencePersonalisation caseOfficerUploadAdditionalEvidencePersonalisation,
        LegalRepresentativeUploadAdditionalEvidencePersonalisation legalRepresentativeUploadAdditionalEvidencePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerUploadAdditionalEvidencePersonalisation, legalRepresentativeUploadAdditionalEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAddendumEvidenceCaseOfficer")
    public List<NotificationGenerator> uploadAddendumEvidenceCaseOfficer(
        HomeOfficeUploadAddendumEvidencePersonalisation homeOfficeUploadAddendumEvidencePersonalisation,
        LegalRepresentativeUploadAddendumEvidencePersonalisation legalRepresentativeUploadAddendumEvidencePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender

    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeUploadAddendumEvidencePersonalisation, legalRepresentativeUploadAddendumEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAddendumEvidenceHomeOffice")
    public List<NotificationGenerator> uploadAddendumEvidenceHomeOffice(
        CaseOfficerUploadAddendumEvidencePersonalisation caseOfficerUploadAddendumEvidencePersonalisation,
        LegalRepresentativeUploadAddendumEvidencePersonalisation legalRepresentativeUploadAddendumEvidencePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender

    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerUploadAddendumEvidencePersonalisation, legalRepresentativeUploadAddendumEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("uploadAddendumEvidenceLegalRep")
    public List<NotificationGenerator> uploadAddendumEvidenceLegalRep(
        CaseOfficerUploadAddendumEvidencePersonalisation caseOfficerUploadAddendumEvidencePersonalisation,
        HomeOfficeUploadAddendumEvidencePersonalisation homeOfficeUploadAddendumEvidencePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerUploadAddendumEvidencePersonalisation, homeOfficeUploadAddendumEvidencePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("changeToHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> changeToHearingRequirementsNotificationGenerator(
        AdminOfficerChangeToHearingRequirementsPersonalisation adminOfficerChangeToHearingRequirementsPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(adminOfficerChangeToHearingRequirementsPersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("appealExitedOnlineNotificationGenerator")
    public List<NotificationGenerator> appealExitedOnlineNotificationGenerator(
        HomeOfficeAppealExitedOnlinePersonalisation homeOfficeAppealExitedOnlinePersonalisation,
        LegalRepresentativeAppealExitedOnlinePersonalisation legalRepresentativeAppealExitedOnlinePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(homeOfficeAppealExitedOnlinePersonalisation, legalRepresentativeAppealExitedOnlinePersonalisation),
                notificationSender,
                notificationIdAppender)
        );
    }

    @Bean("changeHearingCentreNotificationGenerator")
    public List<NotificationGenerator> changeHearingCentreNotificationGenerator(
        LegalRepresentativeChangeHearingCentrePersonalisation legalRepresentativeChangeHearingCentrePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(legalRepresentativeChangeHearingCentrePersonalisation),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("changeHearingCentreNotificationGenerator")
    public List<NotificationGenerator> changeHearingCentreNotificationGenerator(
        LegalRepresentativeChangeHearingCentrePersonalisation legalRepresentativeChangeHearingCentrePersonalisation,
        CaseOfficerChangeHearingCentrePersonalisation caseOfficerChangeHearingCentrePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeChangeHearingCentrePersonalisation,
                    caseOfficerChangeHearingCentrePersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaSubmittedLegalRepNotificationGenerator")
    public List<NotificationGenerator> ftpaSubmittedLegalRep(
        LegalRepresentativeFtpaSubmittedPersonalisation legalRepresentativeFtpaSubmittedPersonalisation,
        AdminOfficerFtpaSubmittedPersonalisation adminOfficerFtpaSubmittedPersonalisation,
        RespondentAppellantFtpaSubmittedPersonalisation respondentAppellantFtpaSubmittedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeFtpaSubmittedPersonalisation,
                    adminOfficerFtpaSubmittedPersonalisation,
                    respondentAppellantFtpaSubmittedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("ftpaSubmittedRespondentNotificationGenerator")
    public List<NotificationGenerator> ftpaSubmittedRespondent(
        RespondentFtpaSubmittedPersonalisation respondentFtpaSubmittedPersonalisation,
        AdminOfficerFtpaSubmittedPersonalisation adminOfficerFtpaSubmittedPersonalisation,
        LegalRepresentativeRespondentFtpaSubmittedPersonalisation legalRepresentativeRespondentFtpaSubmittedPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(
                    respondentFtpaSubmittedPersonalisation,
                    adminOfficerFtpaSubmittedPersonalisation,
                    legalRepresentativeRespondentFtpaSubmittedPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitTimeExtensionAipNotificationGenerator")
    public List<NotificationGenerator> submitTimeExtensionAipNotificationGenerator(
        CaseOfficerSubmitTimeExtensionPersonalisation caseOfficerSubmitTimeExtensionPersonalisation,
        AppellantSubmitTimeExtensionPersonalisationEmail appellantSubmitTimeExtensionPersonalisationEmail,
        AppellantSubmitTimeExtensionPersonalisationSms appellantSubmitTimeExtensionPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(caseOfficerSubmitTimeExtensionPersonalisation, appellantSubmitTimeExtensionPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantSubmitTimeExtensionPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("reviewTimeExtensionGrantedGenerator")
    public List<NotificationGenerator> reviewTimeExtensionGrantedGenerator(
        AppellantReviewTimeExtensionGrantedPersonalisationEmail appellantReviewTimeExtensionGrantedPersonalisationEmail,
        AppellantReviewTimeExtensionGrantedPersonalisationSms appellantReviewTimeExtensionGrantedPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
              newArrayList(appellantReviewTimeExtensionGrantedPersonalisationEmail),
              notificationSender,
              notificationIdAppender
          ),
            new SmsNotificationGenerator(
                newArrayList(appellantReviewTimeExtensionGrantedPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("reviewTimeExtensionRefusedGenerator")
    public List<NotificationGenerator> reviewTimeExtensionRefusedGenerator(
        AppellantReviewTimeExtensionRefusedPersonalisationEmail appellantReviewTimeExtensionRefusedPersonalisationEmail,
        AppellantReviewTimeExtensionRefusedPersonalisationSms appellantReviewTimeExtensionRefusedPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
              newArrayList(appellantReviewTimeExtensionRefusedPersonalisationEmail),
              notificationSender,
              notificationIdAppender
          ),
            new SmsNotificationGenerator(
                newArrayList(appellantReviewTimeExtensionRefusedPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestClarifyingQuestionsAipNotificationGenerator")
    public List<NotificationGenerator> requestClarifyingQuestionsAipNotificationGenerator(
        AppellantRequestClarifyingQuestionsPersonalisationEmail appellantRequestClarifyingQuestionsPersonalisationEmail,
        AppellantRequestClarifyingQuestionsPersonalisationSms appellantRequestClarifyingQuestionsPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantRequestClarifyingQuestionsPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantRequestClarifyingQuestionsPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("submitClarifyingQuestionAnswersNotificationGenerator")
    public List<NotificationGenerator> submitClarifyingQuestionAnswersNotificationGenerator(
        CaseOfficerClarifyingQuestionsAnswersSubmittedPersonalisation caseOfficerClarifyingQuestionsAnswersSubmittedPersonalisation,
        AppellantSubmitClarifyingQuestionAnswersPersonalisationSms appellantSubmitClarifyingQuestionAnswersPersonalisationSms,
        AppellantSubmitClarifyingQuestionAnswersPersonalisationEmail appellantSubmitClarifyingQuestionAnswersPersonalisationEmail,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantSubmitClarifyingQuestionAnswersPersonalisationEmail, caseOfficerClarifyingQuestionsAnswersSubmittedPersonalisation),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantSubmitClarifyingQuestionAnswersPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("forceCaseProgressionToCaseUnderReviewNotificationGenerator")
    public List<NotificationGenerator> forceCaseProgressionToCaseUnderReviewNotificationGenerator(
        LegalRepresentativeForceCaseProgressionToCaseUnderReviewPersonalisation forceCaseProgressionToCaseUnderReviewPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    forceCaseProgressionToCaseUnderReviewPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("forceCaseToSubmitHearingRequirementsNotificationGenerator")
    public List<NotificationGenerator> forceCaseToSubmitHearingRequirementsNotificationGenerator(
        RespondentForceCaseToSubmitHearingRequirementsPersonalisation respondentForceCaseToSubmitHearingRequirementsPersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    respondentForceCaseToSubmitHearingRequirementsPersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("adjournHearingWithoutDateNotificationGenerator")
    public List<NotificationGenerator> adjournHearingWithoutDateNotificationGenerator(
        LegalRepresentativeAdjournHearingWithoutDatePersonalisation legalRepresentativeAdjournHearingWithoutDatePersonalisation,
        RespondentAdjournHearingWithoutDatePersonalisation respondentAdjournHearingWithoutDatePersonalisation,
        CaseOfficerAdjournHearingWithoutDatePersonalisation caseOfficerAdjournHearingWithoutDatePersonalisation,
        AdminOfficerAdjournHearingWithoutDatePersonalisation adminOfficerAdjournHearingWithoutDatePersonalisation,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return Collections.singletonList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeAdjournHearingWithoutDatePersonalisation,
                    respondentAdjournHearingWithoutDatePersonalisation,
                    caseOfficerAdjournHearingWithoutDatePersonalisation,
                    adminOfficerAdjournHearingWithoutDatePersonalisation
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("requestCmaRequirementsAipNotificationGenerator")
    public List<NotificationGenerator> requestCmaRequirementsAipNotificationGenerator(
        AppellantRequestCmaRequirementsPersonalisationEmail appellantRequestCmaRequirementsPersonalisationEmail,
        AppellantRequestCmaRequirementsPersonalisationSms appellantRequestCmaRequirementsPersonalisationSms,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        return Arrays.asList(
            new EmailNotificationGenerator(
                newArrayList(appellantRequestCmaRequirementsPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(appellantRequestCmaRequirementsPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }
}
