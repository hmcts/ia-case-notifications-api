package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

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

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeCmrListingPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;
    @Mock
    EmailAddressFinder emailAddressFinder;

    private final Long caseId = 12345L;
    private final String templateId = "someTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String homeOfficeEmail = "homeoffice@example.com";
    private final String appealReferenceNumber = "PA/00001/2024";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String hearingDateTime = "2024-06-01T10:00:00";
    private final String hearingDate = "2024-06-01";
    private final String hearingTime = "10:00";
    private final String hearingCentreAddress = "Taylor House, 88 Rosebery Avenue, London";

    private HomeOfficeCmrListingPersonalisation homeOfficeCmrListingPersonalisation;

    @BeforeEach
    void setUp() {
        when(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn(hearingCentreAddress);
        when(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeEmail);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        homeOfficeCmrListingPersonalisation = new HomeOfficeCmrListingPersonalisation(
                templateId,
                iaExUiFrontendUrl,
                dateTimeExtractor,
                hearingDetailsFinder,
                emailAddressFinder
        );
    }

    @Test
    void getTemplateId() {
        assertEquals(templateId, homeOfficeCmrListingPersonalisation.getTemplateId());
    }

    @Test
    void getRecipientsList() {
        Set<String> recipients = homeOfficeCmrListingPersonalisation.getRecipientsList(asylumCase);
        assertTrue(recipients.contains(homeOfficeEmail));
        assertEquals(1, recipients.size());
    }

    @Test
    void getReferenceId() {
        assertEquals(caseId + "_LIST_CMR_HOME_OFFICE_EMAIL",
                homeOfficeCmrListingPersonalisation.getReferenceId(caseId));
    }

    @Test
    void getPersonalisation() {
        Map<String, String> personalisation =
                homeOfficeCmrListingPersonalisation.getPersonalisation(asylumCase);

        assertFalse(personalisation.isEmpty());
        assertEquals(appealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("Hyperlink to service"));
    }

    @Test
    void getPersonalisation_should_return_empty_strings_when_optional_fields_absent() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
                homeOfficeCmrListingPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
    }

    @Test
    void should_return_exception_when_asylumcase_is_null() {
        NullPointerException catchNull = assertThrows(NullPointerException.class, () -> homeOfficeCmrListingPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase cannot be null", catchNull.getMessage());
    }
}