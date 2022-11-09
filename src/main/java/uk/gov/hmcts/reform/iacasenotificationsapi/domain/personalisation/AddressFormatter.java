package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;


import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_COMPANY_ADDRESS;

import java.util.Optional;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;

public class AddressFormatter {

    private AddressFormatter(){
    }

    public static String formatCompanyAddress(AsylumCase asylumCase) {
        StringBuilder str = new StringBuilder();

        if (asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class).isPresent()) {

            final String addressLine1 =
                    asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)
                            .flatMap(AddressUk::getAddressLine1).orElse("");

            final String addressLine2 =
                    asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)
                            .flatMap(AddressUk::getAddressLine2).orElse("");

            final String addressLine3 =
                    asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)
                            .flatMap(AddressUk::getAddressLine3).orElse("");

            final String postTown =
                    asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)
                            .flatMap(AddressUk::getPostTown).orElse("");

            final String county =
                    asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)
                            .flatMap(AddressUk::getCounty).orElse("");

            final String postCode =
                    asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)
                            .flatMap(AddressUk::getPostCode).orElse("");

            final String country =
                    asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)
                            .flatMap(AddressUk::getCountry).orElse("");

            if (!Optional.of(addressLine1).get().equals("")) {
                str.append(addressLine1);
                str.append(", ");
            }

            if (!Optional.of(addressLine2).get().isEmpty()) {
                str.append(addressLine2);
                str.append(", ");
            }

            if (!Optional.of(addressLine3).get().isEmpty()) {
                str.append(addressLine3);
                str.append(", ");
            }

            if (!Optional.of(postTown).get().isEmpty()) {
                str.append(postTown);
                str.append(", ");
            }

            if (!Optional.of(county).get().isEmpty()) {
                str.append(county);
                str.append(", ");
            }

            if (!Optional.of(postCode).get().isEmpty()) {
                str.append(postCode);
                str.append(", ");
            }

            if (!Optional.of(country).get().isEmpty()) {
                str.append(country);
            }

        } else {
            return "";
        }

        return str.toString();
    }

}
