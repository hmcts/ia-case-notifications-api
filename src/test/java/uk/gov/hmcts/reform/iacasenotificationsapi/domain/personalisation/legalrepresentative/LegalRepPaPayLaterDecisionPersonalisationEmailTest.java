package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepPaPayLaterDecisionPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    private Long caseId = 12345L;
    private String paPayLaterDecisionTemplateId = "paPayLaterDecisionTemplateId";
    private String feeAmount = "4000.00";
    private String appealReferenceNumber = "appealReferenceNumber";
    private LegalRepPaPayLaterDecisionPersonalisationEmail legalRepPaPayLaterDecisionPersonalisationEmail;
    private String appellantEmail = "test@mail.com";

    @BeforeEach
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        legalRepPaPayLaterDecisionPersonalisationEmail = new LegalRepPaPayLaterDecisionPersonalisationEmail(
                paPayLaterDecisionTemplateId,
                recipientsFinder
        );
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_PA_PAY_LATER_DECISION_EMAIL",
                legalRepPaPayLaterDecisionPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_approved_template_id() {
        assertTrue(legalRepPaPayLaterDecisionPersonalisationEmail.getTemplateId(asylumCase).contains(paPayLaterDecisionTemplateId));
    }

    @Test
    void should_return_appellant_email_address_from_asylum_case() {
        Mockito.when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
                .thenReturn(Collections.singleton(appellantEmail));

        assertTrue(legalRepPaPayLaterDecisionPersonalisationEmail.getRecipientsList(asylumCase)
                .contains(appellantEmail));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
                () -> legalRepPaPayLaterDecisionPersonalisationEmail.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(FEE_AMOUNT_GBP, String.class))
                .thenReturn(Optional.of("400000"));

        Map<String, String> personalisation =
                legalRepPaPayLaterDecisionPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(feeAmount, personalisation.get("feeAmount"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
    }
}
