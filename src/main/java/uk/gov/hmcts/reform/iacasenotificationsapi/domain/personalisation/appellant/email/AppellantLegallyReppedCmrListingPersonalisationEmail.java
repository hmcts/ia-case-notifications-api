package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAipJourney;

@Service
public class AppellantLegallyReppedCmrListingPersonalisationEmail implements EmailNotificationPersonalisation {

    private static final Logger log = LoggerFactory.getLogger(AppellantLegallyReppedCmrListingPersonalisationEmail.class);
    private final String appellantLrCmrListingEmailTemplateId;
    private final String appellantLrCmrListingRemoteEmailTemplateId;
    private final String iaExUiFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;
    private final DateTimeExtractor dateTimeExtractor;
    private final RecipientsFinder recipientsFinder;
    private final HearingDetailsFinder hearingDetailsFinder;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public AppellantLegallyReppedCmrListingPersonalisationEmail(

            @Value("${govnotify.template.listAssistHearing.caseListed.appellant.email}") String appellantLrCmrListingEmailTemplateId,
            @Value("${govnotify.template.listAssistHearing.caseListed.remoteHearing.appellant.email}") String appellantLrCmrListingRemoteEmailTemplateId, //template required, currently placeholder
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            PersonalisationProvider personalisationProvider,
            CustomerServicesProvider customerServicesProvider,
            RecipientsFinder recipientsFinder,
            DateTimeExtractor dateTimeExtractor,
            HearingDetailsFinder hearingDetailsFinder
    ) {
        this.appellantLrCmrListingEmailTemplateId = appellantLrCmrListingEmailTemplateId;
        this.appellantLrCmrListingRemoteEmailTemplateId = appellantLrCmrListingRemoteEmailTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.dateTimeExtractor = dateTimeExtractor;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (asylumCase.read(CMR_IS_REMOTE_HEARING, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            return
                    appellantLrCmrListingRemoteEmailTemplateId;
        } else {
            return appellantLrCmrListingEmailTemplateId;

        }
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return isAipJourney(asylumCase) ?
                recipientsFinder.findAll(asylumCase, NotificationType.EMAIL) :
                recipientsFinder.findReppedAppellant(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CMR_LISTED_OR_REMOTE_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        log.info("AppellantLegallyReppedCmrListingPersonalisationEmail.getPersonalisation called for case");
        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)))
                .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)))
                .put("hearingCentreAddress", hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase))
                .put("linkToOnlineService", iaExUiFrontendUrl);




        return listCaseFields.build();
    }
}