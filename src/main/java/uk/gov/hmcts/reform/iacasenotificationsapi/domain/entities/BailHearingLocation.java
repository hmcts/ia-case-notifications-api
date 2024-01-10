package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum BailHearingLocation {

    BIRMINGHAM("birminghamPrioryCourts", "Birmingham (Priory Courts)"),
    BRADFORD("bradford", "Bradford"),
    FIELD_HOUSE("fieldHouse", "Field House"),
    GLASGOW("glasgowTribunalsCentre", "Glasgow (Tribunals Centre)"),
    HARMONDSWORTH("harmondsworth", "Harmondsworth"),
    MANCHESTER("manchesterPiccadilly", "Manchester – Piccadilly"),
    NEWCASTLE("newcastleCfctc", "Newcastle (CFCTC)"),
    NEWPORT("newportColumbusHouse", "Newport - Columbus House"),
    NORTH_SHIELDS("northShieldsKingsCourt", "North Shields - Kings Court"),
    NOTTINGHAM("nottinghamCivilJusticeCentre", "Nottingham - Civil Justice Centre"),
    STOKE("stokeBennetHouse", "Stoke - Bennet House"),
    TAYLOR_HOUSE("taylorHouse", "Taylor House"),
    YARLSWOOD("yarlsWood", "Yarl's Wood"),
    VIDEO_HEARING("videoHearing", "Video hearing"),
    TELEPHONE_HEARING("telephoneHearing", "Telephone hearing");

    @JsonValue
    private final String value;

    private String description;

    BailHearingLocation(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static Optional<BailHearingLocation> from(
        String value
    ) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return value + ": " + description;
    }
}
