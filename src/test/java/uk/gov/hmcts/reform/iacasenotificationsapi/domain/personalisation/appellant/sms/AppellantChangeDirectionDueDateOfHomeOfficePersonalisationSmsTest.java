package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantChangeDirectionDueDateOfHomeOfficePersonalisationSmsTest {

    private final String smsTemplateId = "afterListingEmailTemplateId";
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String directionExplanation = "Some HO change direction due date content";
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
    private AppellantChangeDirectionDueDateOfHomeOfficePersonalisationSms appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms;

    @BeforeEach
    public void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms =
            new AppellantChangeDirectionDueDateOfHomeOfficePersonalisationSms(
                smsTemplateId,
                personalisationProvider,
                recipientsFinder
            );
    }

    @Test
    public void should_return_given_template_id_for_after_listing() {

        assertEquals(smsTemplateId, appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_APPELLANT_CHANGE_DIRECTION_DUE_DATE_OF_HOME_OFFICE_SMS",
            appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_list_from_subscribers_in_asylum_case() {

        String mockedAppellantMobilePhone = "07123456789";
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms.getRecipientsList(null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationForAppellant());

        Map<String, String> personalisation =
            appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", mockedAppealReferenceNumber)
            .containsEntry("dueDate", "8 Oct 2020")
            .containsEntry("explanation", directionExplanation);
    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationForAppellant());

        Map<String, String> personalisation =
            appellantChangeDirectionDueDateOfHomeOfficePersonalisationSms.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", "")
            .containsEntry("dueDate", "8 Oct 2020")
            .containsEntry("explanation", directionExplanation);
    }

    private Map<String, String> getPersonalisationForAppellant() {
        String dueDate = "2020-10-08";
        return ImmutableMap
            .<String, String>builder()
            .put("explanation", directionExplanation)
            .put("dueDate", LocalDate
                .parse(dueDate)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"))
            )
            .build();
    }

}
