package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;

@Service
public class EditCaseListingPersonalisationFactory extends AbstractPersonalisationFactory {

    private final StringProvider stringProvider;
    private final DateTimeExtractor dateTimeExtractor;

    public EditCaseListingPersonalisationFactory(
            StringProvider stringProvider,
            DateTimeExtractor dateTimeExtractor
    ) {
        this.stringProvider = stringProvider;
        this.dateTimeExtractor = dateTimeExtractor;
    }

    public Map<String, String> create(
            AsylumCase asylumCase,
            AsylumCase caseBeforeDetails
    ) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final HearingCentre listedHearingCentre =
                asylumCase
                        .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                        .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        final HearingCentre oldListedHearingCentre =
                caseBeforeDetails
                    .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                    .orElseThrow(() -> new IllegalStateException("oldCaseHearingCentre is not present"));

        final String hearingCentreAddress =
                stringProvider
                        .get("hearingCentreAddress", listedHearingCentre.toString())
                        .orElseThrow(() -> new IllegalStateException("hearingCentreAddress is not present"));

        final String oldHearingCentreAddress =
                stringProvider
                    .get("hearingCentreAddress", oldListedHearingCentre.toString())
                    .orElseThrow(() -> new IllegalStateException("oldHearingCentreAddress is not present"));

        final String hearingDateTime =
                asylumCase
                        .read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)
                        .orElseThrow(() -> new IllegalStateException("hearingDateTime is not present"));

        final String oldHearingDateTime =
                caseBeforeDetails
                    .read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)
                    .orElseThrow(() -> new IllegalStateException("oldHearingDateTime is not present"));

        Map<String, String> immutableMap =  super.create(asylumCase);
        return ImmutableMap
                .<String, String>builder()
                .putAll(immutableMap)
                .put("oldHearingCentre", oldHearingCentreAddress)
                .put("oldHearingDate", dateTimeExtractor.extractHearingDate(oldHearingDateTime))
                .put("oldHearingCentreAddress", oldHearingCentreAddress)
                .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDateTime))
                .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDateTime))
                .put("hearingCentreAddress", hearingCentreAddress)
                .put("Hearing Requirement Vulnerabilities", asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class)
                        .orElse("No special adjustments are being made to accommodate vulnerabilities"))
                .put("Hearing Requirement Multimedia", asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class)
                        .orElse("No multimedia equipment is being provided"))
                .put("Hearing Requirement Single Sex Court", asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class)
                        .orElse("The court will not be single sex"))
                .put("Hearing Requirement In Camera Court", asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class)
                        .orElse("The hearing will be held in public court"))
                .put("Hearing Requirement Other", asylumCase.read(AsylumCaseDefinition.LIST_CASE_REQUIREMENTS_OTHER, String.class)
                        .orElse("No other adjustments are being made"))
                .build();

    }
}
