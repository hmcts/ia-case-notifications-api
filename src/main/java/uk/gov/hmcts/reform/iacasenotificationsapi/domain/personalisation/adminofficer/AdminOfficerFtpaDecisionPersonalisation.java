package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaAppellantDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;


@Service
public class AdminOfficerFtpaDecisionPersonalisation implements EmailNotificationPersonalisation {

    private final GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;
    private final String reviewHearingRequirementsAdminOfficerEmailAddress;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public AdminOfficerFtpaDecisionPersonalisation(
        GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration,
        @Value("${reviewHearingRequirementsAdminOfficerEmailAddress}") String reviewHearingRequirementsAdminOfficerEmailAddress,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.govNotifyTemplateIdConfiguration = govNotifyTemplateIdConfiguration;
        this.reviewHearingRequirementsAdminOfficerEmailAddress = reviewHearingRequirementsAdminOfficerEmailAddress;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return getFtpaApplicationDecision(asylumCase).equals(FtpaAppellantDecisionOutcomeType.FTPA_GRANTED)
            ? govNotifyTemplateIdConfiguration.getApplicationGrantedAdmin()
            : govNotifyTemplateIdConfiguration.getApplicationPartiallyGrantedAdmin();
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(reviewHearingRequirementsAdminOfficerEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_ADMIN_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .putAll(personalisationProvider.getFtpaDecisionPersonalisation(asylumCase));

        return listCaseFields.build();
    }

    public FtpaAppellantDecisionOutcomeType getFtpaApplicationDecision(AsylumCase asylumCase) {
        return asylumCase
            .read(AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaAppellantDecisionOutcomeType.class)
            .orElseThrow(() -> new IllegalStateException("ftpaApplicationDecision is not present"));
    }
}
