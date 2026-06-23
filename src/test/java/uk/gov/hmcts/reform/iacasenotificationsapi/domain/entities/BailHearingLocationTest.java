package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BailHearingLocationTest {

    @Test
    void has_correct_bail_hearing_location() {
        assertEquals(BailHearingLocation.MANCHESTER, BailHearingLocation.from("manchester").get());
        assertEquals(BailHearingLocation.BIRMINGHAM, BailHearingLocation.from("birmingham").get());
        assertEquals(BailHearingLocation.BRADFORD, BailHearingLocation.from("bradford").get());
        assertEquals(BailHearingLocation.COVENTRY, BailHearingLocation.from("coventry").get());
        assertEquals(BailHearingLocation.GLASGOW_TRIBUNAL_CENTRE, BailHearingLocation.from("glasgowTribunalsCentre").get());
        assertEquals(BailHearingLocation.HARMONDSWORTH, BailHearingLocation.from("harmondsworth").get());
        assertEquals(BailHearingLocation.HATTON_CROSS, BailHearingLocation.from("hattonCross").get());
        assertEquals(BailHearingLocation.HENDON, BailHearingLocation.from("hendon").get());
        assertEquals(BailHearingLocation.NEWCASTLE, BailHearingLocation.from("newcastle").get());
        assertEquals(BailHearingLocation.NEWPORT, BailHearingLocation.from("newport").get());
        assertEquals(BailHearingLocation.TAYLOR_HOUSE, BailHearingLocation.from("taylorHouse").get());
        assertEquals(BailHearingLocation.YARLS_WOOD, BailHearingLocation.from("yarlsWood").get());
        assertEquals(BailHearingLocation.BRADFORD_KEIGHLEY, BailHearingLocation.from("bradfordKeighley").get());
        assertEquals(BailHearingLocation.MCC_MINSHULL, BailHearingLocation.from("mccMinshull").get());
        assertEquals(BailHearingLocation.MCC_CROWN_SQUARE, BailHearingLocation.from("mccCrownSquare").get());
        assertEquals(BailHearingLocation.NOTTINGHAM, BailHearingLocation.from("nottingham").get());
        assertEquals(BailHearingLocation.MANCHESTER_MAGS, BailHearingLocation.from("manchesterMags").get());
        assertEquals(BailHearingLocation.NTH_TYNE_MAGS, BailHearingLocation.from("nthTyneMags").get());
        assertEquals(BailHearingLocation.LEEDS_MAGS, BailHearingLocation.from("leedsMags").get());
        assertEquals(BailHearingLocation.BELFAST, BailHearingLocation.from("belfast").get());
        assertEquals(BailHearingLocation.ALLOA_SHERRIF, BailHearingLocation.from("alloaSherrif").get());
        assertEquals(BailHearingLocation.REMOTE_HEARING, BailHearingLocation.from("remoteHearing").get());
        assertEquals(BailHearingLocation.DECISION_WITHOUT_HEARING, BailHearingLocation.from("decisionWithoutHearing").get());
    }

    @Test
    void has_correct_bail_hearing_location_description() {
        assertEquals("Manchester", BailHearingLocation.MANCHESTER.getDescription());
        assertEquals("Birmingham", BailHearingLocation.BIRMINGHAM.getDescription());
        assertEquals("Bradford", BailHearingLocation.BRADFORD.getDescription());
        assertEquals("Coventry Magistrates Court", BailHearingLocation.COVENTRY.getDescription());
        assertEquals("Glasgow", BailHearingLocation.GLASGOW_TRIBUNAL_CENTRE.getDescription());
        assertEquals("Harmondsworth", BailHearingLocation.HARMONDSWORTH.getDescription());
        assertEquals("Hatton Cross", BailHearingLocation.HATTON_CROSS.getDescription());
        assertEquals("Hendon", BailHearingLocation.HENDON.getDescription());
        assertEquals("Newcastle Civil & Family Courts and Tribunals Centre", BailHearingLocation.NEWCASTLE.getDescription());
        assertEquals("Newport", BailHearingLocation.NEWPORT.getDescription());
        assertEquals("Taylor House", BailHearingLocation.TAYLOR_HOUSE.getDescription());
        assertEquals("Yarl's Wood", BailHearingLocation.YARLS_WOOD.getDescription());
        assertEquals("Bradford & Keighley", BailHearingLocation.BRADFORD_KEIGHLEY.getDescription());
        assertEquals("MCC Minshull st", BailHearingLocation.MCC_MINSHULL.getDescription());
        assertEquals("MCC Crown Square", BailHearingLocation.MCC_CROWN_SQUARE.getDescription());
        assertEquals("Nottingham Justice Centre", BailHearingLocation.NOTTINGHAM.getDescription());
        assertEquals("Manchester Mags", BailHearingLocation.MANCHESTER_MAGS.getDescription());
        assertEquals("NTH Tyne Mags", BailHearingLocation.NTH_TYNE_MAGS.getDescription());
        assertEquals("Leeds Mags", BailHearingLocation.LEEDS_MAGS.getDescription());
        assertEquals("Belfast", BailHearingLocation.BELFAST.getDescription());
        assertEquals("Alloa Sherrif Court", BailHearingLocation.ALLOA_SHERRIF.getDescription());
        assertEquals("Remote", BailHearingLocation.REMOTE_HEARING.getDescription());
        assertEquals("Decision without hearing", BailHearingLocation.DECISION_WITHOUT_HEARING.getDescription());
    }

    @Test
    void returns_optional_for_unknown_bail_hearing_location() {
        assertTrue(BailHearingLocation.from("unknown").isEmpty());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(23, BailHearingLocation.values().length);
    }
}
