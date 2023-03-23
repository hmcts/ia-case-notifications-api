package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;


@ExtendWith(MockitoExtension.class)
public class CustomerServicesProviderTest {

    private String customerServicesTelephone = "555 555";
    private String standardCustomerServicesEmail = "some.email@example.com";
    private String internalCaseCustomerServicesEmail = "some.internal.email@example.com";

    private CustomerServicesProvider customerServicesProvider;

    @BeforeEach
    public void setUp() {

        customerServicesProvider = new CustomerServicesProvider(
            customerServicesTelephone,
            standardCustomerServicesEmail,
            internalCaseCustomerServicesEmail
        );
    }

    @Test
    public void should_return_customer_services_personalisation() {

        Map<String, String> customerServicesPersonalisation =
            customerServicesProvider.getCustomerServicesPersonalisation();

        assertThat(customerServicesPersonalisation.get("customerServicesTelephone"))
            .isEqualTo(customerServicesTelephone);

        assertThat(customerServicesPersonalisation.get("customerServicesEmail")).isEqualTo(standardCustomerServicesEmail);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new CustomerServicesProvider(
            null,
            standardCustomerServicesEmail,
            internalCaseCustomerServicesEmail))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new CustomerServicesProvider(
            customerServicesTelephone,
            null,
            internalCaseCustomerServicesEmail))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new CustomerServicesProvider(
            customerServicesTelephone,
            standardCustomerServicesEmail,
            null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_return_customer_services_telephone_number_and_email() {

        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());

        assertEquals(standardCustomerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_set_correct_email_based_on_asylum_case(YesOrNo isAdmin) {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(isAdmin));

        customerServicesProvider.setCorrectEmail(asylumCase);

        if (isAdmin.equals(YES)) {
            assertEquals(internalCaseCustomerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        } else {
            assertEquals(standardCustomerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        }

    }
}
