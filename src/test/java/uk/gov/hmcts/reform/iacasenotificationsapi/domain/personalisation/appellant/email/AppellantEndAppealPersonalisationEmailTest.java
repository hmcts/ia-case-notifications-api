package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantEndAppealPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    EmailAddressFinder emailAddressFinder;

    private Long caseId = 12345L;
    private String beforeListingEmailTemplateId = "beforeListingEmailTemplateId";
    private String afterListingEmailTemplateId = "afterListingEmailTemplateId";
    private String iaAipFrontendUrl = "http://localhost";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String mockedAppellantGivenNames = "someAppellantGivenNames";
    private String mockedAppellantFamilyName = "someAppellantFamilyName";
    private String mockedAppellantEmailAddress = "appelant@example.net";

    private AppellantEndAppealPersonalisationEmail appellantEndAppealPersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));

        appellantEndAppealPersonalisationEmail =
                new AppellantEndAppealPersonalisationEmail(
                        beforeListingEmailTemplateId,
                        afterListingEmailTemplateId,
                        iaAipFrontendUrl,
                        recipientsFinder,
                        emailAddressFinder
                );
    }

    @Test
    public void should_return_given_template_id_for_before_listing() {
        assertEquals(beforeListingEmailTemplateId, appellantEndAppealPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_template_id_for_after_listing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(HearingCentre.BELFAST));

        assertEquals(afterListingEmailTemplateId, appellantEndAppealPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_END_APPEAL_AIP_APPELLANT_EMAIL",
                appellantEndAppealPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
                .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        assertTrue(appellantEndAppealPersonalisationEmail.getRecipientsList(asylumCase)
                .contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantEndAppealPersonalisationEmail.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given_before_listing() {

        String endAppealApprover = "Judge";
        String outcomeOfAppeal = "Withdrawn";
        String reasonsOfOutcome = "error in application";
        String endAppealDate = LocalDate.now().toString();
        String designatedHearingCentre = "belfast@hearingcentre.gov";
        when(emailAddressFinder.getHearingCentreEmailAddress(asylumCase)).thenReturn(designatedHearingCentre);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(END_APPEAL_APPROVER_TYPE, String.class)).thenReturn(Optional.of(endAppealApprover));
        when(asylumCase.read(AsylumCaseDefinition.END_APPEAL_DATE, String.class)).thenReturn(Optional.of(endAppealDate));
        when(asylumCase.read(AsylumCaseDefinition.END_APPEAL_OUTCOME, String.class)).thenReturn(Optional.of(outcomeOfAppeal));
        when(asylumCase.read(AsylumCaseDefinition.END_APPEAL_OUTCOME_REASON, String.class)).thenReturn(Optional.of(reasonsOfOutcome));

        Map<String, String> personalisation =
                appellantEndAppealPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(endAppealApprover, personalisation.get("endAppealApprover"));
        assertEquals(LocalDate.parse(endAppealDate).format(DateTimeFormatter.ofPattern("d MMM yyyy")), personalisation.get("endAppealDate"));
        assertEquals(outcomeOfAppeal, personalisation.get("outcomeOfAppeal"));
        assertEquals(reasonsOfOutcome, personalisation.get("reasonsOfOutcome"));
        assertEquals(designatedHearingCentre, personalisation.get("designated hearing centre"));

        verify(emailAddressFinder).getHearingCentreEmailAddress(asylumCase);
        verify(emailAddressFinder, times(0)).getListCaseHearingCentreEmailAddress(asylumCase);

    }

    @Test
    public void should_return_personalisation_when_all_information_given_after_listing() {

        String endAppealApprover = "Judge";
        String outcomeOfAppeal = "Withdrawn";
        String reasonsOfOutcome = "error in application";
        String endAppealDate = LocalDate.now().toString();
        String designatedHearingCentre = "belfast@hearingcentre.gov";
        when(emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase)).thenReturn(designatedHearingCentre);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.BELFAST));
        when(asylumCase.read(END_APPEAL_APPROVER_TYPE, String.class)).thenReturn(Optional.of(endAppealApprover));
        when(asylumCase.read(AsylumCaseDefinition.END_APPEAL_DATE, String.class)).thenReturn(Optional.of(endAppealDate));
        when(asylumCase.read(AsylumCaseDefinition.END_APPEAL_OUTCOME, String.class)).thenReturn(Optional.of(outcomeOfAppeal));
        when(asylumCase.read(AsylumCaseDefinition.END_APPEAL_OUTCOME_REASON, String.class)).thenReturn(Optional.of(reasonsOfOutcome));

        Map<String, String> personalisation =
                appellantEndAppealPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(mockedAppealHomeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(mockedAppellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(mockedAppellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(endAppealApprover, personalisation.get("endAppealApprover"));
        assertEquals(LocalDate.parse(endAppealDate).format(DateTimeFormatter.ofPattern("d MMM yyyy")), personalisation.get("endAppealDate"));
        assertEquals(outcomeOfAppeal, personalisation.get("outcomeOfAppeal"));
        assertEquals(reasonsOfOutcome, personalisation.get("reasonsOfOutcome"));
        assertEquals(designatedHearingCentre, personalisation.get("designated hearing centre"));

        verify(emailAddressFinder).getListCaseHearingCentreEmailAddress(asylumCase);
        verify(emailAddressFinder, times(0)).getHearingCentreEmailAddress(asylumCase);

    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        String designatedHearingCentre = "belfast@hearingcentre.gov";
        when(emailAddressFinder.getHearingCentreEmailAddress(asylumCase)).thenReturn(designatedHearingCentre);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        Map<String, String> personalisation =
                appellantEndAppealPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(designatedHearingCentre, personalisation.get("designated hearing centre"));
    }
}
