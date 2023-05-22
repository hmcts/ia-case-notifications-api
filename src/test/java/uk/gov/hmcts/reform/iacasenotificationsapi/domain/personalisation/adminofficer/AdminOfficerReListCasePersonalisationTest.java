package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;

@ExtendWith(MockitoExtension.class)
class AdminOfficerReListCasePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    private String templateId = "someTemplateId";
    private AdminOfficerReListCasePersonalisation adminOfficerReListCasePersonalisation;


    @BeforeEach
    void setup() {

        String changeToHearingRequirementsAdminOfficerEmailAddress =
            "adminofficer-change-to-hearing-requirements@example.com";
        adminOfficerReListCasePersonalisation = new AdminOfficerReListCasePersonalisation(
            templateId,
            changeToHearingRequirementsAdminOfficerEmailAddress,
            new AdminOfficerPersonalisationProvider("")
        );

    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerReListCasePersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {

        Long caseId = 12345L;
        assertEquals(caseId + "_RE_LIST_CASE_ADMIN_OFFICER",
            adminOfficerReListCasePersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> adminOfficerReListCasePersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> expPersonalisation = ImmutableMap
                .<String, String>builder()
                .put(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER.value(), "")
                .put(APPELLANT_GIVEN_NAMES.value(), "")
                .put(APPELLANT_FAMILY_NAME.value(), "")
                .put("linkToOnlineService", "")
                .put(ARIA_LISTING_REFERENCE.value(), "")
                .build();
        Map<String, String> personalisation = adminOfficerReListCasePersonalisation.getPersonalisation(asylumCase);

        assertThat(expPersonalisation).usingRecursiveComparison().isEqualTo(personalisation);

    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {

        Map<String, String> expPersonalisation = ImmutableMap
                .<String, String>builder()
                .put(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER.value(), "")
                .put(APPELLANT_GIVEN_NAMES.value(), "")
                .put(APPELLANT_FAMILY_NAME.value(), "")
                .put("linkToOnlineService", "")
                .put(ARIA_LISTING_REFERENCE.value(), "")
                .build();
        Map<String, String> personalisation = adminOfficerReListCasePersonalisation.getPersonalisation(asylumCase);

        assertThat(expPersonalisation).usingRecursiveComparison().isEqualTo(personalisation);
    }
}
