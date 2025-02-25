package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;


@Service
public class LegalRepresentativeAppealStartedDisposalPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String appealStartedLegalRepresentativeDisposalTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;

    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeAppealStartedDisposalPersonalisation(
        @NotNull(message = "appealStartedLegalRepresentativeDisposalTemplateId cannot be null")
        @Value("${govnotify.template.appealStarted.legalRep.disposal.email}") String appealStartedLegalRepresentativeDisposalTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.appealStartedLegalRepresentativeDisposalTemplateId = appealStartedLegalRepresentativeDisposalTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appealStartedLegalRepresentativeDisposalTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_SUBMITTED_LEGAL_REP";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }
}
