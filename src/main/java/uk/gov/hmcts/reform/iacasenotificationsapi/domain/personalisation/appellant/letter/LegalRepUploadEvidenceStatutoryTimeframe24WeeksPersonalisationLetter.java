package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import lombok.extern.slf4j.Slf4j;
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

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getAppellantAddressInCountryOrOoc;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.*;

@Service
@Slf4j
public class LegalRepUploadEvidenceStatutoryTimeframe24WeeksPersonalisationLetter implements LetterNotificationPersonalisation {

    private final String removeStatutoryTimeframe24WeeksAppellantLetterId;
    private final CustomerServicesProvider customerServicesProvider;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;

    public LegalRepUploadEvidenceStatutoryTimeframe24WeeksPersonalisationLetter(
            @Value(STF_24_WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_APPELLANT_LETTER_TEMPLATE) String removeStatutoryTimeframe24WeeksAppellantLetterId,
            CustomerServicesProvider customerServicesProvider,
            DateTimeExtractor dateTimeExtractor,
            HearingDetailsFinder hearingDetailsFinder
    ) {
        this.removeStatutoryTimeframe24WeeksAppellantLetterId = removeStatutoryTimeframe24WeeksAppellantLetterId;

        this.customerServicesProvider = customerServicesProvider;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId() {
        return removeStatutoryTimeframe24WeeksAppellantLetterId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return getAppellantAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_APPELLANT_LETTER;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return Stf24WeeksUtil.buildHearingRequirementsLetterParameters(Stf24WeeksNotificationFor.APPELLANT, asylumCase, customerServicesProvider, dateTimeExtractor, hearingDetailsFinder);
    }
}
