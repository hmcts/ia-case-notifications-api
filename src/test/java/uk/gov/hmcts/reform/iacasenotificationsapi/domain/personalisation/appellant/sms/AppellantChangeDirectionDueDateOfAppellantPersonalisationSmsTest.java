package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantChangeDirectionDueDateOfAppellantPersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    PersonalisationProvider personalisationProvider;

    private final String smsTemplateId = "afterListingEmailTemplateId";

    private final String mockedAppealReferenceNumber = "someReferenceNumber";

    private AppellantChangeDirectionDueDateOfAppellantPersonalisationSms appellantChangeDirectionDueDateOfAppellantPersonalisationSms;
    private final String iaAipFrontendUrl = "iaAipFrontendUrl";

    @BeforeEach
    public void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantChangeDirectionDueDateOfAppellantPersonalisationSms =
            new AppellantChangeDirectionDueDateOfAppellantPersonalisationSms(
                smsTemplateId,
                iaAipFrontendUrl,
                recipientsFinder
            );
    }

    @Test
    public void should_return_given_template_id() {

        assertEquals(smsTemplateId, appellantChangeDirectionDueDateOfAppellantPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_APPELLANT_CHANGE_DIRECTION_DUE_DATE_OF_APPELLANT_SMS",
            appellantChangeDirectionDueDateOfAppellantPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        String mockedAppellantMobilePhone = "07123456789";
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantChangeDirectionDueDateOfAppellantPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        NullPointerException exception =
assertThrows(NullPointerException.class, () -> appellantChangeDirectionDueDateOfAppellantPersonalisationSms.getRecipientsList(null))
            ;
assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation() {

        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationForAppellant());

        Map<String, String> personalisation =
            appellantChangeDirectionDueDateOfAppellantPersonalisationSms.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", mockedAppealReferenceNumber)
            .containsEntry("linkToService", iaAipFrontendUrl);

    }

    private Map<String, String> getPersonalisationForAppellant() {
        String dueDate = "2020-10-08";
        String directionExplanation = "Some HO change direction due date content";
        return ImmutableMap
            .<String, String>builder()
            .put("linkToService", iaAipFrontendUrl)
            .put("explanation", directionExplanation)
            .put("dueDate", LocalDate
                .parse(dueDate)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"))
            )
            .build();
    }

}
