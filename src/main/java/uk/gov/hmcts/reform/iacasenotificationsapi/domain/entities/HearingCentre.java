package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum HearingCentre {

    BELFAST("belfast"),
    BIRMINGHAM("birmingham"),
    BRADFORD("bradford"),
    COVENTRY("coventry"),
    GLASGOW("glasgow"),
    GLASGOW_TRIBUNAL_CENTRE("glasgowTribunalsCentre"),
    HATTON_CROSS("hattonCross"),
    MANCHESTER("manchester"),
    NEWPORT("newport"),
    NORTH_SHIELDS("northShields"),
    NOTTINGHAM("nottingham"),
    TAYLOR_HOUSE("taylorHouse"),
    NEWCASTLE("newcastle"),
    HARMONDSWORTH("harmondsworth"),
    HENDON("hendon"),
    YARLS_WOOD("yarlsWood"),
    BRADFORD_KEIGHLEY("bradfordKeighley"),
    MCC_MINSHULL("mccMinshull"),
    MCC_CROWN_SQUARE("mccCrownSquare"),
    MANCHESTER_MAGS("manchesterMags"),
    NTH_TYNE_MAGS("nthTyneMags"),
    LEEDS_MAGS("leedsMags"),
    ALLOA_SHERRIF("alloaSherrif"),
    ARNHEM_HOUSE("arnhemHouse"),
    CROWN_HOUSE("crownHouse"),
    REMOTE_HEARING("remoteHearing"),
    DECISION_WITHOUT_HEARING("decisionWithoutHearing");

    @JsonValue
    private final String value;

    HearingCentre(String value) {
        this.value = value;
    }

    public static Optional<HearingCentre> from(
        String value
    ) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
