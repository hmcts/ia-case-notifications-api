package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@Service
public class AppellantInternalRemissionGrantedOutOfTImeLetterPersonalisation implements LetterNotificationPersonalisation {

    private final String appellantInternalRemissionDecisionLetterTemplateId;
    private final int daysAfterRemissionDecision;
    private final CustomerServicesProvider customerServicesProvider;
    private final SystemDateProvider systemDateProvider;

    public AppellantInternalRemissionGrantedOutOfTImeLetterPersonalisation(
        @Value("${govnotify.template.remissionDecision.appellant.rejected.outOfTime.letter}") String appellantInternalRemissionDecisionLetterTemplateId,
        @Value("${appellantDaysToWait.letter.afterSubmitAppeal}") int daysAfterRemissionDecision,
        CustomerServicesProvider customerServicesProvider,
        SystemDateProvider systemDateProvider
    ) {
        this.appellantInternalRemissionDecisionLetterTemplateId = appellantInternalRemissionDecisionLetterTemplateId;
        this.daysAfterRemissionDecision = daysAfterRemissionDecision;
        this.customerServicesProvider = customerServicesProvider;
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public String getTemplateId() {
        return appellantInternalRemissionDecisionLetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return hasBeenSubmittedByAppellantInternalCase(asylumCase) ?
            getAppellantAddressInCountryOrOoc(asylumCase) : getLegalRepAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_REMISSION_GRANTED_OOT_APPELLANT_LETTER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        List<String> address =  getAppellantOrLegalRepAddressLetterPersonalisation(asylumCase);

        final String dueDate = systemDateProvider.dueDate(daysAfterRemissionDecision);

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("tenDaysAfterRemissionDecision", dueDate);

        for (int i = 0; i < address.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), address.get(i));
        }
        return personalizationBuilder.build();
    }
}

