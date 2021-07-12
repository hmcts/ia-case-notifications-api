package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
class AdminOfficerChangeToHearingRequirementsPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;
    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String changeToHearingRequirementsAdminOfficerEmailAddress =
        "adminofficer-change-to-hearing-requirements@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String ariaListingReference = "someAriaListingReference";
    private String linkToOnlineService = "someLinkToOnlineService";
    private AdminOfficerChangeToHearingRequirementsPersonalisation
        adminOfficerChangeToHearingRequirementsPersonalisation;

    @BeforeEach
    void setup() {

        adminOfficerChangeToHearingRequirementsPersonalisation =
            new AdminOfficerChangeToHearingRequirementsPersonalisation(
                templateId,
                changeToHearingRequirementsAdminOfficerEmailAddress,
                adminOfficerPersonalisationProvider
            );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerChangeToHearingRequirementsPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {

        assertEquals(caseId + "_CHANGE_TO_HEARING_REQUIREMENTS_ADMIN_OFFICER",
            adminOfficerChangeToHearingRequirementsPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> adminOfficerChangeToHearingRequirementsPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        when(adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase)).thenReturn((ImmutableMap<String, String>) getPersonalisation());

        Map<String, String> personalisation =
            adminOfficerChangeToHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(linkToOnlineService, personalisation.get("linkToOnlineService"));

    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {
        when(adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase)).thenReturn((ImmutableMap<String, String>) getPersonalisation());

        Map<String, String> personalisation =
            adminOfficerChangeToHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(linkToOnlineService, personalisation.get("linkToOnlineService"));
    }

    private Map<String, String> getPersonalisation() {

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("ariaListingReference", ariaListingReference)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("linkToOnlineService", linkToOnlineService)
            .build();
    }
}
