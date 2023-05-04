package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.*;

@Service
public class LegalRepresentativeListCasePersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativeCaseListedNonAdaTemplateId;
    private final String legalRepresentativeCaseListedAdaTemplateId;
    private final String legalRepresentativeOutOfCountryCaseListedTemplateId;
    private final String iaExUiFrontendUrl;
    private final DateTimeExtractor dateTimeExtractor;
    private final CustomerServicesProvider customerServicesProvider;
    private final HearingDetailsFinder hearingDetailsFinder;


    public LegalRepresentativeListCasePersonalisation(
        @Value("${govnotify.template.caseListed.legalRep.email.nonAda}") String legalRepresentativeCaseListedNonAdaTemplateId,
        @Value("${govnotify.template.caseListed.legalRep.email.ada}") String legalRepresentativeCaseListedAdaTemplateId,
        @Value("${govnotify.template.caseListed.remoteHearing.legalRep.email}") String legalRepresentativeOutOfCountryCaseListedTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        DateTimeExtractor dateTimeExtractor,
        CustomerServicesProvider customerServicesProvider,
        HearingDetailsFinder hearingDetailsFinder
    ) {
        this.legalRepresentativeCaseListedNonAdaTemplateId = legalRepresentativeCaseListedNonAdaTemplateId;
        this.legalRepresentativeCaseListedAdaTemplateId = legalRepresentativeCaseListedAdaTemplateId;
        this.legalRepresentativeOutOfCountryCaseListedTemplateId = legalRepresentativeOutOfCountryCaseListedTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.dateTimeExtractor = dateTimeExtractor;
        this.customerServicesProvider = customerServicesProvider;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .map(centre -> centre == HearingCentre.REMOTE_HEARING)
            .orElse(false)) {
            return legalRepresentativeOutOfCountryCaseListedTemplateId;
        } else {
            return AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase)
                ? legalRepresentativeCaseListedAdaTemplateId
                : legalRepresentativeCaseListedNonAdaTemplateId;
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_LISTED_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase)))
            .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getHearingDateTime(asylumCase)))
            .put("hearingCentreAddress", hearingDetailsFinder.getHearingCentreLocation(asylumCase));

        PersonalisationProvider.buildHearingRequirementsFields(asylumCase, listCaseFields);

        return listCaseFields.build();

    }
}
