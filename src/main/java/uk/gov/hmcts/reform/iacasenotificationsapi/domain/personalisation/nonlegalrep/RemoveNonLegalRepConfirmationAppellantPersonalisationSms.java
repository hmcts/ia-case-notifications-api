package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NLR_DETAILS;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
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
public class RemoveNonLegalRepConfirmationAppellantPersonalisationSms implements SmsNotificationPersonalisation {

    private final String removeNonLegalRepConfirmationTemplateId;
    private final RecipientsFinder recipientsFinder;

    public RemoveNonLegalRepConfirmationAppellantPersonalisationSms(
        @Value("${govnotify.template.nlr.removeNonLegalRepConfirmation.appellant.sms}") String removeNonLegalRepConfirmationTemplateId,
        RecipientsFinder recipientsFinder
    ) {
        this.removeNonLegalRepConfirmationTemplateId = removeNonLegalRepConfirmationTemplateId;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId() {
        return removeNonLegalRepConfirmationTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_NON_LEGAL_REP_CONFIRMATION_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<NonLegalRepDetails> nlrDetails = asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class);
        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("nlrGivenNames", nlrDetails.map(NonLegalRepDetails::getGivenNames).orElse("Sir /"))
                .put("nlrFamilyName", nlrDetails.map(NonLegalRepDetails::getFamilyName).orElse("Madam"))
                .build();
    }
}
