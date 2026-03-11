package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
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
public class AipCmrRelistingPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String aipCmrRelistingAppellantEmailTemplateId;
    private final String aipCmrRelistingRemoteAppellantEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;
    private final DateTimeExtractor dateTimeExtractor;
    private final RecipientsFinder recipientsFinder;
    private final HearingDetailsFinder hearingDetailsFinder;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public AipCmrRelistingPersonalisationEmail(

            @Value("${govnotify.template.caseEdited.appellant.email}") String aipCmrListingAppellantEmailTemplateId,
            @Value("${govnotify.template.listAssistHearing.caseEdited.appellant.email}") String aipCmrListingRemoteAppellantEmailTemplateId, //template required, currently placeholder
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            PersonalisationProvider personalisationProvider,
            CustomerServicesProvider customerServicesProvider,
            RecipientsFinder recipientsFinder,
            DateTimeExtractor dateTimeExtractor,
            HearingDetailsFinder hearingDetailsFinder
    ) {
        this.aipCmrRelistingAppellantEmailTemplateId = aipCmrListingAppellantEmailTemplateId;
        this.aipCmrRelistingRemoteAppellantEmailTemplateId = aipCmrListingRemoteAppellantEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.dateTimeExtractor = dateTimeExtractor;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (asylumCase.read(IS_REMOTE_CMR_HEARING, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            return
                    aipCmrRelistingRemoteAppellantEmailTemplateId;
        } else {
            return aipCmrRelistingAppellantEmailTemplateId;

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
        return caseId + "_CMR_RELISTED_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaAipFrontendUrl)
                .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase)))
                .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getHearingDateTime(asylumCase)))
                .put("hearingCentreAddress", hearingDetailsFinder.getHearingCentreLocation(asylumCase));


        PersonalisationProvider.buildCmrHearingRequirementsFields(asylumCase, listCaseFields);

        return listCaseFields.build();
    }
}
