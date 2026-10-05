package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLegalRepAddressInCountryOrOoc;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.*;

@Service
public class LegalRepresentativeHearingReqReviewStatutoryTimeframe24WeeksPersonalisationLetter implements LetterNotificationPersonalisation {

    private final String templateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;

    public LegalRepresentativeHearingReqReviewStatutoryTimeframe24WeeksPersonalisationLetter(
            @Value(STF_24_WEEKS_HEARING_REQUIREMENTS_LETTER_TEMPLATE) String templateId,
            CustomerServicesProvider customerServicesProvider,
            DateTimeExtractor dateTimeExtractor,
            HearingDetailsFinder hearingDetailsFinder) {
        this.templateId = templateId;
        this.customerServicesProvider = customerServicesProvider;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;
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
        return caseId + STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_LR_LETTER;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return Stf24WeeksUtil.buildHearingRequirementsLetterParameters(Stf24WeeksUtil.Stf24WeeksNotificationFor.LEGAL_REPRESENTATIVE, asylumCase, customerServicesProvider, dateTimeExtractor, hearingDetailsFinder);
    }
}
