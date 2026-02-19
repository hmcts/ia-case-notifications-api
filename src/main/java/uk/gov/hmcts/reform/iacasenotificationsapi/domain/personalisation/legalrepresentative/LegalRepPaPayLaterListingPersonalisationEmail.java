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
public class LegalRepPaPayLaterListingPersonalisationEmail implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepPaPayLaterListingTemplateId;

    public LegalRepPaPayLaterListingPersonalisationEmail(
            @Value("${govnotify.template.listing.paPayLater.email}") String legalRepPaPayLaterListingTemplateId
    ) {
        this.legalRepPaPayLaterListingTemplateId = legalRepPaPayLaterListingTemplateId;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return legalRepPaPayLaterListingTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_PA_PAY_LATER_LISTING_EMAIL";
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
