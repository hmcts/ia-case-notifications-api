package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantRequestCmaRequirementsPersonalisationSmsTest {

    private final String smsTemplateId = "someSmsTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String explanation = "You must do this because...";
    private final String expectedDirectionDueDate = "27 Aug 2019";
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    DirectionFinder directionFinder;
    @Mock
    Direction direction;
    private AppellantRequestCmaRequirementsPersonalisationSms appellantRequestCmaRequirementsPersonalisationSms;

    @BeforeEach
    public void setup() {


        when((direction.getExplanation())).thenReturn(explanation);
        String directionDueDate = "2019-08-27";
        when((direction.getDateDue())).thenReturn(directionDueDate);
        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_CMA_REQUIREMENTS))
            .thenReturn(Optional.of(direction));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantRequestCmaRequirementsPersonalisationSms = new AppellantRequestCmaRequirementsPersonalisationSms(
            smsTemplateId,
            iaAipFrontendUrl,
            recipientsFinder,
            directionFinder);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(smsTemplateId, appellantRequestCmaRequirementsPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_REQUEST_CMA_REQUIREMENTS_APPELLANT_AIP_SMS",
            appellantRequestCmaRequirementsPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantRequestCmaRequirementsPersonalisationSms.getRecipientsList(null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_direction_is_empty() {

        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_CMA_REQUIREMENTS)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantRequestCmaRequirementsPersonalisationSms.getPersonalisation(asylumCase));
        assertEquals("direction 'requestCmaRequirements' is not present", exception.getMessage());
    }


    @Test
    public void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        String mockedAppellantMobilePhone = "07123456789";
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantRequestCmaRequirementsPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantRequestCmaRequirementsPersonalisationSms.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            appellantRequestCmaRequirementsPersonalisationSms.getPersonalisation(asylumCase);
        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", mockedAppealReferenceNumber)
            .containsEntry("reason", explanation)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl)
            .containsEntry("due date", expectedDirectionDueDate);

    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            appellantRequestCmaRequirementsPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", "")
            .containsEntry("reason", explanation)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl)
            .containsEntry("due date", expectedDirectionDueDate);
    }
}
