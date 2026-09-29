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

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLegalRepAddressInCountryOrOoc;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.*;

@Service
@Slf4j
public class LegalRepUploadEvidenceStatutoryTimeframe24WeeksPersonalisationLetter implements LetterNotificationPersonalisation {

    private final String uploadLetterId;
    private final CustomerServicesProvider customerServicesProvider;

    public LegalRepUploadEvidenceStatutoryTimeframe24WeeksPersonalisationLetter(
            @Value(STF_24_WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_APPELLANT_LETTER_TEMPLATE) String uploadLetterId,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.uploadLetterId = uploadLetterId;

        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return uploadLetterId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return getLegalRepAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + STATUTORY_TIMEFRAME_24WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_LR_LETTER;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return Stf24WeeksUtil.buildCommonParams(Stf24WeeksNotificationFor.LEGAL_REPRESENTATIVE, asylumCase, customerServicesProvider).build();
    }
}
