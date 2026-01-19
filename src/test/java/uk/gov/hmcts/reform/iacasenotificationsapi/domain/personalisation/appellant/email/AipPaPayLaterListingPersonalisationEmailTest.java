package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.AMOUNT_LEFT_TO_PAY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision.PARTIALLY_APPROVED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision.REJECTED;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipPaPayLaterListingPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    private Long caseId = 12345L;
    private String aipPaPayLaterListingTemplateId = "aipPaPayLaterListingTemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private String feeAmount = "40.00";
    private String someTestDateEmail = "14/14/2024";
    private AipPaPayLaterListingPersonalisationEmail aipPaPayLaterListingPersonalisationEmail;

    @BeforeEach
    public void setup() {

        aipPaPayLaterListingPersonalisationEmail = new AipPaPayLaterListingPersonalisationEmail(
                paPayLaterListingTemplateId,
                iaAipFrontendUrl,
                recipientsFinder
        );
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_AIP_PA_PAY_LATER_LISTING_EMAIL",
                aipPaPayLaterListingPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));

        Map<String, String> personalisation =
                aipPaPayLaterListingPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(feeAmount, personalisation.get("feeAmount"));
    }
}