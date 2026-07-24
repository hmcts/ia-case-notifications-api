package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeCmrHearingCancelledPersonalisationEmailTest {

    private final Long caseId = 12345L;
    private final String cmrCancelledHomeOfficeEmailTemplateId = "cmrCancelledHomeOfficeEmailTemplateId";
    private final String iaExUiFrontendUrl = "http://somexuiurl";
    private final String homeOfficeReferenceNumber = "someHOReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String oldHearingCentreAddress = "someHearingCentreAddress";
    private final String oldHearingDate = "1 January 2026";
    private final String oldHearingTime = "2:00pm";
    private final String someHearingDateTime = "2026-01-01T14:00";
    private final String homeOfficeEmail = "homeoffice@example.com";

    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;

    private HomeOfficeCmrHearingCancelledPersonalisationEmail homeOfficeCmrHearingCancelledPersonalisationEmail;

    @BeforeEach
    public void setup() {
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase)).thenReturn(oldHearingCentreAddress);
        when(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)).thenReturn(someHearingDateTime);
        when(dateTimeExtractor.extractHearingDate(someHearingDateTime)).thenReturn(oldHearingDate);
        when(dateTimeExtractor.extractHearingTime(someHearingDateTime)).thenReturn(oldHearingTime);

        homeOfficeCmrHearingCancelledPersonalisationEmail = new HomeOfficeCmrHearingCancelledPersonalisationEmail(
            cmrCancelledHomeOfficeEmailTemplateId,
            iaExUiFrontendUrl,
            emailAddressFinder,
            dateTimeExtractor,
            hearingDetailsFinder
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(cmrCancelledHomeOfficeEmailTemplateId,
            homeOfficeCmrHearingCancelledPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_CMR_CANCELLED_HOME_OFFICE_EMAIL",
            homeOfficeCmrHearingCancelledPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_list() {
        when(emailAddressFinder.getCmrListingHomeOfficeEmailAddress(asylumCase))
            .thenReturn(homeOfficeEmail);

        assertThat(homeOfficeCmrHearingCancelledPersonalisationEmail.getRecipientsList(asylumCase))
            .containsExactly(homeOfficeEmail);
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> homeOfficeCmrHearingCancelledPersonalisationEmail.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation =
            homeOfficeCmrHearingCancelledPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("oldHearingDate", oldHearingDate)
            .containsEntry("oldHearingTime", oldHearingTime)
            .containsEntry("oldHearingCentreAddress", oldHearingCentreAddress)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);
    }
}