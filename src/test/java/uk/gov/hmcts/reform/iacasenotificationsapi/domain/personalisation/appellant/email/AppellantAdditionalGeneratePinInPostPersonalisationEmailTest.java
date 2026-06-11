package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_PIN_IN_POST;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.GENERATE_PIP_EMAIL;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
class AppellantAdditionalGeneratePinInPostPersonalisationEmailTest {
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private Callback<AsylumCase> callback;

    private AppellantAdditionalGeneratePinInPostPersonalisationEmail appellantAdditionalGeneratePinInPostPersonalisationEmail;

    private final String templateId = "someTemplateId";

    @BeforeEach
    void setUp() {
        String aipSelfRepLink = "/somePath";
        String aipLink = "someUrl";
        appellantAdditionalGeneratePinInPostPersonalisationEmail = new AppellantAdditionalGeneratePinInPostPersonalisationEmail(
            templateId,
            aipSelfRepLink,
            aipLink,
            customerServicesProvider
        );
    }

    @Test
    void getRecipientsList_empty_if_no_email() {
        when(asylumCase.read(GENERATE_PIP_EMAIL, String.class))
            .thenReturn(Optional.empty());

        Set<String> recipientsList = appellantAdditionalGeneratePinInPostPersonalisationEmail.getRecipientsList(asylumCase);

        verify(asylumCase).read(GENERATE_PIP_EMAIL, String.class);
        assertTrue(recipientsList.isEmpty());
    }

    @Test
    void getRecipientsList_if_email() {
        String emailAddress = "someEmail";
        when(asylumCase.read(GENERATE_PIP_EMAIL, String.class))
            .thenReturn(Optional.of(emailAddress));

        Set<String> recipientsList = appellantAdditionalGeneratePinInPostPersonalisationEmail.getRecipientsList(asylumCase);

        verify(asylumCase).read(GENERATE_PIP_EMAIL, String.class);
        assertEquals(1, recipientsList.size());
        assertTrue(recipientsList.contains(emailAddress));
    }

    @Test
    void getTemplateId() {
        assertEquals(templateId, appellantAdditionalGeneratePinInPostPersonalisationEmail.getTemplateId());
    }

    @Test
    void getReferenceId() {
        assertEquals("12345_GENERATED_PIN_IN_POST_APPELLANT_2_EMAIL",
            appellantAdditionalGeneratePinInPostPersonalisationEmail.getReferenceId(12345L));
    }

    @Test
    void getPersonalisation_with_no_details() {
        when(customerServicesProvider.getCustomerServicesPersonalisation(callback)).thenReturn(Map.of(
            "customerServicesTelephone", "someCustomerServicesTelephone",
            "customerServicesEmail", "someCustomerServicesEmail"));
        initializePrefixes(appellantAdditionalGeneratePinInPostPersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_PIN_IN_POST, PinInPostDetails.class)).thenReturn(Optional.empty());
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(1234L);

        Map<String, String> personalisation = appellantAdditionalGeneratePinInPostPersonalisationEmail.getPersonalisation(callback);

        verify(asylumCase).clear(GENERATE_PIP_EMAIL);
        assertEquals("Accelerated detained appeal", personalisation.get("subjectPrefix"));
        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("", personalisation.get("appellantDateOfBirth"));
        assertEquals("someUrl/somePath", personalisation.get("linkToPiPStartPage"));
        assertEquals("1234", personalisation.get("ccdCaseId"));
        assertEquals("", personalisation.get("securityCode"));
        assertEquals("", personalisation.get("validDate"));
        assertEquals("someUrl", personalisation.get("Hyperlink to service"));
        assertEquals("someCustomerServicesTelephone", personalisation.get("customerServicesTelephone"));
        assertEquals("someCustomerServicesEmail", personalisation.get("customerServicesEmail"));
    }

    @Test
    void getPersonalisation_with_details() {
        initializePrefixes(appellantAdditionalGeneratePinInPostPersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("2020-12-31"));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("someAppealNumber"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("someGivenName"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("someFamilyName"));
        when(caseDetails.getId()).thenReturn(1234L);
        when(asylumCase.read(APPELLANT_PIN_IN_POST, PinInPostDetails.class)).thenReturn(Optional.of(
            PinInPostDetails.builder()
                .accessCode("someAccessCode")
                .expiryDate("2024-12-31")
                .build()
        ));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        Map<String, String> personalisation = appellantAdditionalGeneratePinInPostPersonalisationEmail.getPersonalisation(callback);

        verify(asylumCase).clear(GENERATE_PIP_EMAIL);
        assertEquals("Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
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

    @Test
    void should_throw_exception_on_personalisation_when_callback_is_null() {
        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> appellantAdditionalGeneratePinInPostPersonalisationEmail.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_personalisation_when_asylumCase_is_null() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(null);
        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> appellantAdditionalGeneratePinInPostPersonalisationEmail.getPersonalisation(callback));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}