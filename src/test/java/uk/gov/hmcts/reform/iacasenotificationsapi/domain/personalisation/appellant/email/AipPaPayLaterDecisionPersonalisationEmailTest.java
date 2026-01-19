package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipPaPayLaterDecisionPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    private Long caseId = 12345L;
    private String aipPaPayLaterCDecisionTemplateId = "aipPaPayLaterDecisionTemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private String feeAmount = "400000";
    private String someTestDateEmail = "14/14/2024";
    private AipPaPayLaterDecisionPersonalisationEmail aipPaPayLaterDecisionPersonalisationEmail;

    @BeforeEach
    public void setup() {

        aipPaPayLaterDecisionPersonalisationEmail = new AipPaPayLaterDecisionPersonalisationEmail(
                aipPaPayLaterCDecisionTemplateId,
                iaAipFrontendUrl,
                recipientsFinder
        );
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_AIP_PA_PAY_LATER_DECISION_EMAIL",
                aipPaPayLaterDecisionPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(FEE_AMOUNT_GBP, String.class))
                .thenReturn(Optional.of("400000"));

        Map<String, String> personalisation =
                aipPaPayLaterDecisionPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals("4000.00", personalisation.get("feeAmount"));
    }
}