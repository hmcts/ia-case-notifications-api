package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NLR_DETAILS;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;


@Service
public class JoinAppealConfirmationAppellantPersonalisationSms implements SmsNotificationPersonalisation {

    private final String joinAppealConfirmationTemplateId;
    private final RecipientsFinder recipientsFinder;

    public JoinAppealConfirmationAppellantPersonalisationSms(
        @Value("${govnotify.template.nlr.joinAppealConfirmation.appellant.sms}") String joinAppealConfirmationTemplateId,
        RecipientsFinder recipientsFinder
    ) {
        this.joinAppealConfirmationTemplateId = joinAppealConfirmationTemplateId;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId() {
        return joinAppealConfirmationTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_NON_LEGAL_REP_JOIN_APPEAL_CONFIRMATION_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        NonLegalRepDetails nlrDetails = asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)
            .orElse(null);
        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("nlrGivenNames", nlrDetails != null ? nlrDetails.getGivenNames() : "Sir /")
                .put("nlrFamilyName", nlrDetails != null ? nlrDetails.getFamilyName() : "Madam")
                .build();
    }
}
