package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.SourceOfRemittal;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.INTERNAL_APPELLANT_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SOURCE_OF_REMITTAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.REP;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantMarkAppealAsRemittedNonDetainedPersonalisationSmsTest {
    private final String iaAipFrontendUrl = "https://aip-url";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String ccdReferenceNumber = "0000 0000 0000 0001";
    private final String templateId = "templateId";
    private final String appellantMobileNumber = "07777777777";
    private final SourceOfRemittal sourceOfRemittal = SourceOfRemittal.UPPER_TRIBUNAL;
    private final String securityCode = "securityCode";
    @Mock
    AsylumCase asylumCase;
    @Mock
    PinInPostDetails pinInPostDetails;
    private AppellantMarkAppealAsRemittedNonDetainedPersonalisationSms
        appellantMarkAppealAsRemittedNonDetainedPersonalisationSms;

    @BeforeEach
    public void setUp() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(SOURCE_OF_REMITTAL, SourceOfRemittal.class)).thenReturn(Optional.of(sourceOfRemittal));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(ccdReferenceNumber));
        when(asylumCase.read(INTERNAL_APPELLANT_MOBILE_NUMBER, String.class)).thenReturn(Optional.ofNullable(appellantMobileNumber));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.class)).thenReturn(Optional.of(pinInPostDetails));
        when(pinInPostDetails.getAccessCode()).thenReturn(securityCode);
        String validDate = "2024-03-01";
        when(pinInPostDetails.getExpiryDate()).thenReturn(validDate);

        appellantMarkAppealAsRemittedNonDetainedPersonalisationSms = new AppellantMarkAppealAsRemittedNonDetainedPersonalisationSms(
            templateId,
            iaAipFrontendUrl);
    }

    @Test
    public void should_return_given_email_address() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(AIP));

        assertEquals(Collections.singleton(appellantMobileNumber),
            appellantMarkAppealAsRemittedNonDetainedPersonalisationSms.getRecipientsList(asylumCase));

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(REP));
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId,
            appellantMarkAppealAsRemittedNonDetainedPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_APPELLANT_MARK_APPEAL_AS_REMITTED_NON_DETAINED_APPELLANT_SMS",
            appellantMarkAppealAsRemittedNonDetainedPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation =
            appellantMarkAppealAsRemittedNonDetainedPersonalisationSms.getPersonalisation(asylumCase);

        String validDateShown = "1 Mar 2024";
        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("remittalSource", sourceOfRemittal.getValue())
            .containsEntry("urlLink", iaAipFrontendUrl)
            .containsEntry("ccdRefNumber", ccdReferenceNumber)
            .containsEntry("securityCode", securityCode)
            .containsEntry("expirationDate", validDateShown);
    }

    @Test
    public void should_throw_exception_when_callback_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantMarkAppealAsRemittedNonDetainedPersonalisationSms.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_throw_error_if_remittal_source_missing() {
        when(asylumCase.read(SOURCE_OF_REMITTAL, SourceOfRemittal.class)).thenReturn(Optional.empty());
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantMarkAppealAsRemittedNonDetainedPersonalisationSms.getPersonalisation(asylumCase));
        assertEquals("sourceOfRemittal is not present", exception.getMessage());
    }

}