package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

@Service
public class LegalRepCmrRelistingPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativeCmrRelistingTemplateId;
    private final String legalRepresentativeRemoteCmrTemplateId;
    private final String iaExUiFrontendUrl;
    private final DateTimeExtractor dateTimeExtractor;
    private final CustomerServicesProvider customerServicesProvider;
    private final HearingDetailsFinder hearingDetailsFinder;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepCmrRelistingPersonalisation(
            @Value("${govnotify.template.listAssistHearing.caseEdited.legalRep.email}") String legalRepresentativeCmrRelistingTemplateId,
            @Value("${govnotify.template.listAssistHearing.caseListed.remoteHearing.legalRep.email}") String legalRepresentativeRemoteCmrTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            DateTimeExtractor dateTimeExtractor,
            CustomerServicesProvider customerServicesProvider,
            HearingDetailsFinder hearingDetailsFinder
    ) {
        this.legalRepresentativeCmrRelistingTemplateId = legalRepresentativeCmrRelistingTemplateId;
        this.legalRepresentativeRemoteCmrTemplateId = legalRepresentativeRemoteCmrTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.dateTimeExtractor = dateTimeExtractor;
        this.customerServicesProvider = customerServicesProvider;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        boolean isRemote = asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .map(centre -> centre == HearingCentre.REMOTE_HEARING)
                .orElse(false);

        if (isRemote) {
            return legalRepresentativeRemoteCmrTemplateId;
        } else  {
            return legalRepresentativeCmrRelistingTemplateId;
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CMR_RELISTED_OR_REMOTE_LEGAL_REPRESENTATIVE_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Builder<String, String> listCaseFields = ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase)))
                .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getHearingDateTime(asylumCase)))
                .put("hearingCentreAddress", hearingDetailsFinder.getHearingCentreLocation(asylumCase))
                .put("remoteVideoCallTribunalResponse", asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class).orElse(""))
                .put("hearingRequirementVulnerabilities", asylumCase.read(HEARING_REQUIREMENT_VULNERABILITIES, String.class).orElse(""))
                .put("hearingRequirementMultimedia", asylumCase.read(HEARING_REQUIREMENT_MULTIMEDIA, String.class).orElse(""))
                .put("hearingRequirementSingleSexCourt", asylumCase.read(HEARING_REQUIREMENT_SINGLE_SEX_COURT, String.class).orElse(""))
                .put("hearingRequirementInCameraCourt", asylumCase.read(HEARING_REQUIREMENT_IN_CAMERA_COURT, String.class).orElse(""))
                .put("hearingRequirementOther", asylumCase.read(HEARING_REQUIREMENT_OTHER, String.class).orElse(""));

        PersonalisationProvider.buildHearingRequirementsFields(asylumCase, listCaseFields);

        return listCaseFields.build();

    }
}