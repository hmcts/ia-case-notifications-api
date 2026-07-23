package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getAppellantAddressInCountryOrOoc;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LEGAL_REP_COPY_LETTER;

@Service
@Slf4j
public class AppellantCompleteCaseReviewStatutoryTimeframe24WeeksLrCopyPersonalisationLetter implements LetterNotificationPersonalisation {

    private final String templateId;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantCompleteCaseReviewStatutoryTimeframe24WeeksLrCopyPersonalisationLetter(
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
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return getAppellantAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LEGAL_REP_COPY_LETTER;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        return Stf24WeeksUtil.buildLetterParams(asylumCase, customerServicesProvider, true);
    }
}
