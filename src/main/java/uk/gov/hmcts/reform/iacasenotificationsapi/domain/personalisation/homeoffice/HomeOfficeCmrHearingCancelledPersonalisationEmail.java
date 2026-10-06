package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Map;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

@Service
public class HomeOfficeCmrHearingCancelledPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String cmrCancelledHomeOfficeEmailTemplateId;
    private final String iaExUiFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;


    public HomeOfficeCmrHearingCancelledPersonalisationEmail(
            @Value("${govnotify.template.cmrCancelled.homeOffice.email}") String cmrCancelledHomeOfficeEmailTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            EmailAddressFinder emailAddressFinder,
            DateTimeExtractor dateTimeExtractor,
            HearingDetailsFinder hearingDetailsFinder

    ) {
        this.cmrCancelledHomeOfficeEmailTemplateId = cmrCancelledHomeOfficeEmailTemplateId;
        this.emailAddressFinder = emailAddressFinder;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return cmrCancelledHomeOfficeEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return singleton(emailAddressFinder.getCmrListingHomeOfficeEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CMR_CANCELLED_HOME_OFFICE_EMAIL";
    }


    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                    .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                    .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                    .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                    .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                    .put("oldHearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)))
                    .put("oldHearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)))
                    .put("oldHearingCentreAddress", hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase))
                    .put("linkToOnlineService", iaExUiFrontendUrl)
                    .build();
    }
}
