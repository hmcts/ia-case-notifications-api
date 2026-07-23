package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;


import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@Service
public class LegalRepresentativeCmrHearingCancelledPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepCmrHearingCancelledTemplateId;
    private final String iaExUiFrontendUrl;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;

    public LegalRepresentativeCmrHearingCancelledPersonalisation(
            @Value("${govnotify.template.cmrCancelled.legalRep.email}") String legalRepCmrHearingCancelledTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            DateTimeExtractor dateTimeExtractor,
            HearingDetailsFinder hearingDetailsFinder
    ) {
        this.legalRepCmrHearingCancelledTemplateId = legalRepCmrHearingCancelledTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return legalRepCmrHearingCancelledTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CMR_HEARING_CANCELLED_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .put("oldHearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)))
                .put("oldHearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)))
                .put("oldHearingCentreAddress", hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase))
                .build();
    }
}

