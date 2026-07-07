package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

@Service
public class LegalRepresentativeInPersonCmrListCasePersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String listAssistHearingLegalRepresentativeCaseListedTemplateId;
    private final String iaExUiFrontendUrl;
    private final int appellantProvidingAppealArgumentDeadlineDelay;
    private final int respondentResponseToAppealArgumentDeadlineDelay;
    private final DateTimeExtractor dateTimeExtractor;
    private final CustomerServicesProvider customerServicesProvider;
    private final HearingDetailsFinder hearingDetailsFinder;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeInPersonCmrListCasePersonalisation(
        @Value("${govnotify.template.listAssistHearing.caseListed.legalRep.email}") String listAssistHearingLegalRepresentativeCaseListedTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        @Value("${adaCaseListed.deadlines.appellantProvidingAppealArgumentDelay}") int appellantProvidingAppealArgumentDeadlineDelay,
        @Value("${adaCaseListed.deadlines.respondentResponseToAppealArgumentDelay}") int respondentResponseToAppealArgumentDeadlineDelay,
        DateTimeExtractor dateTimeExtractor,
        CustomerServicesProvider customerServicesProvider,
        HearingDetailsFinder hearingDetailsFinder
    ) {
        this.listAssistHearingLegalRepresentativeCaseListedTemplateId = listAssistHearingLegalRepresentativeCaseListedTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.appellantProvidingAppealArgumentDeadlineDelay = appellantProvidingAppealArgumentDeadlineDelay;
        this.respondentResponseToAppealArgumentDeadlineDelay = respondentResponseToAppealArgumentDeadlineDelay;
        this.dateTimeExtractor = dateTimeExtractor;
        this.customerServicesProvider = customerServicesProvider;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return listAssistHearingLegalRepresentativeCaseListedTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CMR_CASE_LISTED_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase))
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)))
            .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)))
            .put("hearingCentreAddress", hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase));

        if (isAcceleratedDetainedAppeal(asylumCase)) {
            listCaseFields
                .put("appellantProvidingAppealArgumentDeadline",
                    LocalDate.now().plusDays(appellantProvidingAppealArgumentDeadlineDelay)
                        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
                .put("respondentResponseToAppealArgumentDeadline",
                    LocalDate.now().plusDays(respondentResponseToAppealArgumentDeadlineDelay)
                        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        }

        PersonalisationProvider.buildHearingRequirementsFields(asylumCase, listCaseFields);

        return listCaseFields.build();

    }
}
