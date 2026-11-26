package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.buildAddressForIccLetter;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getAppellantAddressInCountryOrOoc;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLegalRepAddressInCountryOrOoc;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.hasBeenSubmittedByAppellantInternalCase;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplicantType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantInternalUpdateFtpaDecisionLetterPersonalisation implements LetterNotificationPersonalisation {

    private static final String FTPA_DISPOSED_REASON = "Correct decision error under rule 31";

    private final String appellantInternalUpdateFtpaDecisionLetterTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantInternalUpdateFtpaDecisionLetterPersonalisation(
        @Value("${govnotify.template.updateFtpaDecision.appellant.letter}") String appellantInternalUpdateFtpaDecisionLetterTemplateId,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.appellantInternalUpdateFtpaDecisionLetterTemplateId = appellantInternalUpdateFtpaDecisionLetterTemplateId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return appellantInternalUpdateFtpaDecisionLetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return hasBeenSubmittedByAppellantInternalCase(asylumCase)
            ? getAppellantAddressInCountryOrOoc(asylumCase)
            : getLegalRepAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_UPDATE_FTPA_DECISION_LETTER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        ImmutableMap.Builder<String, String> builder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase))
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("applicant", isAppellantApplicant(asylumCase) ? "your" : "the Home Office's")
            .put("ftpaDisposedReason", FTPA_DISPOSED_REASON);

        buildAddressForIccLetter(asylumCase, builder);

        return builder.build();
    }

    private boolean isAppellantApplicant(AsylumCase asylumCase) {
        return asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)
            .orElseThrow(() -> new IllegalStateException("ftpaApplicantType is not present"))
            == ApplicantType.APPELLANT;
    }
}
