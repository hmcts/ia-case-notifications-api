package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_PIN_IN_POST;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
class AppellantGeneratePinInPostPersonalisationSmsTest {
    @Mock
    private RecipientsFinder recipientsFinder;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private AsylumCase asylumCase;

    private AppellantGeneratePinInPostPersonalisationSms appellantGeneratePinInPostPersonalisationSms;

    private final String templateId = "legalOfficerTemplateId";
    private final String phoneNumber = "someNumber";

    @BeforeEach
    void setUp() {
        String aipSelfRepLink = "/somePath";
        String aipLink = "someUrl";
        appellantGeneratePinInPostPersonalisationSms = new AppellantGeneratePinInPostPersonalisationSms(
            templateId,
            aipSelfRepLink,
            aipLink,
            recipientsFinder
        );
    }

    @Test
    void getRecipientsList_aip() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(phoneNumber));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
            .thenReturn(Optional.of(JourneyType.AIP));

        Set<String> recipientsList = appellantGeneratePinInPostPersonalisationSms.getRecipientsList(asylumCase);

        verify(recipientsFinder).findAll(asylumCase, NotificationType.SMS);
        verify(recipientsFinder, never()).findReppedAppellant(asylumCase, NotificationType.SMS);
        assertEquals(1, recipientsList.size());
        assertTrue(recipientsList.contains(phoneNumber));
    }

    @Test
    void getRecipientsList_non_aip() {
        when(recipientsFinder.findReppedAppellant(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(phoneNumber));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
            .thenReturn(Optional.of(JourneyType.REP));

        Set<String> recipientsList = appellantGeneratePinInPostPersonalisationSms.getRecipientsList(asylumCase);

        verify(recipientsFinder, never()).findAll(asylumCase, NotificationType.SMS);
        verify(recipientsFinder).findReppedAppellant(asylumCase, NotificationType.SMS);
        assertEquals(1, recipientsList.size());
        assertTrue(recipientsList.contains(phoneNumber));
    }

    @Test
    void getTemplateId() {
        assertEquals(templateId, appellantGeneratePinInPostPersonalisationSms.getTemplateId());
    }

    @Test
    void getReferenceId() {
        assertEquals("12345_GENERATED_PIN_IN_POST_APPELLANT_SMS",
            appellantGeneratePinInPostPersonalisationSms.getReferenceId(12345L));
    }

    @Test
    void getPersonalisation_with_no_details() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(1234L);

        Map<String, String> personalisation = appellantGeneratePinInPostPersonalisationSms.getPersonalisation(callback);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("", personalisation.get("appellantDateOfBirth"));
        assertEquals("someUrl/somePath", personalisation.get("linkToPiPStartPage"));
        assertEquals("1234", personalisation.get("ccdCaseId"));
        assertEquals("", personalisation.get("securityCode"));
        assertEquals("", personalisation.get("validDate"));
        assertEquals("someUrl", personalisation.get("Hyperlink to service"));
    }

    @Test
    void getPersonalisation_with_details() {
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("2020-12-31"));
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

        Map<String, String> personalisation = appellantGeneratePinInPostPersonalisationSms.getPersonalisation(callback);

        assertEquals("someAppealNumber", personalisation.get("appealReferenceNumber"));
        assertEquals("someGivenName", personalisation.get("appellantGivenNames"));
        assertEquals("someFamilyName", personalisation.get("appellantFamilyName"));
        assertEquals("31 Dec 2020", personalisation.get("appellantDateOfBirth"));
        assertEquals("someUrl/somePath", personalisation.get("linkToPiPStartPage"));
        assertEquals("1234", personalisation.get("ccdCaseId"));
        assertEquals("someAccessCode", personalisation.get("securityCode"));
        assertEquals("31 Dec 2024", personalisation.get("validDate"));
        assertEquals("someUrl", personalisation.get("Hyperlink to service"));
    }
}