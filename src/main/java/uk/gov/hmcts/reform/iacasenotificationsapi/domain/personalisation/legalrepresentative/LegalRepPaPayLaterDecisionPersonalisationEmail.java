package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.CommonUtils.convertAsylumCaseFeeValue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

@Service
public class LegalRepPaPayLaterDecisionPersonalisationEmail implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepPaPayLaterDecisionTemplateId;

    public LegalRepPaPayLaterDecisionPersonalisationEmail(
            @Value("${govnotify.template.decision.paPayLater.email}") String legalRepPaPayLaterDecisionTemplateId
    ) {
        this.legalRepPaPayLaterDecisionTemplateId = legalRepPaPayLaterDecisionTemplateId;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return legalRepPaPayLaterDecisionTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LR_PA_PAY_LATER_DECISION_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .put("feeAmount", convertAsylumCaseFeeValue(asylumCase.read(FEE_AMOUNT_GBP, String.class).orElse("")))
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .build();
    }
}
