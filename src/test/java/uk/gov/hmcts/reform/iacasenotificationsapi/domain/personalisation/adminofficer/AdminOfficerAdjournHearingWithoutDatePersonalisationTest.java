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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AdminOfficerAdjournHearingWithoutDatePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    private String templateId = "someTemplateId";

    private String adminOfficerEmailAddress = "adminOfficer@example.com";

    private AdminOfficerAdjournHearingWithoutDatePersonalisation adminOfficerAdjournHearingWithoutDatePersonalisation;

    @BeforeEach
    public void setup() {


        adminOfficerAdjournHearingWithoutDatePersonalisation =
            new AdminOfficerAdjournHearingWithoutDatePersonalisation(templateId, adminOfficerEmailAddress,
                adminOfficerPersonalisationProvider);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerAdjournHearingWithoutDatePersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_ADJOURN_HEARING_WITHOUT_DATE_ADMIN_OFFICER",
            adminOfficerAdjournHearingWithoutDatePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(adminOfficerAdjournHearingWithoutDatePersonalisation.getRecipientsList(asylumCase)
            .contains(adminOfficerEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> adminOfficerAdjournHearingWithoutDatePersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase))
            .thenReturn(getPersonalisation());

        Map<String, String> personalisation =
            adminOfficerAdjournHearingWithoutDatePersonalisation.getPersonalisation(asylumCase);

        assertEquals(getPersonalisation().get("appealReferenceNumber"), personalisation.get("appealReferenceNumber"));
        assertEquals(getPersonalisation().get("appellantGivenNames"), personalisation.get("appellantGivenNames"));
        assertEquals(getPersonalisation().get("appellantFamilyName"), personalisation.get("appellantFamilyName"));
        assertEquals(getPersonalisation().get("ariaListingReference"), personalisation.get("ariaListingReference"));
        assertEquals(getPersonalisation().get("linkToOnlineService"), personalisation.get("linkToOnlineService"));

    }

    private ImmutableMap<String,String> getPersonalisation() {

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "PA/12345/001")
            .put("ariaListingReference", "ariaListingReference")
            .put("linkToOnlineService", "linkToOnlineService")
            .put("appellantGivenNames", "Talha")
            .put("appellantFamilyName", "Awan")
            .build();
    }

}
