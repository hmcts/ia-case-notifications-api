package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantInternalReinstateAppealLetterPersonalisation implements LetterNotificationPersonalisation {

    private final String appellantInternalCaseReinstateAppealLetterTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantInternalReinstateAppealLetterPersonalisation(
        @Value("${govnotify.template.reinstateAppeal.appellant.letter}") String appellantInternalCaseReinstateAppealLetterTemplateId,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.appellantInternalCaseReinstateAppealLetterTemplateId = appellantInternalCaseReinstateAppealLetterTemplateId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return appellantInternalCaseReinstateAppealLetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return getAppellantAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_REINSTATE_APPEAL_APPELLANT_LETTER";
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
            .put("decisionMaker", asylumCase.read(AsylumCaseDefinition.REINSTATED_DECISION_MAKER, String.class).orElse(""))
            .put("reinstateAppealDate", asylumCase.read(AsylumCaseDefinition.REINSTATE_APPEAL_DATE, String.class)
                .map(date -> LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy")))
                .orElse(""))
            .put("reinstateReason", asylumCase.read(AsylumCaseDefinition.REINSTATE_APPEAL_REASON, String.class).orElse("No reason given"))
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
