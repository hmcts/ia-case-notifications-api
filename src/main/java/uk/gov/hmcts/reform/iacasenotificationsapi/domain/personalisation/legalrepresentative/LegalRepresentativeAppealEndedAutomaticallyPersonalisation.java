package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class LegalRepresentativeAppealEndedAutomaticallyPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativeAppealEndedAutomaticallyTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;

    public LegalRepresentativeAppealEndedAutomaticallyPersonalisation(
        @NotNull(message = "legalRepresentativeAppealEndedAutomaticallyTemplateId cannot be null")
        @Value("${govnotify.template.appealEndedAutomatically.legalRep.email}")
            String legalRepresentativeAppealEndedAutomaticallyTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider
    ) {
        requireNonNull(iaExUiFrontendUrl, "iaExUiFrontendUrl must not be null");
        this.legalRepresentativeAppealEndedAutomaticallyTemplateId =
            legalRepresentativeAppealEndedAutomaticallyTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return legalRepresentativeAppealEndedAutomaticallyTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_ENDED_AUTOMATICALLY_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
                    .orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .put("endAppealDate", asylumCase.read(AsylumCaseDefinition.END_APPEAL_DATE, String.class)
                    .map(date -> LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy")))
                    .orElse(""))
                .build();
    }
}