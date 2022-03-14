package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_COMPANY_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.AddressFormatter.formatCompanyAddress;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddressFormatterTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    AppealService appealService;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    EmailAddressFinder emailAddressFinder;

    private final String addressLine1 = "A";
    private final String addressLine2 = "B";
    private final String addressLine3 = "C";
    private final String postTown = "D";
    private final String county = "E";
    private final String postCode = "F";
    private final String country = "G";

    private AddressUk addressUk = new AddressUk(
            addressLine1,
            addressLine2,
            addressLine3,
            postTown,
            county,
            postCode,
            country
    );

    @Test
    void should_return_correctly_formatted_company_address() {

        when(asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)).thenReturn(Optional.of(addressUk));

        assertEquals("A, B, C, D, E, F, G", formatCompanyAddress(asylumCase));
    }

    @Test
    void should_return_correctly_formatted_company_address_for_missing_fields() {

        AddressUk addressUk = new AddressUk(
                "",
                "",
                "",
                "",
                "",
                "",
                ""
        );

        when(asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)).thenReturn(Optional.of(addressUk));

        assertEquals("", formatCompanyAddress(asylumCase));
    }




}