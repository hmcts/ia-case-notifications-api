package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.EMAIL;

@Service
public class AipAppellantStartAppealDisposalPersonalisationEmail implements EmailNotificationPersonalisation {
    private final String appealStartedAppellantAipDisposalEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final AppealService appealService;
    private final CustomerServicesProvider customerServicesProvider;

    public AipAppellantStartAppealDisposalPersonalisationEmail(
        @Value("${govnotify.template.appealStarted.appellant.aip.disposal.email}") String appealStartedAppellantAipDisposalEmailTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        RecipientsFinder recipientsFinder,
        AppealService appealService,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.appealStartedAppellantAipDisposalEmailTemplateId = appealStartedAppellantAipDisposalEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.appealService = appealService;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appealStartedAppellantAipDisposalEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        if (appealService.isAppellantInPersonJourney(asylumCase)) {
            return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
        } else {
            return Collections.singleton(asylumCase
                .read(EMAIL, String.class)
                .orElseThrow(() -> new IllegalStateException("appellantEmailAddress is not present")));
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_STARTED_APPELLANT_AIP_DISPOSAL";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return
            ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("creationDate", LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")))
                .put("linkToOnlineService", iaAipFrontendUrl)
                .build();
    }
}
