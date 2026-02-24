package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REVOKE_ACCESS_DL;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class CaseRevokedV2Personalisation implements EmailNotificationPersonalisation {

    private final String caseRevokedV2TemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    public CaseRevokedV2Personalisation(
        @Value("${govnotify.template.nlr.caseRevokedV2.email}") String caseRevokedV2TemplateId,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.caseRevokedV2TemplateId = caseRevokedV2TemplateId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return caseRevokedV2TemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        DynamicList revokeAccessDl = asylumCase.read(REVOKE_ACCESS_DL, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("Dynamic list of users to revoke access from is not present."));
        if (revokeAccessDl.getValue() != null) {
            String[] userString = Optional.ofNullable(revokeAccessDl.getValue().getLabel())
                .orElse("")
                .split(" - ");
            if (userString.length > 1) {
                String userEmail = userString[0].trim();
                return Collections.singleton(userEmail);
            }
        }
        return Collections.emptySet();
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_REVOKED_V2_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        asylumCase.write(REVOKE_ACCESS_DL, null);

        final ImmutableMap.Builder<String, String> fields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""));

        return fields.build();
    }

}
