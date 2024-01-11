package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BailHearingLocationTest {

    @Test
    void has_correct_bail_hearing_location() {
        assertThat(BailHearingLocation.from("birminghamPrioryCourts").get()).isEqualByComparingTo(BailHearingLocation.BIRMINGHAM);
        assertThat(BailHearingLocation.from("bradford").get()).isEqualByComparingTo(BailHearingLocation.BRADFORD);
        assertThat(BailHearingLocation.from("fieldHouse").get()).isEqualByComparingTo(BailHearingLocation.FIELD_HOUSE);
        assertThat(BailHearingLocation.from("glasgowTribunalsCentre").get()).isEqualByComparingTo(BailHearingLocation.GLASGOW);
        assertThat(BailHearingLocation.from("harmondsworth").get()).isEqualByComparingTo(BailHearingLocation.HARMONDSWORTH);
        assertThat(BailHearingLocation.from("manchesterPiccadilly").get()).isEqualByComparingTo(BailHearingLocation.MANCHESTER);
        assertThat(BailHearingLocation.from("newcastleCfctc").get()).isEqualByComparingTo(BailHearingLocation.NEWCASTLE);
        assertThat(BailHearingLocation.from("newportColumbusHouse").get()).isEqualByComparingTo(BailHearingLocation.NEWPORT);
        assertThat(BailHearingLocation.from("northShieldsKingsCourt").get()).isEqualByComparingTo(BailHearingLocation.NORTH_SHIELDS);
        assertThat(BailHearingLocation.from("nottinghamCivilJusticeCentre").get()).isEqualByComparingTo(BailHearingLocation.NOTTINGHAM);
        assertThat(BailHearingLocation.from("stokeBennetHouse").get()).isEqualByComparingTo(BailHearingLocation.STOKE);
        assertThat(BailHearingLocation.from("taylorHouse").get()).isEqualByComparingTo(BailHearingLocation.TAYLOR_HOUSE);
        assertThat(BailHearingLocation.from("yarlsWood").get()).isEqualByComparingTo(BailHearingLocation.YARLSWOOD);
        assertThat(BailHearingLocation.from("videoHearing").get()).isEqualByComparingTo(BailHearingLocation.VIDEO_HEARING);
        assertThat(BailHearingLocation.from("telephoneHearing").get()).isEqualByComparingTo(BailHearingLocation.TELEPHONE_HEARING);
    }

    @Test
    void has_correct_bail_hearing_location_description() {
        assertEquals("Birmingham (Priory Courts)", BailHearingLocation.BIRMINGHAM.getDescription());
        assertEquals("Bradford", BailHearingLocation.BRADFORD.getDescription());
        assertEquals("Field House", BailHearingLocation.FIELD_HOUSE.getDescription());
        assertEquals("Glasgow (Tribunals Centre)", BailHearingLocation.GLASGOW.getDescription());
        assertEquals("Harmondsworth", BailHearingLocation.HARMONDSWORTH.getDescription());
        assertEquals("Manchester – Piccadilly", BailHearingLocation.MANCHESTER.getDescription());
        assertEquals("Newcastle (CFCTC)", BailHearingLocation.NEWCASTLE.getDescription());
        assertEquals("Newport - Columbus House", BailHearingLocation.NEWPORT.getDescription());
        assertEquals("North Shields - Kings Court", BailHearingLocation.NORTH_SHIELDS.getDescription());
        assertEquals("Nottingham - Civil Justice Centre", BailHearingLocation.NOTTINGHAM.getDescription());
        assertEquals("Stoke - Bennet House", BailHearingLocation.STOKE.getDescription());
        assertEquals("Taylor House", BailHearingLocation.TAYLOR_HOUSE.getDescription());
        assertEquals("Yarl's Wood", BailHearingLocation.YARLSWOOD.getDescription());
        assertEquals("Video hearing", BailHearingLocation.VIDEO_HEARING.getDescription());
        assertEquals("Telephone hearing", BailHearingLocation.TELEPHONE_HEARING.getDescription());
    }

    @Test
    void returns_optional_for_unknown_bail_hearing_location() {
        assertThat(BailHearingLocation.from("unknown")).isEmpty();
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(15, BailHearingLocation.values().length);
    }
}
