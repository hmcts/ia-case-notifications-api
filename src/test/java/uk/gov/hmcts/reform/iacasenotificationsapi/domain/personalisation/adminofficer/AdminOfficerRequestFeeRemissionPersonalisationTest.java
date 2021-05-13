package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AdminOfficerRequestFeeRemissionPersonalisationTest {

    @Mock private AsylumCase asylumCase;
    @Mock protected CustomerServicesProvider customerServicesProvider;

    private Long caseId = 12345L;
    private String templateId = "applyForLateRemissionTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String adminOfficerEmailAddress = "adminOfficer@example.com";
    private String appealReferenceNumber = "test";
    private String legalRepReferenceNumber = "test";
    private String appellantGivenNames = "test";
    private String appellantFamilyName = "test";
    private String customerServicesTelephone = "test";
    private String customerServicesEmail = "test";

    private AdminOfficerRequestFeeRemissionPersonalisation adminOfficerRequestFeeRemissionPersonalisation;

    @BeforeEach
    void setUp() {

        adminOfficerRequestFeeRemissionPersonalisation =
            new AdminOfficerRequestFeeRemissionPersonalisation(
                templateId, iaExUiFrontendUrl,
                adminOfficerEmailAddress, customerServicesProvider);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerRequestFeeRemissionPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {

        assertEquals(caseId + "_ADMIN_OFFICER_LATE_REMISSION_SUBMITTED",
            adminOfficerRequestFeeRemissionPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        assertTrue(adminOfficerRequestFeeRemissionPersonalisation.getRecipientsList(asylumCase)
            .contains(adminOfficerEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> adminOfficerRequestFeeRemissionPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(getCustomerServicesPersonalisation());

        Map<String, String> personalisation =
            adminOfficerRequestFeeRemissionPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, personalisation.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, personalisation.get("customerServicesEmail"));

    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(getCustomerServicesPersonalisation());
        Map<String, String> personalisation =
            adminOfficerRequestFeeRemissionPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("legalRepReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, personalisation.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, personalisation.get("customerServicesEmail"));
    }

    public Map<String, String> getCustomerServicesPersonalisation() {

        final ImmutableMap.Builder<String, String> customerServicesValues = ImmutableMap
            .<String, String>builder()
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail);

        return customerServicesValues.build();
    }
}
