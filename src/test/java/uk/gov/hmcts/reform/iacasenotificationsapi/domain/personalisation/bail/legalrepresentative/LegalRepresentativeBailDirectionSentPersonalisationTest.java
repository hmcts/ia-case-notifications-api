package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailDirectionSentPersonalisation;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeBailDirectionSentPersonalisationTest {

    private final String templateIdForDirectRecipient = "someTemplateIdForDirectRecipient";
    private final String bailReferenceNumber = "someReferenceNumber";
    private final String legalRepReference = "someLegalRepReference";
    private final String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String applicantGivenNames = "someApplicantGivenNames";
    private final String applicantFamilyName = "someApplicantFamilyName";
    private final String sendDirectionDescription = "someDescriptionOfTheDirectionSent";
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

    private LegalRepresentativeBailDirectionSentPersonalisation legalRepresentativeBailDirectionSentPersonalisation;

    @BeforeEach
    public void setup() {
        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(oldestDirectionIdValue.getValue()).thenReturn(oldestDirection);
        String dateTimeOldestDirectionCreated = "2022-05-24T15:00:00.000000000";
        when(oldestDirection.getDateTimeDirectionCreated()).thenReturn(dateTimeOldestDirectionCreated);
        when(newestDirectionIdValue.getValue()).thenReturn(newestDirection);
        String dateTimeLatestDirectionCreated = "2022-05-24T16:00:00.000000000";
        when(newestDirection.getDateTimeDirectionCreated()).thenReturn(dateTimeLatestDirectionCreated);
        when(bailCase.read(DIRECTIONS)).thenReturn(Optional.of(List.of(oldestDirectionIdValue, newestDirectionIdValue)));

        when(newestDirection.getSendDirectionDescription()).thenReturn(sendDirectionDescription);
        String dateOfCompliance = "2022-05-24";
        when(newestDirection.getDateOfCompliance()).thenReturn(dateOfCompliance);
        when(newestDirection.getSendDirectionList()).thenReturn("Legal Representative");

        String templateIdForOtherParties = "someTemplateIdForOtherParties";
        legalRepresentativeBailDirectionSentPersonalisation =
            new LegalRepresentativeBailDirectionSentPersonalisation(templateIdForDirectRecipient, templateIdForOtherParties);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateIdForDirectRecipient, legalRepresentativeBailDirectionSentPersonalisation.getTemplateId(bailCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_BAIL_SENT_DIRECTION_LEGAL_REPRESENTATIVE",
            legalRepresentativeBailDirectionSentPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> legalRepresentativeBailDirectionSentPersonalisation.getPersonalisation((BailCase) null));
        assertEquals("bailCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information_given_as_direct_recipient() {

        Map<String, String> personalisation =
            legalRepresentativeBailDirectionSentPersonalisation.getPersonalisation(bailCase);

        assertThat(personalisation)
            .containsEntry("bailReferenceNumber", bailReferenceNumber)
            .containsEntry("applicantGivenNames", applicantGivenNames)
            .containsEntry("applicantFamilyName", applicantFamilyName)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber);
        assertEquals("\nLegal representative reference: " + legalRepReference,
            personalisation.get("legalRepReference"));
        assertThat(personalisation)
            .containsEntry("sendDirectionDescription", sendDirectionDescription)
            .containsEntry("dateOfCompliance", "24 May 2022");
    }

    @Test
    public void should_return_personalisation_when_all_information_given_as_other_party() {
        when(newestDirection.getSendDirectionList()).thenReturn("Applicant");

        Map<String, String> personalisation =
            legalRepresentativeBailDirectionSentPersonalisation.getPersonalisation(bailCase);

        assertThat(personalisation)
            .containsEntry("bailReferenceNumber", bailReferenceNumber)
            .containsEntry("applicantGivenNames", applicantGivenNames)
            .containsEntry("applicantFamilyName", applicantFamilyName)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber);
        assertEquals("\nLegal representative reference: " + legalRepReference,
            personalisation.get("legalRepReference"));
        assertThat(personalisation)
            .containsEntry("sendDirectionDescription", sendDirectionDescription)
            .containsEntry("dateOfCompliance", "24 May 2022")
            .containsEntry("party", "Applicant");
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(DIRECTIONS)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            legalRepresentativeBailDirectionSentPersonalisation.getPersonalisation(bailCase);

        assertThat(personalisation)
            .containsEntry("bailReferenceNumber", "")
            .containsEntry("applicantGivenNames", "")
            .containsEntry("applicantFamilyName", "")
            .containsEntry("homeOfficeReferenceNumber", "")
            .containsEntry("legalRepReference", "")
            .containsEntry("sendDirectionDescription", "")
            .containsEntry("dateOfCompliance", "")
            .containsEntry("party", "");
    }
}
