package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminOfficerAppealSubmittedPendingPaymentPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    private final String templateId = "someTemplateId";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String adminOfficerEmailAddress = "adminOfficer@example.com";
    private final String iaExUiFrontendUrl = "http://localhost";

    private AdminOfficerAppealSubmittedPendingPaymentPersonalisation
        adminOfficerAppealSubmittedPendingPaymentPersonalisation;

    @BeforeEach
    void setup() {
        String listRef = "LP/12345/2019";
        when(adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase))
            .thenReturn(ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("ariaListingReference", listRef)
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build());

        String paymentExceptionsAdminOfficerEmailAddress = "payment-exceptions-ao@example.com";
        adminOfficerAppealSubmittedPendingPaymentPersonalisation =
            new AdminOfficerAppealSubmittedPendingPaymentPersonalisation(
                templateId,
                adminOfficerEmailAddress,
                paymentExceptionsAdminOfficerEmailAddress,
                adminOfficerPersonalisationProvider);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerAppealSubmittedPendingPaymentPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_APPEAL_SUBMITTED_PENDING_PAYMENT_ADMIN_OFFICER",
            adminOfficerAppealSubmittedPendingPaymentPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        assertTrue(adminOfficerAppealSubmittedPendingPaymentPersonalisation.getRecipientsList(asylumCase)
            .contains(adminOfficerEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
assertThrows(NullPointerException.class,
            () -> adminOfficerAppealSubmittedPendingPaymentPersonalisation.getPersonalisation((AsylumCase) null))
            ;
assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            adminOfficerAppealSubmittedPendingPaymentPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);
    }
}
