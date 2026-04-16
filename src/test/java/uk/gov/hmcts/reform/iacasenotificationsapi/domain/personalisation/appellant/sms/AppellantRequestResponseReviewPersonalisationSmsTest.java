package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;


@ExtendWith(MockitoExtension.class)
class AppellantRequestResponseReviewPersonalisationSmsTest {


    private final String requestResponseReviewWithdrawnTemplateId = "requestResponseReviewWithdrawnTemplateId";
    private final String maintainedResponseReviewDirectionTemplateId = "maintainedResponseReviewDirectionTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final String designatedHearingCentre = "belfast@hearingcentre.gov";
    private final String directionDueDate = "2019-08-27";
    private final String expectedDirectionDueDate = "27 Aug 2019";
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    DirectionFinder directionFinder;
    @Mock
    Direction direction;
    private AppellantRequestResponseReviewPersonalisationSms
        appellantRequestResponseReviewPersonalisationSms;

    @BeforeEach
    void setUp() {

        appellantRequestResponseReviewPersonalisationSms =
            new AppellantRequestResponseReviewPersonalisationSms(
                requestResponseReviewWithdrawnTemplateId, maintainedResponseReviewDirectionTemplateId,
                iaAipFrontendUrl, emailAddressFinder, recipientsFinder, directionFinder);

    }

    @Test
    void should_return_personalisation_when_all_information_given_before_listing() {

        when((direction.getDateDue())).thenReturn(directionDueDate);
        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_RESPONSE_REVIEW))
            .thenReturn(Optional.of(direction));

        String appealReferenceNumber = "someReferenceNumber";
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(emailAddressFinder.getHearingCentreEmailAddress(asylumCase)).thenReturn(designatedHearingCentre);

        Map<String, String> personalisation =
            appellantRequestResponseReviewPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", appealReferenceNumber)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl)
            .containsEntry("designated hearing centre", designatedHearingCentre)
            .containsEntry("dueDate", expectedDirectionDueDate);
        verify(emailAddressFinder).getHearingCentreEmailAddress(asylumCase);
    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when((direction.getDateDue())).thenReturn(directionDueDate);
        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_RESPONSE_REVIEW))
            .thenReturn(Optional.of(direction));

        when(emailAddressFinder.getHearingCentreEmailAddress(asylumCase)).thenReturn(designatedHearingCentre);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            appellantRequestResponseReviewPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", "")
            .containsEntry("Hyperlink to service", iaAipFrontendUrl)
            .containsEntry("designated hearing centre", designatedHearingCentre)
            .containsEntry("dueDate", expectedDirectionDueDate);
        verify(emailAddressFinder).getHearingCentreEmailAddress(asylumCase);
    }

    @Test
    public void should_return_given_template_id_for_decision_withdrawn() {

        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class))
            .thenReturn(Optional.of(AppealReviewOutcome.DECISION_WITHDRAWN));

        assertEquals(requestResponseReviewWithdrawnTemplateId, appellantRequestResponseReviewPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_empty_template_id_for_decision_withdrawn() {

        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class))
            .thenReturn(Optional.of(AppealReviewOutcome.DECISION_MAINTAINED));

        assertEquals(maintainedResponseReviewDirectionTemplateId, appellantRequestResponseReviewPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_throw_exception_on_missing_appeal_review_outcome() {

        IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class,
                () -> appellantRequestResponseReviewPersonalisationSms.getTemplateId(asylumCase));
        assertEquals("AppealReviewOutcome not present", exception.getMessage());
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_REQUEST_RESPONSE_REVIEW_AIP_SMS",
            appellantRequestResponseReviewPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        String mockedAppellantMobileNumber = "1234445556";
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobileNumber));

        assertTrue(appellantRequestResponseReviewPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobileNumber));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantRequestResponseReviewPersonalisationSms.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}
