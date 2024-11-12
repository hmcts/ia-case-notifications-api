package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NationalityGovUk {
    AR("Argentina"),
    AM("Armenia"),
    AU("Australia"),
    AT("Austria"),
    AZ("Azerbaijan"),
    BH("Bahrain"),
    BY("Belarus"),
    BE("Belgium"),
    BJ("Benin"),
    BO("Bolivia"),
    BA("Bosnia and Herzegovina"),
    BW("Botswana"),
    BR("Brazil"),
    BN("Brunei Darussalam"),
    BG("Bulgaria"),
    CA("Canada"),
    CV("Cape Verde"),
    CF("Central African Republic"),
    CN("China"),
    CO("Colombia"),
    KM("Comoros"),
    HR("Croatia"),
    CU("Cuba"),
    CY("Cyprus"),
    CZ("Czech Republic"),
    DK("Denmark"),
    DO("Dominican Republic"),
    EC("Ecuador"),
    EG("Egypt"),
    EE("Estonia"),
    SZ("Eswatini"),
    ET("Ethiopia"),
    FJ("Fiji"),
    FI("Finland"),
    FR("France"),
    GA("Gabon"),
    GE("Georgia"),
    DE("Germany"),
    GH("Ghana"),
    GR("Greece"),
    HK("Hong Kong"),
    HU("Hungary"),
    IS("Iceland"),
    IN("India"),
    ID("Indonesia"),
    IR("Iran, Islamic Republic of"),
    IQ("Iraq"),
    IE("Ireland"),
    IL("Israel"),
    IT("Italy"),
    JP("Japan"),
    JO("Jordan"),
    KZ("Kazakhstan"),
    KE("Kenya"),
    KR("Korea, Republic of"),
    KW("Kuwait"),
    LA("Laos"),
    LV("Latvia"),
    LB("Lebanon"),
    LS("Lesotho"),
    LI("Liechtenstein"),
    LT("Lithuania"),
    LU("Luxembourg"),
    MO("Macao"),
    MG("Madagascar"),
    MW("Malawi"),
    MY("Malaysia"),
    ML("Mali"),
    MT("Malta"),
    MU("Mauritius"),
    MX("Mexico"),
    MD("Moldova"),
    MN("Mongolia"),
    MA("Morocco"),
    MZ("Mozambique"),
    MM("Myanmar"),
    NA("Namibia"),
    NP("Nepal"),
    NL("Netherlands"),
    NZ("New Zealand"),
    NI("Nicaragua"),
    NG("Nigeria"),
    NO("Norway"),
    OM("Oman"),
    PK("Pakistan"),
    PY("Paraguay"),
    PE("Peru"),
    PH("Philippines"),
    PL("Poland"),
    PT("Portugal"),
    QA("Qatar"),
    RO("Romania"),
    RU("Russian Federation"),
    RW("Rwanda"),
    LC("Saint Lucia"),
    SA("Saudi Arabia"),
    SN("Senegal"),
    SG("Singapore"),
    SK("Slovakia"),
    SI("Slovenia"),
    ZA("South Africa"),
    ES("Spain"),
    SD("Sudan"),
    SE("Sweden"),
    CH("Switzerland"),
    SY("Syrian Arab Republic (Syria)"),
    TJ("Tajikistan"),
    TZ("Tanzania *, United Republic of"),
    TH("Thailand"),
    TR("Turkey"),
    TM("Turkmenistan"),
    UG("Uganda"),
    UA("Ukraine"),
    AE("United Arab Emirates"),
    GB("United Kingdom"),
    US("United States of America"),
    UY("Uruguay"),
    UZ("Uzbekistan"),
    VU("Vanuatu"),
    VN("Viet Nam"),
    YE("Yemen"),
    ZW("Zimbabwe"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    NationalityGovUk(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
