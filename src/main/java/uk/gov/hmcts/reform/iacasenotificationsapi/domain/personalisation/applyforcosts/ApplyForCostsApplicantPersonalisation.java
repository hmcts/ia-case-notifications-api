package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isHomeOfficeApplicant;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.retrieveLatestApplyForCosts;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class ApplyForCostsApplicantPersonalisation implements EmailNotificationPersonalisation {

    private final String applyForCostsNotificationForApplicantTemplateId;
    private final String homeOfficeEmailAddress;
    private final EmailAddressFinder emailAddressFinder;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public ApplyForCostsApplicantPersonalisation(
        @Value("${govnotify.template.applyForCostsNotification.applicant.email}") String applyForCostsNotificationForApplicantTemplateId,
        @Value("${applyForCostsHomeOfficeEmailAddress}") String homeOfficeEmailAddress,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider,
        PersonalisationProvider personalisationProvider
    ) {
        this.applyForCostsNotificationForApplicantTemplateId = applyForCostsNotificationForApplicantTemplateId;
        this.homeOfficeEmailAddress = homeOfficeEmailAddress;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return applyForCostsNotificationForApplicantTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (isHomeOfficeApplicant(asylumCase)) {
            return Collections.singleton(homeOfficeEmailAddress);
        } else {
            return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPLY_FOR_COSTS_APPLICANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        ImmutableMap.Builder<String, String> personalisationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(personalisationProvider.getApplyForCostsPesonalisation(asylumCase))
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation());

        final String applicantReferenceNumber = "applicantReferenceNumber";
        final String applicant = "applicant";

        if (isHomeOfficeApplicant(asylumCase)) {
            personalisationBuilder
                .put(applicant, retrieveLatestApplyForCosts(asylumCase).getValue().getApplyForCostsApplicantType())
                .put(applicantReferenceNumber, asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));
        } else {
            personalisationBuilder
                .put(applicant, "Your")
                .put(applicantReferenceNumber, asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""));
        }

        return personalisationBuilder.build();
    }
}
