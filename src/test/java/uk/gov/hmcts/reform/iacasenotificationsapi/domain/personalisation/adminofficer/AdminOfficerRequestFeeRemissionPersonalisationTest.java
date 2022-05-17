package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

        Map<String, String> expPersonalisation = ImmutableMap
                .<String, String>builder()
                .put(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER.value(), "")
                .put(APPELLANT_GIVEN_NAMES.value(), "")
                .put(APPELLANT_FAMILY_NAME.value(), "")
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .put(LEGAL_REP_REFERENCE_NUMBER.value(), "")
                .build();
        Map<String, String> personalisation =
            adminOfficerRequestFeeRemissionPersonalisation.getPersonalisation(asylumCase);

        assertThat(expPersonalisation).usingRecursiveComparison().isEqualTo(personalisation);

    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {

        Map<String, String> expPersonalisation = ImmutableMap
                .<String, String>builder()
                .put(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER.value(), "")
                .put(APPELLANT_GIVEN_NAMES.value(), "")
                .put(APPELLANT_FAMILY_NAME.value(), "")
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .put(LEGAL_REP_REFERENCE_NUMBER.value(), "")
                .build();
        Map<String, String> personalisation =
            adminOfficerRequestFeeRemissionPersonalisation.getPersonalisation(asylumCase);

        assertThat(expPersonalisation).usingRecursiveComparison().isEqualTo(personalisation);
    }

}
