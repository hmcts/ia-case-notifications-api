package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantForceCaseProgressionToCaseUnderReviewPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String appellantEmailAddress = "appellantp@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private AppellantForceCaseProgressionToCaseUnderReviewPersonalisationEmail
        forceCaseProgressionToCaseUnderReviewPersonalisation;

    @BeforeEach
    void setUp() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(appellantEmailAddress));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        forceCaseProgressionToCaseUnderReviewPersonalisation =
            new AppellantForceCaseProgressionToCaseUnderReviewPersonalisationEmail(templateId);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, forceCaseProgressionToCaseUnderReviewPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_FORCE_CASE_TO_CASE_UNDER_REVIEW_AIP",
            forceCaseProgressionToCaseUnderReviewPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(forceCaseProgressionToCaseUnderReviewPersonalisation.getRecipientsList(asylumCase)
            .contains(appellantEmailAddress));
    }

    @Test
    public void should_throw_exception_when_appellant_email_is_not_present() {
        when(asylumCase.read(APPELLANT_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> forceCaseProgressionToCaseUnderReviewPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appellantEmailAddress is not present");
    }
}