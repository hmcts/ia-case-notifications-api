package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplicantType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@Service
public class HomeOfficeFtpaApplicationDecisionPersonalisation implements EmailNotificationPersonalisation {

    private final GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;
    private final PersonalisationProvider personalisationProvider;
    private final String homeOfficeEmailAddress;


    public HomeOfficeFtpaApplicationDecisionPersonalisation(
        GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration,
        PersonalisationProvider personalisationProvider,
        @Value("${allowedAppealHomeOfficeEmailAddress}") String homeOfficeEmailAddress) {
        this.govNotifyTemplateIdConfiguration = govNotifyTemplateIdConfiguration;
        this.personalisationProvider = personalisationProvider;
        this.homeOfficeEmailAddress = homeOfficeEmailAddress;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        YesOrNo isRespondent = this.personalisationProvider.getApplicantType(asylumCase).equals(ApplicantType.RESPONDENT) ? YesOrNo.YES : YesOrNo.NO;
        return personalisationProvider.getFtpaDecisionTemplateId(asylumCase, govNotifyTemplateIdConfiguration, isRespondent);
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(homeOfficeEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return this.personalisationProvider.getFtpaDecisionPersonalisation(asylumCase);
    }
}
