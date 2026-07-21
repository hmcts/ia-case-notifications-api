package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.notificationgenerators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Message;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email.AipCmrRelistedAppellantEmailPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email.AppellantCmrRelistingPersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms.AipCmrRelistedAppellantSmsPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms.AppellantCmrRelistingPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerCmrListingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerCmrRelistingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam.DetentionEngagementTeamCmrListingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam.DetentionEngagementTeamCmrListingProductionPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeCmrRelistingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeInPersonCmrListingCasePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
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

    @Bean("aipManualCmrListingNotificationGenerator")
    public List<NotificationGenerator> aipManualCmrListingNotificationGenerator(
        CaseOfficerCmrListingPersonalisation caseOfficerCmrListingPersonalisation,
        HomeOfficeInPersonCmrListingCasePersonalisation homeOfficeInPersonCmrListingCasePersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender,
        DocumentDownloadClient documentDownloadClient
    ) {
        DocumentTag documentTag = DocumentTag.INTERNAL_CMR_LISTING_LETTER_BUNDLE;

        return newArrayList(
            new EmailNotificationGenerator(
                newArrayList(
                    caseOfficerCmrListingPersonalisation,
                    homeOfficeInPersonCmrListingCasePersonalisation
                ),
                notificationSender,
                notificationIdAppender
            ),
            new PrecompiledLetterNotificationGenerator(
                newArrayList(
                    documentTag
                ),
                notificationSender,
                notificationIdAppender,
                documentDownloadClient
            ) {
                    @Override
                public Message getSuccessMessage() {
                        return new Message("success","body");
                }
            }
        );
    }
  
    @Bean("aipManualCmrRelistingAppellantPostalNotificationGenerator")
    public List<NotificationGenerator> aipManualCmrRelistingAppellantPostalNotificationGenerator(
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender,
        DocumentDownloadClient documentDownloadClient
    ) {
        DocumentTag documentTag = DocumentTag.INTERNAL_CMR_LISTING_LETTER_BUNDLE;

        return newArrayList(
            new PrecompiledLetterNotificationGenerator(
                newArrayList(
                    documentTag
                ),
                notificationSender,
                notificationIdAppender,
                documentDownloadClient
            ) {
                    @Override
                public Message getSuccessMessage() {
                        return new Message("success","body");
                }
            }
        );
    }

    @Bean("nonDetainedCmrRelistingHoCoLrNotificationGenerator")
    public List<NotificationGenerator> nonDetainedCmrRelistingHoCoLrNotificationGenerator(
        LegalRepresentativeCmrRelistingPersonalisation legalRepresentativeCmrRelistingPersonalisation,
        CaseOfficerCmrRelistingPersonalisation caseOfficerCmrRelistingPersonalisation,
        HomeOfficeCmrRelistingPersonalisation homeOfficeCmrRelistingPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        List<EmailNotificationPersonalisation> emailPersonalisations = newArrayList(
            legalRepresentativeCmrRelistingPersonalisation,
            caseOfficerCmrRelistingPersonalisation,
            homeOfficeCmrRelistingPersonalisation
        );

        return newArrayList(
            new EmailNotificationGenerator(
                emailPersonalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("nonDetainedCmrRelistingAppellantEmailNotificationGenerator")
    public List<NotificationGenerator> nonDetainedCmrRelistingAppellantEmailNotificationGenerator(
        AppellantCmrRelistingPersonalisationEmail appellantCmrRelistingPersonalisationEmail,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return newArrayList(
            new EmailNotificationGenerator(
                newArrayList(appellantCmrRelistingPersonalisationEmail),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("nonDetainedCmrRelistingAppellantSmsNotificationGenerator")
    public List<NotificationGenerator> nonDetainedCmrRelistingAppellantSmsNotificationGenerator(
        AppellantCmrRelistingPersonalisationSms appellantCmrRelistingPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return newArrayList(
            new SmsNotificationGenerator(
                newArrayList(appellantCmrRelistingPersonalisationSms),
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("cmrRelistedAipHoCoEmailsGenerator")
    public List<NotificationGenerator> cmrRelistedAipHoCoEmailsGenerator(
        CaseOfficerCmrRelistingPersonalisation caseOfficerCmrRelistingPersonalisation,
        HomeOfficeCmrRelistingPersonalisation homeOfficeCmrRelistingPersonalisation,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        List<EmailNotificationPersonalisation> emailPersonalisations = newArrayList(
            caseOfficerCmrRelistingPersonalisation,
            homeOfficeCmrRelistingPersonalisation
        );
        return newArrayList(
            new EmailNotificationGenerator(
                emailPersonalisations,
                notificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("cmrRelistedAppellantEmailsGenerator")
    public List<NotificationGenerator> cmrRelistedAppellantEmailsGenerator(
        AipCmrRelistedAppellantEmailPersonalisation aipCmrRelistedAppellantEmailPersonalisation,
        GovNotifyNotificationSender govNotifyNotificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return newArrayList(
            new EmailNotificationGenerator(
                newArrayList(aipCmrRelistedAppellantEmailPersonalisation),
                govNotifyNotificationSender,
                notificationIdAppender
            )
        );
    }

    @Bean("cmrRelistedAppellantSmsGenerator")
    public List<NotificationGenerator> cmrRelistedAppellantSmsGenerator(
        AipCmrRelistedAppellantSmsPersonalisation aipCmrRelistedAppellantSmsPersonalisation,
        GovNotifyNotificationSender govNotifyNotificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        return newArrayList(
            new SmsNotificationGenerator(
                newArrayList(aipCmrRelistedAppellantSmsPersonalisation),
                govNotifyNotificationSender,
                notificationIdAppender
            )
        );
    }
}
