package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL;

@Service
@Slf4j
public class LegalRepresentativeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {


    private final String templateId;
    private final String nonAdaPrefix;
    private final CustomerServicesProvider customerServicesProvider;

    public LegalRepresentativeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisation(
            CustomerServicesProvider customerServicesProvider, @NotNull(message = "templateId cannot be null") @Value("${govnotify.template.completeCaseReviewStatutoryTimeframe24Weeks.legalRep.email}") String templateId,

            @Value("${govnotify.emailPrefix.nonAda}") String nonAdaPrefix) {
        this.templateId = templateId;
        this.nonAdaPrefix = nonAdaPrefix;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return templateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return Stf24WeeksUtil.buildParams(asylumCase, customerServicesProvider, nonAdaPrefix, false);
    }


}
