package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;


@ExtendWith(MockitoExtension.class)
class AppellantConfirmHomeOfficeReviewMaintained24WeeksPersonalisationSmsTest {


    private final String confirmHOReviewMaintainedTemplateId = "confirmHOReviewMaintainedTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    
    private AppellantConfirmHomeOfficeReviewMaintained24WeeksPersonalisationSms
        appellantConfirmHomeOfficeReviewMaintained24WeeksPersonalisationSms;

    @BeforeEach
    void setUp() {

        appellantConfirmHomeOfficeReviewMaintained24WeeksPersonalisationSms =
            new AppellantConfirmHomeOfficeReviewMaintained24WeeksPersonalisationSms(
                confirmHOReviewMaintainedTemplateId, iaAipFrontendUrl, recipientsFinder);

    }

    @Test
    public void should_return_personalisation_when_all_information_given_before_listing() {
        
        String appealReferenceNumber = "someReferenceNumber";
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));

        Map<String, String> personalisation =
            appellantConfirmHomeOfficeReviewMaintained24WeeksPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", appealReferenceNumber)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl);
    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            appellantConfirmHomeOfficeReviewMaintained24WeeksPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", "")
            .containsEntry("Hyperlink to service", iaAipFrontendUrl);
    }

    @Test
    public void should_return_given_template_id_for_decision_maintained() {
        assertEquals(confirmHOReviewMaintainedTemplateId, appellantConfirmHomeOfficeReviewMaintained24WeeksPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_STATUTORY_TIMEFRAME_24WEEKS_CONFIRM_HO_REVIEW_MAINTAINED_AIP_SMS",
            appellantConfirmHomeOfficeReviewMaintained24WeeksPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        String mockedAppellantMobileNumber = "1234445556";
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobileNumber));

        assertTrue(appellantConfirmHomeOfficeReviewMaintained24WeeksPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobileNumber));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantConfirmHomeOfficeReviewMaintained24WeeksPersonalisationSms.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}
