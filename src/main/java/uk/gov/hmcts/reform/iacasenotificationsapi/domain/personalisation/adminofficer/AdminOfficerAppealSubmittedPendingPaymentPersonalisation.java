package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionType.HO_WAIVER_REMISSION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionType.NO_REMISSION;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;

@Service
public class AdminOfficerAppealSubmittedPendingPaymentPersonalisation implements EmailNotificationPersonalisation {

    private final String adminOfficerAppealSubmittedPendingPaymentTemplateId;
    private final String adminOfficerAppealSubmittedPendingPaymentWithRemissionTemplateId;
    private final String feesAdminOfficerEmailAddress;
    private final AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;


    public AdminOfficerAppealSubmittedPendingPaymentPersonalisation(
        @NotNull(message = "pendingPaymentAdminOfficerTemplateId cannot be null")
        @Value("${govnotify.template.appealSubmitted.adminOfficer.pendingPaymentEaHu.email}")
            String adminOfficerAppealSubmittedPendingPaymentTemplateId,
        @NotNull(message = "pendingPaymentAdminOfficerWithRemissionTemplateId cannot be null")
        @Value("${govnotify.template.appealSubmitted.adminOfficer.remission.email}")
            String adminOfficerAppealSubmittedPendingPaymentWithRemissionTemplateId,
        @Value("${feesAdminOfficerEmailAddress}")
            String feesAdminOfficerEmailAddress,
        AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider
    ) {
        this.adminOfficerAppealSubmittedPendingPaymentTemplateId = adminOfficerAppealSubmittedPendingPaymentTemplateId;
        this.adminOfficerAppealSubmittedPendingPaymentWithRemissionTemplateId =
            adminOfficerAppealSubmittedPendingPaymentWithRemissionTemplateId;
        this.feesAdminOfficerEmailAddress = feesAdminOfficerEmailAddress;
        this.adminOfficerPersonalisationProvider = adminOfficerPersonalisationProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_SUBMITTED_PENDING_PAYMENT_ADMIN_OFFICER";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        RemissionType remissionType = asylumCase
            .read(REMISSION_TYPE, RemissionType.class).orElse(NO_REMISSION);

        if (remissionType.equals(HO_WAIVER_REMISSION)) {
            return adminOfficerAppealSubmittedPendingPaymentWithRemissionTemplateId;
        }
        return adminOfficerAppealSubmittedPendingPaymentTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(feesAdminOfficerEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase);
    }
}
