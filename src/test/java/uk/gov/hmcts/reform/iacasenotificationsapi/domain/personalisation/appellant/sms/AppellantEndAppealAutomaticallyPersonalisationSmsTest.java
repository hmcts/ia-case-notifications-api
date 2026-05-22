package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantEndAppealAutomaticallyPersonalisationSmsTest {

    private final String smsTemplateId = "someSmsTemplateId";
    private final int daysToAskReinstate = 14;
    private final String iaAipFrontendUrl = "http://localhost/";
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    private AppellantEndAppealAutomaticallyPersonalisationSms appellantEndAppealAutomaticallyPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantEndAppealAutomaticallyPersonalisationSms = new AppellantEndAppealAutomaticallyPersonalisationSms(
            smsTemplateId,
            daysToAskReinstate,
            iaAipFrontendUrl,
            recipientsFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(smsTemplateId, appellantEndAppealAutomaticallyPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_APPEAL_ENDED_AUTOMATICALLY_APPELLANT_SMS",
            appellantEndAppealAutomaticallyPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantEndAppealAutomaticallyPersonalisationSms.getRecipientsList(null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        String mockedAppellantMobilePhone = "07123456789";
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantEndAppealAutomaticallyPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantEndAppealAutomaticallyPersonalisationSms.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        String endAppealDate = LocalDate.now().toString();
        when(asylumCase.read(AsylumCaseDefinition.END_APPEAL_DATE, String.class)).thenReturn(Optional.of(endAppealDate));

        Map<String, String> personalisation =
            appellantEndAppealAutomaticallyPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", mockedAppealReferenceNumber)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl)
            .containsEntry("deadLine", LocalDate.parse(endAppealDate).plusDays(daysToAskReinstate).format(DateTimeFormatter.ofPattern("d MMM yyyy")));
    }
}
