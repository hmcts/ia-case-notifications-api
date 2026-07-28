package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLegalRepAddressInCountryOrOoc;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_LETTER;

import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class LegalRepresentativeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisationLetter implements LetterNotificationPersonalisation {

    private final String templateId;
    private final CustomerServicesProvider customerServicesProvider;

    public LegalRepresentativeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisationLetter(
            @Value("${govnotify.template.completeCaseReviewStatutoryTimeframe24Weeks.legalRep.letter}") String templateId,
            CustomerServicesProvider customerServicesProvider) {
        this.templateId = templateId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return templateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return getLegalRepAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_LETTER;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return Stf24WeeksUtil.buildLetterParams(asylumCase, customerServicesProvider, false);
    }
}
