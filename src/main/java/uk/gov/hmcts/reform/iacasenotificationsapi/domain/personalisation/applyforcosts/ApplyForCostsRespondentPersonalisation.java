package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.*;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplyForCosts;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class ApplyForCostsRespondentPersonalisation implements EmailNotificationPersonalisation {

    private final String applyForCostsNotificationForRespondentTemplateId;
    private final String homeOfficeEmailAddress;
    private final String iaExUiFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;
    private final String homeOffice = "Home office";
    private String newestApplicationCreatedNumber = "1";

    public ApplyForCostsRespondentPersonalisation(
        @Value("${govnotify.template.applyForCostsNotificationForRespondent.email}") String applyForCostsNotificationForRespondentTemplateId,
        @Value("${applyForCostsHomeOfficeEmailAddress}") String homeOfficeEmailAddress,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        PersonalisationProvider personalisationProvider
    ) {
        this.applyForCostsNotificationForRespondentTemplateId = applyForCostsNotificationForRespondentTemplateId;
        this.homeOfficeEmailAddress = homeOfficeEmailAddress;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return applyForCostsNotificationForRespondentTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (isHomeOfficeRespondent(asylumCase)) {
            return Collections.singleton(homeOfficeEmailAddress);
        } else {
            return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPLY_FOR_COSTS_SUBMITTED";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");


        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("respondent",
                    isHomeOfficeRespondent(asylumCase)
                        ? retrieveLatestApplyForCosts(asylumCase).getValue().getRespondentToCostsOrder()
                        : "Your"
                )
                .put("respondentReferenceNumber",
                    isHomeOfficeRespondent(asylumCase)
                        ? asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse("")
                        : asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .putAll(personalisationProvider.getAppellantCredentials(asylumCase))
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("linkToOnlineService", iaExUiFrontendUrl)
                //ExUi will always display the latest application with number 1 in 'Costs' tab
                .put("applicationId", newestApplicationCreatedNumber)
                .put("appliedCostsType", retrieveLatestApplyForCosts(
                    asylumCase).getValue().getAppliedCostsType().replaceAll("costs", "").trim()
                )
                .build();
    }

    private boolean isHomeOfficeRespondent(AsylumCase asylumCase) {
        ApplyForCosts latestApplyForCosts = retrieveLatestApplyForCosts(asylumCase).getValue();
        return latestApplyForCosts.getRespondentToCostsOrder().equals(homeOffice);
    }

    private IdValue<ApplyForCosts> retrieveLatestApplyForCosts(AsylumCase asylumCase) {
        Optional<List<IdValue<ApplyForCosts>>> applyForCosts = asylumCase.read(APPLIES_FOR_COSTS);

        if (applyForCosts.isPresent()) {
            List<IdValue<ApplyForCosts>> applyForCostsList = applyForCosts.get();
            return applyForCostsList.get(0);
        } else {
            throw new IllegalStateException("Applies for costs are not present");
        }
    }
}
