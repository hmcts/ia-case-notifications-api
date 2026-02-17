package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.CommonUtils.convertAsylumCaseFeeValue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@Service
public class PaPayLaterDecisionPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String paPayLaterDecisionTemplateId;
    private final RecipientsFinder recipientsFinder;

    public PaPayLaterDecisionPersonalisationEmail(
            @Value("${govnotify.template.decision.paPayLater.email}") String paPayLaterDecisionTemplateId,
            RecipientsFinder recipientsFinder
    ) {
        this.paPayLaterDecisionTemplateId = paPayLaterDecisionTemplateId;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return paPayLaterDecisionTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_PA_PAY_LATER_DECISION_EMAIL";
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

