package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;


@ExtendWith(MockitoExtension.class)
class AppellantConfirmHOReviewMaintainedStatutoryTimeframe24WeeksPersonalisationEmailTest {

    private final String maintainedResponseReviewDirectionTemplateId = "maintainedResponseReviewDirectionTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    RecipientsFinder recipientsFinder;

    private AppellantConfirmHOReviewMaintainedStatutoryTimeframe24WeeksPersonalisationEmail
        appellantConfirmHOReviewMaintainedStatutoryTimeframe24WeeksPersonalisationEmail;

    @BeforeEach
    void setUp() {

        appellantConfirmHOReviewMaintainedStatutoryTimeframe24WeeksPersonalisationEmail =
            new AppellantConfirmHOReviewMaintainedStatutoryTimeframe24WeeksPersonalisationEmail(
                maintainedResponseReviewDirectionTemplateId, iaAipFrontendUrl, recipientsFinder, customerServicesProvider);
    }

    @Test
    void should_return_personalisation_when_all_information_given_before_listing() {

        String appealReferenceNumber = "someReferenceNumber";
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));

        String appellantGivenNames = "someAppellantGivenNames";
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        String appellantFamilyName = "someAppellantFamilyName";
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        Map<String, String> personalisation =
            appellantConfirmHOReviewMaintainedStatutoryTimeframe24WeeksPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", mockedAppealHomeOfficeReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl);

    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            appellantConfirmHOReviewMaintainedStatutoryTimeframe24WeeksPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("homeOfficeReferenceNumber", "")
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("Hyperlink to service", iaAipFrontendUrl);
    }

    @Test
    public void should_return_given_template_id_for_decision_maintained() {

        assertEquals(maintainedResponseReviewDirectionTemplateId, appellantConfirmHOReviewMaintainedStatutoryTimeframe24WeeksPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_STATUTORY_TIMEFRAME_24WEEKS_CONFIRM_HO_REVIEW_MAINTAINED_AIP_EMAIL",
            appellantConfirmHOReviewMaintainedStatutoryTimeframe24WeeksPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        String mockedAppellantEmailAddress = "appelant@example.net";
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        assertTrue(appellantConfirmHOReviewMaintainedStatutoryTimeframe24WeeksPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantConfirmHOReviewMaintainedStatutoryTimeframe24WeeksPersonalisationEmail.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}
