package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

@ExtendWith(MockitoExtension.class)
class AdminOfficerEditPaymentMethodPersonalisationTest {

    @Mock private AsylumCase asylumCase;
    @Mock private AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    private String templateId = "someTemplateId";
    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String adminOfficerEmailAddress = "adminOfficer@example.com";
    private String iaExUiFrontendUrl = "http://localhost";

    private AdminOfficerEditPaymentMethodPersonalisation adminOfficerEditPaymentMethodPersonalisation;

    @BeforeEach
    void setUp() {

        adminOfficerEditPaymentMethodPersonalisation =
            new AdminOfficerEditPaymentMethodPersonalisation(templateId, adminOfficerEmailAddress, adminOfficerPersonalisationProvider);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerEditPaymentMethodPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_EDIT_PAYMENT_METHOD_PENDING_PAYMENT_ADMIN_OFFICER",
            adminOfficerEditPaymentMethodPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        assertTrue(adminOfficerEditPaymentMethodPersonalisation.getRecipientsList(asylumCase)
            .contains(adminOfficerEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> adminOfficerEditPaymentMethodPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        when(adminOfficerPersonalisationProvider.getDefaultPersonalisation(asylumCase))
            .thenReturn(ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build());

        Map<String, String> personalisation =
            adminOfficerEditPaymentMethodPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
    }
}
