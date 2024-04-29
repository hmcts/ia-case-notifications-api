package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.AsylumCaseUtils.isIntegrated;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;

import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailHearingLocation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;

@Service
public class HearingDetailsFinder {

    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";

    private final StringProvider stringProvider;

    public HearingDetailsFinder(StringProvider stringProvider) {
        this.stringProvider = stringProvider;
    }

    public String getHearingCentreAddress(AsylumCase asylumCase) {
        final HearingCentre listCaseHearingCentre =
                getHearingCentre(asylumCase);

        Optional<String> refDataAddress = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE_ADDRESS, String.class);

        if (isIntegrated(asylumCase) && isCaseUsingLocationRefData(asylumCase) && refDataAddress.isPresent())  {
            return refDataAddress.get();
        }
        return stringProvider.get(HEARING_CENTRE_ADDRESS, listCaseHearingCentre.toString())
                .orElseThrow(() -> new IllegalStateException("hearingCentreAddress is not present"));
    }

    public String getHearingCentreName(AsylumCase asylumCase) {

        final HearingCentre hearingCentre =
            asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        return stringProvider.get("hearingCentreName", hearingCentre.toString())
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentreName is not present"));
    }

    public String getHearingDateTime(AsylumCase asylumCase) {
        return asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingDate is not present"));
    }

    public String getBailHearingDateTime(BailCase bailCase) {
        return bailCase
            .read(LISTING_HEARING_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException("listHearingDate is not present"));
    }

    private HearingCentre getHearingCentre(AsylumCase asylumCase) {
        HearingCentre hearingCentre =
            asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        if (hearingCentre == HearingCentre.REMOTE_HEARING) {
            return
                asylumCase
                    .read(AsylumCaseDefinition.HEARING_CENTRE, HearingCentre.class)
                    .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));
        }
        return hearingCentre;
    }

    public String getHearingCentreLocation(AsylumCase asylumCase) {
        HearingCentre hearingCentre =
                asylumCase
                        .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                        .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        if (hearingCentre == HearingCentre.REMOTE_HEARING) {
            return "Remote hearing";
        } else {
            return isCaseUsingLocationRefData(asylumCase) && isIntegrated(asylumCase)
                ? asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE_ADDRESS, String.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentreAddress is not present"))
                : getHearingCentreAddress(asylumCase);
        }
    }

    public String getBailHearingCentreLocation(BailCase bailCase) {
        BailHearingLocation hearingLocation =
            bailCase
                .read(LISTING_LOCATION, BailHearingLocation.class)
                .orElseThrow(() -> new IllegalStateException("listingLocation is not present"));

        return hearingLocation.getDescription();
    }

    public String getBailHearingCentreAddress(BailCase bailCase) {
        final BailHearingLocation listCaseHearingCentre =
                bailCase
                        .read(LISTING_LOCATION, BailHearingLocation.class)
                        .orElseThrow(() -> new IllegalStateException("listingLocation is not present"));

        final String hearingCentreAddress =
                stringProvider
                        .get(HEARING_CENTRE_ADDRESS, listCaseHearingCentre.getValue())
                        .orElseThrow(() -> new IllegalStateException("hearingCentreAddress is not present"));

        boolean isRemote = Stream.of("remoteHearing", "decisionWithoutHearing").anyMatch(listCaseHearingCentre.getValue()::equalsIgnoreCase);
        return listCaseHearingCentre.getDescription() + (isRemote ? "" : "\n" + hearingCentreAddress);
    }

    private boolean isCaseUsingLocationRefData(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseDefinition.IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)
            .map(yesOrNo -> yesOrNo.equals(YesOrNo.YES))
            .orElse(false);
    }
}
