package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantMarkAsReadyForUtTransferPersonalisationEmailTest {

    private final String beforeListingTemplateId = "beforeListingTemplateId";
    private final String afterListingTemplateId = "afterListingTemplateId";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "homeOfficeReferenceNumber";
    private final String ariaListingReference = "someAriaListingReference";
    private final String appellantGivenNames = "appellantGivenNames";
    private final String appellantFamilyName = "appellantFamilyName";
    private final String utAppealReferenceNumber = "1234567890";

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;

    private AppellantMarkAsReadyForUtTransferPersonalisationEmail appellantMarkAsReadyForUtTransferPersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(UT_APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(utAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));

        appellantMarkAsReadyForUtTransferPersonalisationEmail = new AppellantMarkAsReadyForUtTransferPersonalisationEmail(
            beforeListingTemplateId,
            afterListingTemplateId,
            recipientsFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        assertEquals(afterListingTemplateId,
            appellantMarkAsReadyForUtTransferPersonalisationEmail.getTemplateId(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.empty());
        assertEquals(beforeListingTemplateId,
            appellantMarkAsReadyForUtTransferPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_MARK_AS_READY_FOR_UT_TRANSFER_APPELLANT_EMAIL",
            appellantMarkAsReadyForUtTransferPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        String emailAddress = "appellant@example.com";
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(emailAddress));

        assertTrue(appellantMarkAsReadyForUtTransferPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(emailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantMarkAsReadyForUtTransferPersonalisationEmail.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            appellantMarkAsReadyForUtTransferPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("utAppealReferenceNumber", utAppealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber);

    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(UT_APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            appellantMarkAsReadyForUtTransferPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("ariaListingReference", "")
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("utAppealReferenceNumber", "")
            .containsEntry("homeOfficeReferenceNumber", "");
    }

    @Test
    public void should_return_false_if_appeal_not_yet_listed() {
        assertFalse(appellantMarkAsReadyForUtTransferPersonalisationEmail.isAppealListed(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        assertTrue(appellantMarkAsReadyForUtTransferPersonalisationEmail.isAppealListed(asylumCase));
    }

}
