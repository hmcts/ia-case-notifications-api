package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@Service
public class HomeOfficeFtpaApplicationDecisionPersonalisation implements EmailNotificationPersonalisation {

    private final GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;
    private final PersonalisationProvider personalisationProvider;
    private final String homeOfficeEmailAddressFtpaGranted;
    private final String homeOfficeEmailAddressFtpaRefused;
    private EmailAddressFinder emailAddressFinder;
    private final CustomerServicesProvider customerServicesProvider;

    public HomeOfficeFtpaApplicationDecisionPersonalisation(
        GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration,
        PersonalisationProvider personalisationProvider,
        @Value("${allowedAppealHomeOfficeEmailAddress}") String homeOfficeEmailAddressFtpaGranted,
        @Value("${dismissedAppealHomeOfficeEmailAddress}") String homeOfficeEmailAddressFtpaRefused,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.govNotifyTemplateIdConfiguration = govNotifyTemplateIdConfiguration;
        this.personalisationProvider = personalisationProvider;
        this.homeOfficeEmailAddressFtpaGranted = homeOfficeEmailAddressFtpaGranted;
        this.homeOfficeEmailAddressFtpaRefused = homeOfficeEmailAddressFtpaRefused;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        YesOrNo isRespondent = this.personalisationProvider.getApplicantType(asylumCase).equals(ApplicantType.RESPONDENT) ? YesOrNo.YES : YesOrNo.NO;
        return personalisationProvider.getFtpaDecisionTemplateId(asylumCase, govNotifyTemplateIdConfiguration, isRespondent);
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {

        if (getFtpaApplicationDecision(asylumCase).equals(FtpaAppellantDecisionOutcomeType.FTPA_GRANTED)
            || getFtpaApplicationDecision(asylumCase).equals(FtpaAppellantDecisionOutcomeType.FTPA_PARTIALLY_GRANTED)) {
            return Collections.singleton(homeOfficeEmailAddressFtpaGranted);
        } else if (getFtpaApplicationDecision(asylumCase).equals(FtpaAppellantDecisionOutcomeType.FTPA_REFUSED)) {
            return Collections.singleton(homeOfficeEmailAddressFtpaRefused);
        } else {
            return Collections.singleton(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .putAll(personalisationProvider.getFtpaDecisionPersonalisation(asylumCase));

        return listCaseFields.build();
    }

    protected FtpaAppellantDecisionOutcomeType getFtpaApplicationDecision(AsylumCase asylumCase) {
        return asylumCase
            .read(AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaAppellantDecisionOutcomeType.class)
            .orElseThrow(() -> new IllegalStateException("ftpaApplicationDecision is not present"));
    }
}
