package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;


@Service
public class LegalRepresentativeRaiseQueryPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativeRaiseQueryTemplateId;
    private final String iaExUiFrontendUrl;

    public LegalRepresentativeRaiseQueryPersonalisation(
            @Value("${govnotify.template.raiseQuery.legalRep.email}") String legalRepresentativeRaiseQueryTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl
    ) {
        this.legalRepresentativeRaiseQueryTemplateId = legalRepresentativeRaiseQueryTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LR_RAISE_QUERY_EMAIL_NOTIFICATION";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return legalRepresentativeRaiseQueryTemplateId;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap.<String, String>builder().put("appealReferenceNumber",
                asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("legalRepReferenceNumber",
                        asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames",
                        asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName",
                        asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }
}
