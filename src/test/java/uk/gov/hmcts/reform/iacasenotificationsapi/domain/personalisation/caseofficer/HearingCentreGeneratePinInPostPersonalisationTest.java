package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_PIN_IN_POST;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@ExtendWith(MockitoExtension.class)
class HearingCentreGeneratePinInPostPersonalisationTest {
    @Mock
    private EmailAddressFinder emailAddressFinder;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private Callback<AsylumCase> callback;

    private HearingCentreGeneratePinInPostPersonalisation hearingCentreGeneratePinInPostPersonalisation;

    private final String templateId = "legalOfficerTemplateId";

    @BeforeEach
    void setUp() {
        String aipSelfRepLink = "/somePath";
        String aipLink = "someUrl";
        hearingCentreGeneratePinInPostPersonalisation = new HearingCentreGeneratePinInPostPersonalisation(
            templateId,
            aipSelfRepLink,
            aipLink,
            emailAddressFinder
        );
    }

    @Test
    void getRecipientsList() {
        String emailAddress = "someEmail";
        when(emailAddressFinder.getHearingCentreEmailAddress(asylumCase)).thenReturn(emailAddress);

        Set<String> recipientsList = hearingCentreGeneratePinInPostPersonalisation.getRecipientsList(asylumCase);

        assertEquals(1, recipientsList.size());
        assertTrue(recipientsList.contains(emailAddress));
    }

    @Test
    void getTemplateId() {
        assertEquals(templateId, hearingCentreGeneratePinInPostPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void getReferenceId() {
        assertEquals("12345_GENERATED_PIN_IN_POST_HEARING_CENTRE",
            hearingCentreGeneratePinInPostPersonalisation.getReferenceId(12345L));
    }

    @Test
    void getPersonalisation_with_no_details() {
        initializePrefixes(hearingCentreGeneratePinInPostPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_PIN_IN_POST, PinInPostDetails.class)).thenReturn(Optional.empty());
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(1234L);

        Map<String, String> personalisation = hearingCentreGeneratePinInPostPersonalisation.getPersonalisation(callback);

        assertEquals("Accelerated detained appeal", personalisation.get("subjectPrefix"));
        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("someUrl/somePath", personalisation.get("linkToPiPStartPage"));
        assertEquals("1234", personalisation.get("ccdCaseId"));
        assertEquals("", personalisation.get("securityCode"));
        assertEquals("", personalisation.get("validDate"));
        assertEquals("someUrl", personalisation.get("Hyperlink to service"));
    }

    @Test
    void getPersonalisation_with_details() {
        initializePrefixes(hearingCentreGeneratePinInPostPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("someAppealNumber"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("someGivenName"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("someFamilyName"));
        when(asylumCase.read(APPELLANT_PIN_IN_POST, PinInPostDetails.class)).thenReturn(Optional.of(
            PinInPostDetails.builder()
                .accessCode("someAccessCode")
                .expiryDate("2024-12-31")
                .build()
        ));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(1234L);

        Map<String, String> personalisation = hearingCentreGeneratePinInPostPersonalisation.getPersonalisation(callback);

        assertEquals("Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertEquals("someAppealNumber", personalisation.get("appealReferenceNumber"));
        assertEquals("someGivenName", personalisation.get("appellantGivenNames"));
        assertEquals("someFamilyName", personalisation.get("appellantFamilyName"));
        assertEquals("someUrl/somePath", personalisation.get("linkToPiPStartPage"));
        assertEquals("1234", personalisation.get("ccdCaseId"));
        assertEquals("someAccessCode", personalisation.get("securityCode"));
        assertEquals("31 Dec 2024", personalisation.get("validDate"));
        assertEquals("someUrl", personalisation.get("Hyperlink to service"));
    }

    @Test
    void should_throw_exception_on_personalisation_when_callback_is_null() {
        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> hearingCentreGeneratePinInPostPersonalisation.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_personalisation_when_asylumCase_is_null() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(null);
        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> hearingCentreGeneratePinInPostPersonalisation.getPersonalisation(callback));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}