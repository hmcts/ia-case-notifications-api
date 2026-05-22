package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.*;
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
public class AppellantRemoveDetainedStatusPersonalisationEmailTest {

    private final String beforeListingEmailTemplateId = "someEmailTemplateIdBeforeListing";
    private final String afterListingEmailTemplateId = "someEmailTemplateIdAfterListing";
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String mockedListingReferenceNumber = "someListingReferenceNumber";
    private final String mockedAppellantGivenNames = "Talha";
    private final String mockedAppellantFamilyName = "Awan";
    @Mock
    AsylumCase asylumCase;
    private AppellantRemoveDetainedStatusPersonalisationEmail appellantRemoveDetainedStatusPersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of(mockedAppellantFamilyName));


        appellantRemoveDetainedStatusPersonalisationEmail = new AppellantRemoveDetainedStatusPersonalisationEmail(
            beforeListingEmailTemplateId,
            afterListingEmailTemplateId
        );
    }

    @Test
    public void should_return_before_listing_template_id() {
        assertEquals(beforeListingEmailTemplateId,
            appellantRemoveDetainedStatusPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_after_listing_template_id() {
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class))
            .thenReturn(Optional.of(mockedListingReferenceNumber));
        assertEquals(afterListingEmailTemplateId,
            appellantRemoveDetainedStatusPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_REMOVE_DETENTION_STATUS_APPELLANT_EMAIL",
            appellantRemoveDetainedStatusPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_correct_recipient_email_address() {
        List<String> mockedContactPreferences = new ArrayList<>(List.of("wantsEmail"));

        when(asylumCase.read(CONTACT_PREFERENCE_UN_REP))
            .thenReturn(Optional.of(mockedContactPreferences));

        String mockedAppellantEmailAddress = "appellant@example.net";
        when(asylumCase.read(EMAIL, String.class))
            .thenReturn(Optional.of(mockedAppellantEmailAddress));

        assertTrue(appellantRemoveDetainedStatusPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_return_empty_recipient_set_when_email_contact_preference_not_chosen() {
        List<String> mockedContactPreferences = new ArrayList<>(List.of());

        when(asylumCase.read(CONTACT_PREFERENCE_UN_REP))
            .thenReturn(Optional.ofNullable(mockedContactPreferences));

        assertTrue(appellantRemoveDetainedStatusPersonalisationEmail.getRecipientsList(asylumCase)
            .isEmpty());
        verify(asylumCase, times(0)).read(EMAIL);

    }

    @Test
    public void should_return_personalisation_when_all_information_given_before_listing_case() {
        Map<String, String> personalisation =
            appellantRemoveDetainedStatusPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", mockedAppealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", mockedAppealHomeOfficeReferenceNumber)
            .containsEntry("appellantGivenNames", mockedAppellantGivenNames)
            .containsEntry("appellantFamilyName", mockedAppellantFamilyName)
            .containsEntry("ariaListingReference", "");

    }

    @Test
    public void should_return_personalisation_when_all_information_given_after_listing_case() {
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class))
            .thenReturn(Optional.of(mockedListingReferenceNumber));

        Map<String, String> personalisation =
            appellantRemoveDetainedStatusPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", mockedAppealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", mockedAppealHomeOfficeReferenceNumber)
            .containsEntry("ariaListingReference", mockedListingReferenceNumber)
            .containsEntry("appellantGivenNames", mockedAppellantGivenNames)
            .containsEntry("appellantFamilyName", mockedAppellantFamilyName);
    }
}
