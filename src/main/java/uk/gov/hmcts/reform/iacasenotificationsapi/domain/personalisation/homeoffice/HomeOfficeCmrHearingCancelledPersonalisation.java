package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;

@Slf4j
@Service
public class HomeOfficeCmrHearingCancelledPersonalisation implements EmailNotificationPersonalisation {

    private final String caseOfficerCmrHearingCancelledTemplateId;
    private final String iaExUiFrontendUrl;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final EmailAddressFinder emailAddressFinder;

    public HomeOfficeCmrHearingCancelledPersonalisation(
        @Value("${govnotify.template.cmrHearingCancelled.caseOfficer.email}") String caseOfficerCmrHearingCancelledTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        DateTimeExtractor dateTimeExtractor,
        HearingDetailsFinder hearingDetailsFinder,
        EmailAddressFinder emailAddressFinder
    ) {
        this.caseOfficerCmrHearingCancelledTemplateId = caseOfficerCmrHearingCancelledTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId() {
        return caseOfficerCmrHearingCancelledTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CMR_HEARING_CANCELLED_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "asylumCase must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<CaseDetails<AsylumCase>> caseDetailsBefore = callback.getCaseDetailsBefore();

        String oldHearingDate;
        String oldHearingTime;
        String oldHearingCentreAddress;
        if (caseDetailsBefore.isPresent()) {
            AsylumCase asylumCaseBefore = caseDetailsBefore.get().getCaseData();
            oldHearingDate = dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCaseBefore));
            oldHearingTime = dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getHearingDateTime(asylumCaseBefore));
            oldHearingCentreAddress = hearingDetailsFinder.getHearingCentreAddress(asylumCaseBefore);
        } else {
            oldHearingDate = "";
            oldHearingTime = "";
            oldHearingCentreAddress = "";
        }

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("oldHearingDate", oldHearingDate)
            .put("oldHearingTime", oldHearingTime)
            .put("oldHearingCentreAddress", oldHearingCentreAddress)
            .build();
    }
}
