package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.AMOUNT_LEFT_TO_PAY;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipPaPayLaterCaseBuildingPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    private Long caseId = 12345L;
    private String aipPaPayLaterCaseBuildingTemplateId = "aipPaPayLaterCaseBuildingTemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private String feeAmount = "400000";
    private String someTestDateEmail = "14/14/2024";
    private AipPaPayLaterCaseBuildingPersonalisationEmail aipPaPayLaterCaseBuildingPersonalisationEmail;

    @BeforeEach
    public void setup() {

        aipPaPayLaterCaseBuildingPersonalisationEmail = new AipPaPayLaterCaseBuildingPersonalisationEmail(
                aipPaPayLaterCaseBuildingTemplateId,
                iaAipFrontendUrl,
                recipientsFinder
        );
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_AIP_PA_PAY_LATER_CASE_BUILDING_EMAIL",
                aipPaPayLaterCaseBuildingPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(FEE_AMOUNT_GBP, String.class))
                .thenReturn(Optional.of("400000"));

        Map<String, String> personalisation =
                aipPaPayLaterCaseBuildingPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals("4000.00", personalisation.get("feeAmount"));
    }

}