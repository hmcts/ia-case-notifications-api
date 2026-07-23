package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.notificationgenerators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Message;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email.AppellantCmrHearingCancelledPersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email.AppellantCmrListingPersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email.AppellantCmrRelistingPersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms.AppellantCmrHearingCancelledPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms.AppellantCmrListingPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms.AppellantCmrRelistingPersonalisationSms;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerAipCmrHearingCancelledPersonalisationEmail;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerCmrListingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.CaseOfficerCmrRelistingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam.DetentionEngagementTeamCmrListingPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam.DetentionEngagementTeamCmrListingProductionPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeAipCmrHearingCancelledPersonalisationEmail;
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
        LegalRepresentativeCmrListingPersonalisation legalRepresentativeCmrListingPersonalisation,
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
                    legalRepresentativeCmrListingPersonalisation,
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

    @Bean("legalRepDigitalCmrListingNotificationGenerator")
    public List<NotificationGenerator> legalRepDigitalCmrListingNotificationGenerator(
        LegalRepresentativeCmrListingPersonalisation legalRepresentativeCmrListingPersonalisation,
        CaseOfficerCmrListingPersonalisation caseOfficerCmrListingPersonalisation,
        HomeOfficeInPersonCmrListingCasePersonalisation homeOfficeInPersonCmrListingCasePersonalisation,
        AppellantCmrListingPersonalisationEmail appellantCmrListingPersonalisationEmail,
        AppellantCmrListingPersonalisationSms appellantCmrListingPersonalisationSms,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {

        return newArrayList(
            new EmailNotificationGenerator(
                newArrayList(
                    legalRepresentativeCmrListingPersonalisation,
                    caseOfficerCmrListingPersonalisation,
                    homeOfficeInPersonCmrListingCasePersonalisation,
                    appellantCmrListingPersonalisationEmail
                ),
                notificationSender,
                notificationIdAppender
            ),
            new SmsNotificationGenerator(
                newArrayList(
                        appellantCmrListingPersonalisationSms
                ),
                notificationSender,
                notificationIdAppender
            )
        );
    }

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
