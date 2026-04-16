package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailDirection;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeUploadBailSummaryDirectionPersonalisationTest {

    private final String templateId = "someTemplateId";
    private final String homeOfficeEmailAddress = "HO_user@example.com";
    private final String bailReferenceNumber = "someReferenceNumber";
    private final String legalRepReference = "someLegalRepReference";
    private final String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String applicantGivenNames = "someApplicantGivenNames";
    private final String applicantFamilyName = "someApplicantFamilyName";

    @Mock
    BailCase bailCase;
    @Mock
    IdValue<BailDirection> oldestDirectionIdValue;
    @Mock
    BailDirection oldestDirection;
    @Mock
    IdValue<BailDirection> newestDirectionIdValue;
    @Mock
    BailDirection newestDirection;
    private HomeOfficeUploadBailSummaryDirectionPersonalisation homeOfficeUploadBailSummaryDirectionPersonalisation;

    @BeforeEach
    public void setup() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        String dateOfCompliance = "2022-05-24";
        when(bailCase.read(BAIL_DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.of(dateOfCompliance));

        when(oldestDirectionIdValue.getValue()).thenReturn(oldestDirection);
        String dateTimeOldestDirectionCreated = "2022-05-24T15:00:00.000000000";
        when(oldestDirection.getDateTimeDirectionCreated()).thenReturn(dateTimeOldestDirectionCreated);
        when(newestDirectionIdValue.getValue()).thenReturn(newestDirection);
        String dateTimeLatestDirectionCreated = "2022-05-24T16:00:00.000000000";
        when(newestDirection.getDateTimeDirectionCreated()).thenReturn(dateTimeLatestDirectionCreated);
        when(bailCase.read(DIRECTIONS)).thenReturn(Optional.of(List.of(oldestDirectionIdValue, newestDirectionIdValue)));

        when(newestDirection.getDateOfCompliance()).thenReturn(dateOfCompliance);

        homeOfficeUploadBailSummaryDirectionPersonalisation =
            new HomeOfficeUploadBailSummaryDirectionPersonalisation(templateId, homeOfficeEmailAddress);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, homeOfficeUploadBailSummaryDirectionPersonalisation.getTemplateId(bailCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_BAIL_UPLOAD_BAIL_SUMMARY_DIRECTION_HOME_OFFICE",
            homeOfficeUploadBailSummaryDirectionPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_bail_case() {
        assertTrue(homeOfficeUploadBailSummaryDirectionPersonalisation.getRecipientsList(bailCase)
            .contains(homeOfficeEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> homeOfficeUploadBailSummaryDirectionPersonalisation.getPersonalisation((BailCase) null));
        assertEquals("bailCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            homeOfficeUploadBailSummaryDirectionPersonalisation.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals("\nLegal representative reference: " + legalRepReference,
            personalisation.get("legalRepReference"));
        assertThat(personalisation)
            .containsEntry("applicantGivenNames", applicantGivenNames)
            .containsEntry("applicantFamilyName", applicantFamilyName)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("dateOfCompliance", "24 May 2022");
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(DIRECTIONS)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            homeOfficeUploadBailSummaryDirectionPersonalisation.getPersonalisation(bailCase);

        assertThat(personalisation)
            .containsEntry("bailReferenceNumber", "")
            .containsEntry("legalRepReference", "")
            .containsEntry("applicantGivenNames", "")
            .containsEntry("applicantFamilyName", "")
            .containsEntry("homeOfficeReferenceNumber", "")
            .containsEntry("dateOfCompliance", "");
    }

}