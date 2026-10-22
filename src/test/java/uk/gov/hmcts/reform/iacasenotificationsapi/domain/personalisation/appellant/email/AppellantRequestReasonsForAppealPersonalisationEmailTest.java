package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantRequestReasonsForAppealPersonalisationEmailTest {

    private final String emailTemplateId = "someEmailTemplateId";
    private final String email24WeeksTemplateId = "some24WeeksEmailTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final String expectedDirectionDueDate = "27 Aug 2019";
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String mockedAppellantGivenNames = "someAppellantGivenNames";
    private final String mockedAppellantFamilyName = "someAppellantFamilyName";
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    DirectionFinder directionFinder;
    @Mock
    Direction direction;
    private AppellantRequestReasonsForAppealPersonalisationEmail
        appellantRequestReasonsForAppealSubmissionPersonalisationEmail;

    @BeforeEach
    public void setup() {

        String directionDueDate = "2019-08-27";
        when((direction.getDateDue())).thenReturn(directionDueDate);
        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_REASONS_FOR_APPEAL))
            .thenReturn(Optional.of(direction));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));

        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.empty());

        appellantRequestReasonsForAppealSubmissionPersonalisationEmail =
            new AppellantRequestReasonsForAppealPersonalisationEmail(
                emailTemplateId,
                email24WeeksTemplateId,
                iaAipFrontendUrl,
                directionFinder,
                recipientsFinder
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(emailTemplateId, appellantRequestReasonsForAppealSubmissionPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_24_weeks_template_id_for_stf_case() {
        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertEquals(email24WeeksTemplateId, appellantRequestReasonsForAppealSubmissionPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_REQUEST_REASONS_FOR_APPEAL_APPELLANT_AIP_EMAIL",
            appellantRequestReasonsForAppealSubmissionPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {
        String mockedAppellantEmailAddress = "appelant@example.net";
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        assertTrue(appellantRequestReasonsForAppealSubmissionPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantRequestReasonsForAppealSubmissionPersonalisationEmail.getRecipientsList(null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_direction_is_empty() {

        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_REASONS_FOR_APPEAL))
            .thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class,
                () -> appellantRequestReasonsForAppealSubmissionPersonalisationEmail.getPersonalisation(asylumCase));
        assertEquals("direction 'requestReasonsForAppeal' is not present", exception.getMessage());
    }


    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            appellantRequestReasonsForAppealSubmissionPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", mockedAppealReferenceNumber)
            .containsEntry("HO Ref Number", mockedAppealHomeOfficeReferenceNumber)
            .containsEntry("Given names", mockedAppellantGivenNames)
            .containsEntry("Family name", mockedAppellantFamilyName)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl)
            .containsEntry("due date", expectedDirectionDueDate)
            .containsEntry("date in the timetabling directions", expectedDirectionDueDate);


    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            appellantRequestReasonsForAppealSubmissionPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", "")
            .containsEntry("HO Ref Number", "")
            .containsEntry("Given names", "")
            .containsEntry("Family name", "")
            .containsEntry("Hyperlink to service", iaAipFrontendUrl)
            .containsEntry("due date", expectedDirectionDueDate)
            .containsEntry("date in the timetabling directions", expectedDirectionDueDate);
    }
}
