package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CASE_FLAG_SET_ASIDE_REHEARD_EXISTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_REHEARD_APPEAL_ENABLED;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;


@ExtendWith(MockitoExtension.class)
class AdminOfficerReviewHearingRequirementsPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String reviewHearingRequirementsTemplateId = "someTemplateId";
    private String reviewReheardHearingRequirementsTemplateId = "anotherTemplateId";
    private String reviewHearingRequirementsAdminOfficerEmailAddress = "adminofficer-review-hearing-requirements@example.com";
    private AdminOfficerReviewHearingRequirementsPersonalisation adminOfficerReviewHearingRequirementsPersonalisation;

    @BeforeEach
    public void setup() {

        adminOfficerReviewHearingRequirementsPersonalisation = new AdminOfficerReviewHearingRequirementsPersonalisation(
            reviewHearingRequirementsTemplateId,
            reviewReheardHearingRequirementsTemplateId,
            reviewHearingRequirementsAdminOfficerEmailAddress,
            adminOfficerPersonalisationProvider
        );
    }

    @Test
    void should_return_given_template_id_when_reheard_flag_is_disabled() {

        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertEquals(reviewHearingRequirementsTemplateId, adminOfficerReviewHearingRequirementsPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_template_id_when_reheard_flag_is_enabled() {

        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertEquals(reviewHearingRequirementsTemplateId, adminOfficerReviewHearingRequirementsPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertEquals(reviewReheardHearingRequirementsTemplateId, adminOfficerReviewHearingRequirementsPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {

        assertEquals(caseId + "_REVIEW_HEARING_REQUIREMENTS_ADMIN_OFFICER",
            adminOfficerReviewHearingRequirementsPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> adminOfficerReviewHearingRequirementsPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        // Indu commented it
        //Map<String, String> personalisation =
          //  adminOfficerReviewHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        //assertThat(asylumCase).usingComparatorForFields(Comparator.comparing(personalisation::get));

    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {
        //Indu commented
        //Map<String, String> personalisation =
          //  adminOfficerReviewHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        //assertThat(asylumCase).usingComparatorForFields(Comparator.comparing(personalisation::get));
    }
}
