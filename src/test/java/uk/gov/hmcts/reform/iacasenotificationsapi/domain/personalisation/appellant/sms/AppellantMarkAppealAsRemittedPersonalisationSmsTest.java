package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SOURCE_OF_REMITTAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.REP;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.SourceOfRemittal;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantMarkAppealAsRemittedPersonalisationSmsTest {
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String templateId = "templateId";
    private final String appellantMobileNumber = "07777777777";
    private final SourceOfRemittal sourceOfRemittal = SourceOfRemittal.UPPER_TRIBUNAL;
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    private AppellantMarkAppealAsRemittedPersonalisationSms
        appellantMarkAppealAsRemittedPersonalisationSms;

    @BeforeEach
    public void setUp() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(SOURCE_OF_REMITTAL, SourceOfRemittal.class)).thenReturn(Optional.of(sourceOfRemittal));
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(appellantMobileNumber));

        appellantMarkAppealAsRemittedPersonalisationSms = new AppellantMarkAppealAsRemittedPersonalisationSms(
            templateId,
            recipientsFinder);
    }

    @Test
    public void should_return_given_email_address() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(AIP));

        assertEquals(Collections.singleton(appellantMobileNumber),
            appellantMarkAppealAsRemittedPersonalisationSms.getRecipientsList(asylumCase));

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(REP));

        assertEquals(Collections.emptySet(),
            appellantMarkAppealAsRemittedPersonalisationSms.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId,
            appellantMarkAppealAsRemittedPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_APPELLANT_MARK_APPEAL_AS_REMITTED_SMS",
            appellantMarkAppealAsRemittedPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation =
            appellantMarkAppealAsRemittedPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("remittalSource", sourceOfRemittal.getValue());

    }

    @Test
    public void should_throw_exception_when_callback_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantMarkAppealAsRemittedPersonalisationSms.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_throw_error_if_remittal_source_missing() {
        when(asylumCase.read(SOURCE_OF_REMITTAL, SourceOfRemittal.class)).thenReturn(Optional.empty());
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantMarkAppealAsRemittedPersonalisationSms.getPersonalisation(asylumCase));
        assertEquals("sourceOfRemittal is not present", exception.getMessage());
    }

}