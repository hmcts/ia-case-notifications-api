package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getAppellantAddressAsList;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantInternalCaseSubmitAppealWithExemptionLetterPersonalisation implements LetterNotificationPersonalisation {

    private final String appellantInternalCaseSubmitAppealWithExemptionLetterTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantInternalCaseSubmitAppealWithExemptionLetterPersonalisation(
        @Value("${govnotify.template.appealSubmitted.appellant.letter.inTime.withExemption}") String appellantInternalCaseSubmitAppealWithExemptionLetterTemplateId,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.appellantInternalCaseSubmitAppealWithExemptionLetterTemplateId = appellantInternalCaseSubmitAppealWithExemptionLetterTemplateId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return appellantInternalCaseSubmitAppealWithExemptionLetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return Collections.singleton(getAppellantAddressAsList(asylumCase).stream()
            .map(item -> item.replaceAll("\\s", "")).collect(Collectors.joining("_")));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_SUBMIT_APPEAL_WITH_EXEMPTION_APPELLANT_LETTER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        List<String> appellantAddress = getAppellantAddressAsList(asylumCase);

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""));

        for (int i = 0; i < appellantAddress.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), appellantAddress.get(i));
        }
        return personalizationBuilder.build();
    }
}
