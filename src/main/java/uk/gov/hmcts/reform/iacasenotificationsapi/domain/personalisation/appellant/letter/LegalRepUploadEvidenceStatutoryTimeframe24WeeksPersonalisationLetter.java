package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLegalRepAddressInCountryOrOoc;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.*;

@Service
@Slf4j
public class LegalRepUploadEvidenceStatutoryTimeframe24WeeksPersonalisationLetter implements LetterNotificationPersonalisation {

    private final String templateId;
    private final CustomerServicesProvider customerServicesProvider;

    public LegalRepUploadEvidenceStatutoryTimeframe24WeeksPersonalisationLetter(
            @Value(STF_24_WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_LETTER_TEMPLATE) String templateId,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.templateId = templateId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return templateId;
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
        ImmutableMap.Builder<String, String> builder = populateUploadEvidenceLetterParams(asylumCase, Stf24WeeksNotificationFor.LEGAL_REPRESENTATIVE, customerServicesProvider);
        return builder.build();
    }
}
