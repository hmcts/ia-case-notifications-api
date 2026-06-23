package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CustomerServicesProviderTest {

    private final String customerServicesTelephone = "555 555";
    private final String standardCustomerServicesEmail = "some.email@example.com";
    private final String internalCaseCustomerServicesEmail = "some.internal.email@example.com";
    private final String appealIaCustomerServicesEmail = "some.appeal.email@example.com";

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private Callback<AsylumCase> callback;

    private CustomerServicesProvider customerServicesProvider;

    @BeforeEach
    public void setUp() {

        customerServicesProvider = new CustomerServicesProvider(
            customerServicesTelephone,
            standardCustomerServicesEmail,
            internalCaseCustomerServicesEmail,
            appealIaCustomerServicesEmail
        );
    }

    @Test
    public void should_return_customer_services_personalisation_with_asylum_case_ada() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));

        Map<String, String> customerServicesPersonalisation =
            customerServicesProvider.getCustomerServicesPersonalisation(asylumCase);

        assertEquals(customerServicesTelephone, customerServicesPersonalisation.get("customerServicesTelephone"));
        assertEquals(internalCaseCustomerServicesEmail, customerServicesPersonalisation.get("customerServicesEmail"));
        assertEquals(appealIaCustomerServicesEmail, customerServicesPersonalisation.get("AppealIAEmail"));
    }

    @Test
    public void should_return_customer_services_personalisation_with_callback_case_ada() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));

        Map<String, String> customerServicesPersonalisation =
            customerServicesProvider.getCustomerServicesPersonalisation(callback);

        assertEquals(customerServicesTelephone, customerServicesPersonalisation.get("customerServicesTelephone"));
        assertEquals(internalCaseCustomerServicesEmail, customerServicesPersonalisation.get("customerServicesEmail"));
        assertEquals(appealIaCustomerServicesEmail, customerServicesPersonalisation.get("AppealIAEmail"));
    }

    @ParameterizedTest
    @CsvSource({"YES,NO", "NO,YES", "NO,NO"})
    public void should_return_customer_services_personalisation_with_asylum_case_non_ada(YesOrNo isAdmin, YesOrNo isAda) {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(isAdmin));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> customerServicesPersonalisation =
            customerServicesProvider.getCustomerServicesPersonalisation(asylumCase);

        assertEquals(customerServicesTelephone, customerServicesPersonalisation.get("customerServicesTelephone"));
        assertEquals(standardCustomerServicesEmail, customerServicesPersonalisation.get("customerServicesEmail"));
        assertEquals(appealIaCustomerServicesEmail, customerServicesPersonalisation.get("AppealIAEmail"));
    }

    @ParameterizedTest
    @CsvSource({"YES,NO", "NO,YES", "NO,NO"})
    public void should_return_customer_services_personalisation_with_callback_case_non_ada(YesOrNo isAdmin, YesOrNo isAda) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(isAdmin));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> customerServicesPersonalisation =
            customerServicesProvider.getCustomerServicesPersonalisation(callback);

        assertEquals(customerServicesTelephone, customerServicesPersonalisation.get("customerServicesTelephone"));
        assertEquals(standardCustomerServicesEmail, customerServicesPersonalisation.get("customerServicesEmail"));
        assertEquals(appealIaCustomerServicesEmail, customerServicesPersonalisation.get("AppealIAEmail"));
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThrows(NullPointerException.class,
            () -> new CustomerServicesProvider(
                null,
                standardCustomerServicesEmail,
                internalCaseCustomerServicesEmail,
                appealIaCustomerServicesEmail));

        assertThrows(NullPointerException.class,
            () -> new CustomerServicesProvider(
                customerServicesTelephone,
                null,
                internalCaseCustomerServicesEmail,
                appealIaCustomerServicesEmail));

        assertThrows(NullPointerException.class,
            () -> new CustomerServicesProvider(
                customerServicesTelephone,
                standardCustomerServicesEmail,
                null,
                appealIaCustomerServicesEmail));

        assertThrows(NullPointerException.class,
            () -> new CustomerServicesProvider(
                customerServicesTelephone,
                standardCustomerServicesEmail,
                internalCaseCustomerServicesEmail,
                null));
    }
}
