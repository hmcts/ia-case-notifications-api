package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.AsylumCaseUtils.isIntegrated;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.IS_REMOTE_HEARING;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.REF_DATA_LISTING_LOCATION_DETAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailHearingLocation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.model.refdata.CourtVenue;

@Service
public class HearingDetailsFinder {

    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";
    private static final String APPEALS_LOCATION_REFERENCE_DATA = "appeals-location-reference-data";
    private static final String REMOTE_HEARING_LOCATION = "Cloud Video Platform (CVP)";

    private final StringProvider stringProvider;
    private final FeatureToggler featureToggler;

    public HearingDetailsFinder(StringProvider stringProvider, FeatureToggler featureToggler) {
        this.stringProvider = stringProvider;
        this.featureToggler = featureToggler;
    }

    public String getHearingCentreAddress(AsylumCase asylumCase) {
        final HearingCentre listCaseHearingCentre =
                getHearingCentre(asylumCase);

        Optional<String> refDataAddress = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE_ADDRESS, String.class);

        if (isIntegrated(asylumCase) && isAppealsLocationReferenceDataEnabled() && refDataAddress.isPresent())  {
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
            return isAppealsLocationReferenceDataEnabled() && isIntegrated(asylumCase)
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

    private boolean isAppealsLocationReferenceDataEnabled() {
        return featureToggler.getValue(APPEALS_LOCATION_REFERENCE_DATA, false);
    }

    public String getListingLocationAddressFromRefDataOrCcd(BailCase bailCase) {
        String hearingLocationAddress = getBailHearingCentreAddress(bailCase);
        YesOrNo isBailsLocationRefDataEnabled = bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YesOrNo.class)
                .orElse(NO);

        if (isBailsLocationRefDataEnabled == YES) {
            if (bailCase.read(IS_REMOTE_HEARING, YesOrNo.class).orElse(NO) == YES) {
                return REMOTE_HEARING_LOCATION;
            } else {
                Optional<CourtVenue> refDataListingLocationDetail = bailCase.read(REF_DATA_LISTING_LOCATION_DETAIL, CourtVenue.class);

                if (refDataListingLocationDetail.isPresent()) {
                    hearingLocationAddress = (refDataListingLocationDetail.get().getCourtName() + ", " +
                            refDataListingLocationDetail.get().getCourtAddress() + ", " +
                            refDataListingLocationDetail.get().getPostcode());

                }
            }
        }
        return hearingLocationAddress;
    }

}
