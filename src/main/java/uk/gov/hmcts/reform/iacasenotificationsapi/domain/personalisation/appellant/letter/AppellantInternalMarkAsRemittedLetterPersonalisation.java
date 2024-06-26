package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getAppellantAddressAsList;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getAppellantAddressAsListOoc;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.SourceOfRemittal;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantInternalMarkAsRemittedLetterPersonalisation  implements LetterNotificationPersonalisation {
    private final String appellantInternalMarkAsRemittedLetterTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantInternalMarkAsRemittedLetterPersonalisation(
        @Value("${govnotify.template.markAppealAsRemitted.appellant.letter}") String appellantInternalMarkAsRemittedLetterTemplateId,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.appellantInternalMarkAsRemittedLetterTemplateId = appellantInternalMarkAsRemittedLetterTemplateId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return appellantInternalMarkAsRemittedLetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        YesOrNo isAppellantInUK = asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class).orElse(YesOrNo.NO);

        return switch (isAppellantInUK) {
            case YES ->
                Collections.singleton(getAppellantAddressAsList(asylumCase).stream()
                    .map(item -> item.replaceAll("\\s", "")).collect(Collectors.joining("_")));
            case NO -> Collections.singleton(getAppellantAddressAsListOoc(asylumCase).stream()
                .map(item -> item.replaceAll("\\s", "")).collect(Collectors.joining("_")));
        };
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_APPEAL_REMITTED_APPELLANT_LETTER";
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

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("remittalSource", asylumCase.read(AsylumCaseDefinition.SOURCE_OF_REMITTAL, SourceOfRemittal.class)
                .orElseThrow(() -> new IllegalStateException("sourceOfRemittal is not present"))
                .getValue());

        for (int i = 0; i < appellantAddress.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), appellantAddress.get(i));
        }
        return personalizationBuilder.build();
    }
}
