package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@Service
public class AppellantRecordRefundDecisionPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String aipAppellantRemissionApprovedTemplateId;
    private final String iaAipFrontendUrl;
    private final int daysAfterRefundDecision;
    private final CustomerServicesProvider customerServicesProvider;
    private final RecipientsFinder recipientsFinder;
    private final SystemDateProvider systemDateProvider;

    public AppellantRecordRefundDecisionPersonalisationEmail(
        @Value("${govnotify.template.recordRefundDecision.appellant.approved.email}") String aipAppellantRemissionApprovedTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterRemissionDecision}") int daysAfterRefundDecision,
        CustomerServicesProvider customerServicesProvider,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider
    ) {
        this.aipAppellantRemissionApprovedTemplateId = aipAppellantRemissionApprovedTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysAfterRefundDecision = daysAfterRefundDecision;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REFUND_DECISION_DECIDED_AIP_APPELLANT_EMAIL";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        return aipAppellantRemissionApprovedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final String dueDate = systemDateProvider.dueDate(daysAfterRefundDecision);

        return ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("linkToService", iaAipFrontendUrl)
                .put("14DaysAfterRefundDecision", dueDate)
                .put("refundAmount", asylumCase.read(AMOUNT_REMITTED, String.class).orElse(""))
                .build();
    }
}
