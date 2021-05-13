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
class AdminOfficerAppealRemissionApprovedPersonalisationTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String feesAdminOfficerEmailAddress = "remissions-ao@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String linkToOnlineService = "someLinkToOnlineService";
    private AdminOfficerAppealRemissionApprovedPersonalisation adminOfficerAppealRemissionApprovedPersonalisation;

    @BeforeEach
    void setup() {

        adminOfficerAppealRemissionApprovedPersonalisation = new AdminOfficerAppealRemissionApprovedPersonalisation(
            templateId,
            feesAdminOfficerEmailAddress,
            adminOfficerPersonalisationProvider
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerAppealRemissionApprovedPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {

        assertEquals(caseId + "_REMISSION_DECISION_APPROVED_ADMIN_OFFICER",
            adminOfficerAppealRemissionApprovedPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        assertTrue(adminOfficerAppealRemissionApprovedPersonalisation.getRecipientsList(asylumCase)
            .contains(feesAdminOfficerEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> adminOfficerAppealRemissionApprovedPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        when(adminOfficerPersonalisationProvider.getDefaultPersonalisation(asylumCase)).thenReturn((ImmutableMap<String, String>) getPersonalisation());

        Map<String, String> personalisation =
            adminOfficerAppealRemissionApprovedPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(linkToOnlineService, personalisation.get("linkToOnlineService"));

    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {
        when(adminOfficerPersonalisationProvider.getDefaultPersonalisation(asylumCase)).thenReturn((ImmutableMap<String, String>) getEmptyPersonalisation());

        Map<String, String> personalisation =
            adminOfficerAppealRemissionApprovedPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
    }

    private Map<String, String> getPersonalisation() {

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("linkToOnlineService", linkToOnlineService)
            .build();
    }

    private Map<String, String> getEmptyPersonalisation() {

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "")
            .put("appellantGivenNames", "")
            .put("appellantFamilyName", "")
            .put("linkToOnlineService", "")
            .build();
    }

}
