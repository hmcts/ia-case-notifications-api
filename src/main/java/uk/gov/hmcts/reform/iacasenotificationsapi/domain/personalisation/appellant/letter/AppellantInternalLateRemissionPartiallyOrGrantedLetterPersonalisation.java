package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

@Service
public class AppellantInternalLateRemissionPartiallyOrGrantedLetterPersonalisation implements LetterNotificationPersonalisation {

    private final String appellantInternalRemissionDecisionLetterTemplateId;
    private final int daysAfterRemissionDecision;
    private final CustomerServicesProvider customerServicesProvider;
    private final SystemDateProvider systemDateProvider;

    public AppellantInternalLateRemissionPartiallyOrGrantedLetterPersonalisation(
        @Value("${govnotify.template.remissionDecision.appellant.approvedOrPartiallyApproved.letter}") String appellantInternalRemissionDecisionLetterTemplateId,
        @Value("${appellantDaysToWait.afterRemissionDecision}") int daysAfterRemissionDecision,
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
        return getAppellantAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_LATE_REMISSION_PARTIALLY_OR_GRANTED_APPELLANT_LETTER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        YesOrNo isAppellantInUK = asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class).orElse(YesOrNo.NO);

        List<String> appellantAddress = switch (isAppellantInUK) {
            case YES -> getAppellantAddressAsList(asylumCase);
            case NO -> getAppellantAddressAsListOoc(asylumCase);
        };

        final String dueDate = systemDateProvider.dueDate(daysAfterRemissionDecision);

        String refundAmount = asylumCase.read(AsylumCaseDefinition.AMOUNT_REMITTED, String.class).orElse("");

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("daysAfterRemissionDecision", dueDate)
            .put("refundAmount", convertAsylumCaseFeeValue(refundAmount));

        for (int i = 0; i < appellantAddress.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), appellantAddress.get(i));
        }
        return personalizationBuilder.build();
    }
}

