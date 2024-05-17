package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
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

@Service
public class AppellantInternalHomeOfficeDirectedToUploadBundleLetterPersonalisation implements LetterNotificationPersonalisation{
    private final String appellantInternalCaseSubmitAppealWithFeeoutOfTimeLetterTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantInternalHomeOfficeDirectedToUploadBundleLetterPersonalisation(
        @Value("${govnotify.template.requestRespondentEvidenceDirection.appellant.letter}") String appellantInternalHomeOfficeBundleLetter,
        CustomerServicesProvider customerServicesProvider,

    ) {
        this.appellantInternalCaseSubmitAppealWithFeeoutOfTimeLetterTemplateId = appellantInternalHomeOfficeBundleLetter;
        this.customerServicesProvider = customerServicesProvider;

    }

    @Override
    public String getTemplateId() {
        return appellantInternalCaseSubmitAppealWithFeeoutOfTimeLetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return Collections.singleton(getAppellantAddressAsList(asylumCase).stream()
            .map(item -> item.replaceAll("\\s", "")).collect(Collectors.joining("_")));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_HO_UPLOAD_BUNDLE_APPELLANT_LETTER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        List<String> appellantAddress = getAppellantAddressAsList(asylumCase);

        String changeDirectionDueDate = asylumCase.read(AsylumCaseDefinition.SEND_DIRECTION_DATE_DUE, String.class).orElse("");
        if (!changeDirectionDueDate.isBlank()) {
            changeDirectionDueDate = LocalDate.parse(changeDirectionDueDate).format(DateTimeFormatter.ofPattern("d MMM uuuu"));
        }

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("directionDueDate", changeDirectionDueDate);

        for (int i = 0; i < appellantAddress.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), appellantAddress.get(i));
        }
        return personalizationBuilder.build();
    }
}
