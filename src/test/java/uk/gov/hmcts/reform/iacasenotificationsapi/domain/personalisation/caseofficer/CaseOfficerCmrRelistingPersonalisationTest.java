package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CMR_IS_REMOTE_HEARING;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseOfficerCmrRelistingPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;

    private final Long caseId = 12345L;
    private final String inPersonTemplateId = "inPersonTemplateId";
    private final String remoteTemplateId = "remoteTemplateId";
    private final String caseOfficerEmail = "caseofficer@example.com";
    private final String hearingCentreAddress = "Taylor House, 88 Rosebery Avenue, London";
    private final String appealReferenceNumber = "PA/00001/2024";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";

    private CaseOfficerCmrRelistingPersonalisation caseOfficerCmrRelistingPersonalisation;

    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(emailAddressFinder.getListCaseCaseOfficerHearingCentreEmailAddress(asylumCase)).thenReturn(caseOfficerEmail);
        when(hearingDetailsFinder.getHearingCentreLocation(asylumCase)).thenReturn(hearingCentreAddress);
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(Map.of(
                "appealReferenceNumber", appealReferenceNumber,
                "appellantGivenNames", appellantGivenNames,
                "appellantFamilyName", appellantFamilyName
        ));

        caseOfficerCmrRelistingPersonalisation = new CaseOfficerCmrRelistingPersonalisation(
                inPersonTemplateId,
                remoteTemplateId,
                emailAddressFinder,
                personalisationProvider,
                hearingDetailsFinder
        );
    }

    @Test
    void getTemplateId_should_return_in_person_template_when_not_remote() {
        when(asylumCase.read(CMR_IS_REMOTE_HEARING)).thenReturn(Optional.of(YesOrNo.NO));
        assertEquals(inPersonTemplateId, caseOfficerCmrRelistingPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void getTemplateId_should_return_remote_template_when_remote() {
        when(asylumCase.read(CMR_IS_REMOTE_HEARING)).thenReturn(Optional.of(YesOrNo.YES));
        assertEquals(remoteTemplateId, caseOfficerCmrRelistingPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void getTemplateId_should_return_in_person_template_when_field_absent() {
        when(asylumCase.read(CMR_IS_REMOTE_HEARING)).thenReturn(Optional.empty());
        assertEquals(inPersonTemplateId, caseOfficerCmrRelistingPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void getRecipientsList() {
        Set<String> recipients = caseOfficerCmrRelistingPersonalisation.getRecipientsList(asylumCase);
        assertTrue(recipients.contains(caseOfficerEmail));
        assertEquals(1, recipients.size());
    }

    @Test
    void getReferenceId() {
        assertEquals(caseId + "_CMR_LISTED_CASE_OFFICER",
                caseOfficerCmrRelistingPersonalisation.getReferenceId(caseId));
    }

    @Test
    void getPersonalisation() {
        Map<String, String> personalisation =
                caseOfficerCmrRelistingPersonalisation.getPersonalisation(callback);

        assertFalse(personalisation.isEmpty());
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
    }

    @Test
    void getPersonalisation_should_throw_exception_when_callback_is_null() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> caseOfficerCmrRelistingPersonalisation.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }
}