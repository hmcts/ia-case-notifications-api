package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_CASE_USING_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.IS_REMOTE_HEARING;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.REF_DATA_LISTING_LOCATION_DETAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailHearingLocation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.model.refdata.CourtVenue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HearingDetailsFinderTest {

    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";

    @Mock
    AsylumCase asylumCase;
    @Mock
    BailCase bailCase;
    @Mock
    StringProvider stringProvider;
    private HearingDetailsFinder hearingDetailsFinder;
    private CourtVenue hattonCross;
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String hearingCentreEmailAddress = "hearingCentre@example.com";
    private String hearingCentreName = "some hearing centre name";
    private String bailHearingLocationName = "Glasgow";
    private String hearingCentreAddress = "some hearing centre address";
    private String hearingCentreRefDataAddress = "hearing centre address retrieved from ref data";
    private String hearingDateTime = "2019-08-27T14:25:15.000";
    private String bailHearingDateTime = "2024-01-01T10:29:00.000";
    private String hearingDate = "2019-08-27";
    private String hearingTime = "14:25";

    @BeforeEach
    void setUp() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateTime));

        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString()))
            .thenReturn(Optional.of(hearingCentreAddress));
        when(stringProvider.get("hearingCentreName", hearingCentre.toString()))
            .thenReturn(Optional.of(hearingCentreName));
        when(bailCase.read(LISTING_LOCATION, BailHearingLocation.class))
                .thenReturn(Optional.of(BailHearingLocation.GLASGOW_TRIBUNAL_CENTRE));
        when(stringProvider.get(HEARING_CENTRE_ADDRESS, BailHearingLocation.GLASGOW_TRIBUNAL_CENTRE.getValue()))
                .thenReturn(Optional.of(
                        "IAC Glasgow, " +
                                "1st Floor, " +
                                "The Glasgow Tribunals Centre, " +
                                "Atlantic Quay, " +
                                "20 York Street, " +
                                "Glasgow, " +
                                "G2 8GT"
                ));
        when(bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));

        hattonCross = new CourtVenue("Hatton Cross Tribunal Hearing Centre",
                "Hatton Cross Tribunal Hearing Centre",
                "386417",
                "Open",
                "Y",
                "Y",
                "York House And Wellington House, 2-3 Dukes Green, Feltham, Middlesex",
                "TW14 0LS");

        hearingDetailsFinder = new HearingDetailsFinder(stringProvider);
    }

    @Test
    void should_return_given_hearing_centre_address() {
        assertEquals(hearingCentreAddress, hearingDetailsFinder.getHearingCentreAddress(asylumCase));
    }

    @Test
    void should_return_given_hearing_centre_address_from_ref_data_if_location_ref_data_enabled() {
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE_ADDRESS, String.class))
            .thenReturn(Optional.of(hearingCentreRefDataAddress));

        assertEquals(hearingCentreRefDataAddress, hearingDetailsFinder.getHearingCentreAddress(asylumCase));
    }

    @Test
    void should_throw_exception_when_hearing_centre_address_is_empty() {
        when(stringProvider.get(HEARING_CENTRE_ADDRESS, hearingCentre.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingCentreAddress is not present");
    }

    @Test
    void should_return_given_hearing_centre_name() {
        assertEquals(hearingCentreName, hearingDetailsFinder.getHearingCentreName(asylumCase));
    }

    @Test
    void should_throw_exception_when_hearing_centre_name_is_empty() {
        when(stringProvider.get("hearingCentreName", hearingCentre.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreName(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingCentreName is not present");
    }

    @Test
    void should_return_given_bail_hearing_location_name() {
        when(bailCase.read(LISTING_LOCATION, BailHearingLocation.class)).thenReturn(Optional.of(BailHearingLocation.GLASGOW_TRIBUNAL_CENTRE));
        assertEquals(bailHearingLocationName, hearingDetailsFinder.getBailHearingCentreLocation(bailCase));
    }

    @Test
    void should_throw_exception_when_bail_hearing_location_name_is_empty() {
        when(bailCase.read(LISTING_LOCATION, BailHearingLocation.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getBailHearingCentreLocation(bailCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listingLocation is not present");
    }

    @Test
    void should_return_given_hearing_date_time() {
        assertEquals(hearingDateTime, hearingDetailsFinder.getHearingDateTime(asylumCase));
    }

    @Test
    void should_throw_exception_when_hearing_date_time_is_empty() {
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingDateTime(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingDate is not present");
    }

    @Test
    void should_return_given_bail_hearing_date_time() {
        when(bailCase.read(LISTING_HEARING_DATE, String.class)).thenReturn(Optional.of(bailHearingDateTime));
        assertEquals(bailHearingDateTime, hearingDetailsFinder.getBailHearingDateTime(bailCase));
    }

    @Test
    void should_throw_exception_when_bail_hearing_date_time_is_empty() {
        when(bailCase.read(LISTING_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getBailHearingDateTime(bailCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listHearingDate is not present");
    }

    @Test
    void should_throw_exception_when_hearing_centre_is_empty() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingCentre is not present");
    }

    @Test
    void should_throw_exception_when_hearing_centre_is_empty_in_remote_hearing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingCentre is not present");
    }

    @Test
    void should_throw_exception_when_list_hearing_centre_is_empty() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreLocation(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("listCaseHearingCentre is not present");
    }

    @Test
    void should_return_remote_hearing_when_list_hearing_centre_is_remote() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));

        assertEquals("Remote hearing", hearingDetailsFinder.getHearingCentreLocation(asylumCase));
    }

    @Test
    void should_return_hearing_location_address_when_list_hearing_centre_is_not_remote() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(hearingCentre));

        assertEquals(hearingCentreAddress, hearingDetailsFinder.getHearingCentreLocation(asylumCase));
    }

    @Test
    void should_return_listing_location_address_from_ccd_if_disabled_ref_data_flag() {


        assertEquals("Glasgow\nIAC Glasgow, " +
                        "1st Floor, " +
                        "The Glasgow Tribunals Centre, " +
                        "Atlantic Quay, " +
                        "20 York Street, " +
                        "Glasgow, " +
                        "G2 8GT",
                hearingDetailsFinder.getListingLocationAddressFromRefDataOrCcd(bailCase));
    }

    @Test
    void should_return_remote_address_with_enabled_ref_data_flag() {
        when(bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(IS_REMOTE_HEARING, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));

        assertEquals("Cloud Video Platform (CVP)",
                hearingDetailsFinder.getListingLocationAddressFromRefDataOrCcd(bailCase));
    }

    @Test
    void should_return_listing_location_address_from_ref_data_with_enabled_ref_data_flag() {
        when(bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(REF_DATA_LISTING_LOCATION_DETAIL, CourtVenue.class)).thenReturn(Optional.of(hattonCross));

        assertEquals("Hatton Cross Tribunal Hearing Centre, " +
                        "York House And Wellington House, 2-3 Dukes Green, Feltham, Middlesex, " +
                        "TW14 0LS",
                hearingDetailsFinder.getListingLocationAddressFromRefDataOrCcd(bailCase));
    }
}
