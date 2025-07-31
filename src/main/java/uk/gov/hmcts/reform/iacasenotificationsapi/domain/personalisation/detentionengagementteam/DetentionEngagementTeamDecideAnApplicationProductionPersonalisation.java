package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PrisonNomsNumber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;

@Slf4j
@Service
public class DetentionEngagementTeamDecideAnApplicationProductionPersonalisation implements EmailNotificationPersonalisation {

    private final String caseListedProductionDetainedTemplateId;
    private final DetentionEmailService detentionEmailService;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final String subjectPrefix;

    public DetentionEngagementTeamDecideAnApplicationProductionPersonalisation(
        @Value("${govnotify.template.decideAnApplication.otherParty.detentionEngagementTeam.production.email}") String caseListedProductionDetainedTemplateId,
        DetentionEmailService detentionEmailService,
        DateTimeExtractor dateTimeExtractor,
        HearingDetailsFinder hearingDetailsFinder,
        @Value("${govnotify.emailPrefix.nonAdaInPerson}") String subjectPrefix
    ) {
        this.caseListedProductionDetainedTemplateId = caseListedProductionDetainedTemplateId;
        this.detentionEmailService = detentionEmailService;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;
        this.subjectPrefix = subjectPrefix;
    }

    @Override
    public String getTemplateId() {
        return caseListedProductionDetainedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (!isAppellantInDetention(asylumCase)) {
            return Collections.emptySet();
        }

        Optional<String> detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class);
        if (detentionFacility.isEmpty() || detentionFacility.get().equals("other")) {
            return Collections.emptySet();
        }

        return Collections.singleton(detentionEmailService.getDetentionEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_DETAINED_APPLICATION_DECIDED_PRODUCTION_DET";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "asylumCase must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<CaseDetails<AsylumCase>> caseDetailsBefore = callback.getCaseDetailsBefore();

        String hearingDate;
        String hearingTime;
        String hearingCentreAddress;
        if (caseDetailsBefore.isPresent()) {
            AsylumCase asylumCaseBefore = caseDetailsBefore.get().getCaseData();
            hearingDate = dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCaseBefore));
            hearingTime = dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getHearingDateTime(asylumCaseBefore));
            hearingCentreAddress = hearingDetailsFinder.getHearingCentreAddress(asylumCaseBefore);
        } else {
            hearingDate = "";
            hearingTime = "";
            hearingCentreAddress = "";
        }

        boolean isPrison = asylumCase.read(DETENTION_FACILITY, String.class).orElse("").equals("prison");
        String prisonNomsNumber = isPrison
            ? asylumCase.read(PRISON_NOMS, PrisonNomsNumber.class)
            .map(prisonNoms -> "NOMS Ref: " + prisonNoms.getPrison()).orElse("")
            : "";

        return ImmutableMap
            .<String, String>builder()
            .put("subjectPrefix", subjectPrefix)
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("nomsRef", prisonNomsNumber)
            .put("hearingDate", hearingDate)
            .put("hearingTime", hearingTime)
            .put("hearingCentreAddress", hearingCentreAddress)
            .put("detentionBuilding", asylumCase.read(DETENTION_BUILDING, String.class).orElse(""))
            .build();
    }
}
