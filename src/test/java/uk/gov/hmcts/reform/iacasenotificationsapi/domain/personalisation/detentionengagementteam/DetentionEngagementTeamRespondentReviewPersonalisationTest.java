package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamRespondentReviewPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    private DirectionFinder directionFinder;
    @Mock
    private DetEmailService detEmailService;
    @Mock
    Direction direction;

    private final Long caseId = 12345L;
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String ariaListingReference = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String detentionEngagementTeamRespondentReviewTemplateId = "detentionEngagementTeamRespondentReviewTemplateId";
    private final String detentionEngagementTeamEmail = "det@email.com";
    private final String directionDueDate = "2023-02-02";

    private DetentionEngagementTeamRespondentReviewPersonalisation detentionEngagementTeamRespondentReviewPersonalisation;

    @BeforeEach
    void setup() {
        detentionEngagementTeamRespondentReviewPersonalisation = new DetentionEngagementTeamRespondentReviewPersonalisation(
                detentionEngagementTeamRespondentReviewTemplateId,
                directionFinder,
                detEmailService
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(detentionEngagementTeamRespondentReviewTemplateId, detentionEngagementTeamRespondentReviewPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_DENTENTION_ENGAGEMENT_TEAM_REQUEST_RESPONDENT_REVIEW",
                detentionEngagementTeamRespondentReviewPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_det_email_address() {
        when(detEmailService.getDetEmailAddress(asylumCase)).thenReturn(detentionEngagementTeamEmail);

        assertTrue(
                detentionEngagementTeamRespondentReviewPersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
                () -> detentionEngagementTeamRespondentReviewPersonalisation.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));

        when((direction.getDateDue())).thenReturn(directionDueDate);
        when(directionFinder.findFirst(asylumCase, DirectionTag.RESPONDENT_REVIEW))
                .thenReturn(Optional.of(direction));


        final Map<String, String> expectedPersonalisation =
                ImmutableMap
                        .<String, String>builder()
                        .put("appealReferenceNumber", appealReferenceNumber)
                        .put("ariaListingReference", ariaListingReference)
                        .put("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
                        .put("appellantGivenNames", appellantGivenNames)
                        .put("appellantFamilyName", appellantFamilyName)
                        .put("insertDate", directionDueDate)
                        .build();

        Map<String, String> actualPersonalisation =
                detentionEngagementTeamRespondentReviewPersonalisation.getPersonalisation(asylumCase);

        assertThat(actualPersonalisation).isEqualToComparingOnlyGivenFields(expectedPersonalisation);
    }
}

