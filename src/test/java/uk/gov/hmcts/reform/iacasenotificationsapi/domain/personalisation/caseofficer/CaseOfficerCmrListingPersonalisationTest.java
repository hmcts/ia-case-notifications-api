package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
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
class CaseOfficerCmrListingPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;
    @Mock
    DateTimeExtractor dateTimeExtractor;

    private final Long caseId = 12345L;
    private final String inPersonTemplateId = "inPersonTemplateId";
    private final String remoteTemplateId = "remoteTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String caseOfficerEmail = "caseofficer@example.com";
    private final String hearingCentreAddress = "Taylor House, 88 Rosebery Avenue, London";
    private final String appealReferenceNumber = "PA/00001/2024";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String hearingDateTime = "2024-06-01T10:00:00";
    private final String hearingDate = "2024-06-01";
    private final String hearingTime = "10:00";

    private CaseOfficerCmrListingPersonalisation caseOfficerCmrListingPersonalisation;

    @BeforeEach
    void setUp() {
        when(emailAddressFinder.getListCaseCaseOfficerHearingCentreEmailAddress(asylumCase)).thenReturn(caseOfficerEmail);
        when(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase)).thenReturn(hearingCentreAddress);
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        caseOfficerCmrListingPersonalisation = new CaseOfficerCmrListingPersonalisation(
                inPersonTemplateId,
                remoteTemplateId,
                iaExUiFrontendUrl,
                emailAddressFinder,
                hearingDetailsFinder,
                dateTimeExtractor
        );
    }

    @Test
    void getTemplateId_should_return_in_person_template_when_not_remote() {
        when(asylumCase.read(CMR_IS_REMOTE_HEARING)).thenReturn(Optional.of(YesOrNo.NO));
        assertEquals(inPersonTemplateId, caseOfficerCmrListingPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void getTemplateId_should_return_remote_template_when_remote() {
        when(asylumCase.read(CMR_IS_REMOTE_HEARING)).thenReturn(Optional.of(YesOrNo.YES));
        assertEquals(remoteTemplateId, caseOfficerCmrListingPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void getTemplateId_should_return_in_person_template_when_field_absent() {
        when(asylumCase.read(CMR_IS_REMOTE_HEARING)).thenReturn(Optional.empty());
        assertEquals(inPersonTemplateId, caseOfficerCmrListingPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void getRecipientsList() {
        Set<String> recipients = caseOfficerCmrListingPersonalisation.getRecipientsList(asylumCase);
        assertTrue(recipients.contains(caseOfficerEmail));
        assertEquals(1, recipients.size());
    }

    @Test
    void getReferenceId() {
        assertEquals(caseId + "_CMR_LISTED_CASE_OFFICER",
                caseOfficerCmrListingPersonalisation.getReferenceId(caseId));
    }

    @Test
    void getPersonalisation() {
        Map<String, String> personalisation =
                caseOfficerCmrListingPersonalisation.getPersonalisation(asylumCase);

        assertFalse(personalisation.isEmpty());
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
    }

    @Test
    void getPersonalisation_should_return_empty_strings_when_optional_fields_absent() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
                caseOfficerCmrListingPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
    }

    @Test
    void getPersonalisation_should_throw_exception_when_asylum_case_is_null() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> caseOfficerCmrListingPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}