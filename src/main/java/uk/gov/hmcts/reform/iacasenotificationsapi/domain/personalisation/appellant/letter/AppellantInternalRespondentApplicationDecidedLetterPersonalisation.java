package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getAppellantAddressAsList;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getAppellantAddressAsListOoc;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplicationTypes;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.WhatHappensNextContentUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@Service
public class AppellantInternalRespondentApplicationDecidedLetterPersonalisation implements LetterNotificationPersonalisation {
    private final String appellantInternalRemissionDecisionLetterTemplateId;
    private final int daysAfterApplicationDecisionInCountry;
    private final int daysAfterApplicationDecisionOoc;
    private final CustomerServicesProvider customerServicesProvider;
    private final SystemDateProvider systemDateProvider;
    private final MakeAnApplicationService makeAnApplicationService;

    public AppellantInternalRespondentApplicationDecidedLetterPersonalisation(
            @Value("${govnotify.template.decideARespondentApplication.appellant.letter}") String appellantInternalRemissionDecisionLetterTemplateId,
            @Value("${appellantDaysToWait.letter.afterRespondentApplicationDecided.inCountry}") int daysAfterApplicationDecisionInCountry,
            @Value("${appellantDaysToWait.letter.afterRespondentApplicationDecided.outOfCountry}") int daysAfterApplicationDecisionOoc,
            CustomerServicesProvider customerServicesProvider,
            SystemDateProvider systemDateProvider,
            MakeAnApplicationService makeAnApplicationService
    ) {
        this.appellantInternalRemissionDecisionLetterTemplateId = appellantInternalRemissionDecisionLetterTemplateId;
        this.daysAfterApplicationDecisionInCountry = daysAfterApplicationDecisionInCountry;
        this.daysAfterApplicationDecisionOoc = daysAfterApplicationDecisionOoc;
        this.customerServicesProvider = customerServicesProvider;
        this.systemDateProvider = systemDateProvider;
        this.makeAnApplicationService = makeAnApplicationService;
    }

    @Override
    public String getTemplateId() {
        return appellantInternalRemissionDecisionLetterTemplateId;
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
        return caseId + "_INTERNAL_RESPONDENT_APPLICATION_DECIDED_APPELLANT_LETTER";
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

        Optional<MakeAnApplication> makeAnApplicationOptional = makeAnApplicationService.getMakeAnApplication(asylumCase, true);
        MakeAnApplication application = makeAnApplicationOptional.orElseThrow(() -> new RequiredFieldMissingException("Application is missing."));

        final String dueDate = isAppellantInUK.equals(YesOrNo.YES)
                ? systemDateProvider.dueDate(daysAfterApplicationDecisionInCountry)
                : systemDateProvider.dueDate(daysAfterApplicationDecisionOoc);

        String nextSteps = WhatHappensNextContentUtils.getWhatHappensNextContent(
                MakeAnApplicationTypes.from(application.getType()).orElseThrow(() -> new IllegalStateException("Invalid MakeAnApplicationType")),
                false, application.getDecision(), dueDate);
        if (nextSteps.equals("Unknown")) {
            throw new IllegalStateException("Invalid MakeAnApplicationType: Couldn't find next steps.");
        }

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("decision", application.getDecision().equals("Granted") ? "grant" : "refuse")
            .put("applicationType", application.getType())
            .put("applicationReason", application.getDecisionReason())
            .put("nextStep", nextSteps);

        for (int i = 0; i < appellantAddress.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), appellantAddress.get(i));
        }
        return personalizationBuilder.build();
    }
}
