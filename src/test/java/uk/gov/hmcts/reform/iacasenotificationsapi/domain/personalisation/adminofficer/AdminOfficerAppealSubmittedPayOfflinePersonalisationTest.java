package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

@ExtendWith(MockitoExtension.class)
class AdminOfficerAppealSubmittedPayOfflinePersonalisationTest {

    private final String templateId = "someTemplateId";
    private final String remissionTemplateId = "someRemissionTemplateId";
    private final String changeToHearingRequirementsAdminOfficerEmailAddress = "fees-ao@example.com";
    @Mock
    AsylumCase asylumCase;
    @Mock
    AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;
    private AdminOfficerAppealSubmittedPayOfflinePersonalisation adminOfficerAppealSubmittedPayOfflinePersonalisation;

    @BeforeEach
    void setup() {

        String paymentExceptionsAdminOfficerEmailAddress = "payment-exceptions-ao@example.com";
        adminOfficerAppealSubmittedPayOfflinePersonalisation = new AdminOfficerAppealSubmittedPayOfflinePersonalisation(
            templateId,
            changeToHearingRequirementsAdminOfficerEmailAddress,
            paymentExceptionsAdminOfficerEmailAddress,
            adminOfficerPersonalisationProvider
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerAppealSubmittedPayOfflinePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {

        Long caseId = 12345L;
        assertEquals(caseId + "_APPEAL_SUBMITTED_PAY_OFFLINE_ADMIN_OFFICER",
            adminOfficerAppealSubmittedPayOfflinePersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        assertTrue(adminOfficerAppealSubmittedPayOfflinePersonalisation.getRecipientsList(asylumCase)
            .contains(changeToHearingRequirementsAdminOfficerEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> adminOfficerAppealSubmittedPayOfflinePersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        adminOfficerAppealSubmittedPayOfflinePersonalisation.getPersonalisation(asylumCase);
        verify(adminOfficerPersonalisationProvider).getChangeToHearingRequirementsPersonalisation(asylumCase);

    }
}
