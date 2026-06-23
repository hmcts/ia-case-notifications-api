package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

@ExtendWith(MockitoExtension.class)
class AdminOfficerDecidedOrEndedAppealPendingPaymentTest {

    private final String templateId = "someTemplateId";
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;
    private AdminOfficerDecidedOrEndedAppealPendingPayment adminOfficerDecidedOrEndedAppealPendingPayment;

    @BeforeEach
    void setUp() {

        String ctscAdminPendingPaymentEmailAddress = "pendingPayment@example.com";
        adminOfficerDecidedOrEndedAppealPendingPayment =
            new AdminOfficerDecidedOrEndedAppealPendingPayment(
                templateId, ctscAdminPendingPaymentEmailAddress, adminOfficerPersonalisationProvider);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerDecidedOrEndedAppealPendingPayment.getTemplateId());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> adminOfficerDecidedOrEndedAppealPendingPayment.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_APPEAL_PENDING_PAYMENT_ADMIN_OFFICER", adminOfficerDecidedOrEndedAppealPendingPayment.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        adminOfficerDecidedOrEndedAppealPendingPayment.getPersonalisation(asylumCase);
        verify(adminOfficerPersonalisationProvider).getDefaultPersonalisation(asylumCase);
    }
}
