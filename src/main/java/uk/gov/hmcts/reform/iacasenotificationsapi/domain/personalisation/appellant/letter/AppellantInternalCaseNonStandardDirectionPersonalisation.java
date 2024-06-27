package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

@Service
public class AppellantInternalCaseNonStandardDirectionPersonalisation implements LetterNotificationPersonalisation {
    private final String appellantInternalCaseNonStandardDirectionLetterTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final DirectionFinder directionFinder;

    public AppellantInternalCaseNonStandardDirectionPersonalisation(
            @Value("${govnotify.template.nonStandardDirectionInternal.appellant.letter}") String appellantInternalCaseNonStandardDirectionLetterTemplateId,
            CustomerServicesProvider customerServicesProvider,
            DirectionFinder directionFinder
    ) {
        this.appellantInternalCaseNonStandardDirectionLetterTemplateId = appellantInternalCaseNonStandardDirectionLetterTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.directionFinder = directionFinder;
    }

    @Override
    public String getTemplateId() {
        return appellantInternalCaseNonStandardDirectionLetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return switch (isAppellantInUK(asylumCase)) {
            case YES ->
                    Collections.singleton(getAppellantAddressAsList(asylumCase).stream()
                            .map(item -> item.replaceAll("\\s", "")).collect(Collectors.joining("_")));
            case NO -> Collections.singleton(getAppellantAddressAsListOoc(asylumCase).stream()
                    .map(item -> item.replaceAll("\\s", "")).collect(Collectors.joining("_")));
        };
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_NON_STANDARD_DIRECTION_APPELLANT_LETTER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        List<String> appellantAddress = switch (isAppellantInUK(asylumCase)) {
            case YES -> getAppellantAddressAsList(asylumCase);
            case NO -> getAppellantAddressAsListOoc(asylumCase);
        };

        final Direction direction =
                directionFinder
                        .findFirst(asylumCase, DirectionTag.NONE)
                        .orElseThrow(() -> new IllegalStateException("direction '" + DirectionTag.NONE + "' is not present"));

        final String dueDate =
                LocalDate
                        .parse(direction.getDateDue())
                        .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("directionExplanation", direction.getExplanation())
                .put("directionDueDate", dueDate);

        for (int i = 0; i < appellantAddress.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), appellantAddress.get(i));
        }
        return personalizationBuilder.build();
    }
}
