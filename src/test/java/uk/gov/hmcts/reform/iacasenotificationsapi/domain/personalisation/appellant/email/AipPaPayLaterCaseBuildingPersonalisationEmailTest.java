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
    private String amountLeftToPay = "4000";
    private String amountLeftToPayInGbp = "40.00";
    private String someTestDateEmail = "14/14/2024";
    private AipPaPayLaterCaseBuildingPersonalisationEmail aipPaPayLaterCaseBuildingPersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));

        aipPaPayLaterCaseBuildingPersonalisationEmail = new aipPaPayLaterCaseBuildingPersonalisationEmail(
                paPayLaterCaseBuildingTemplateId,
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

        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(onlineCaseReferenceNumber));
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of(amountLeftToPay));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));

        Map<String, String> personalisation =
                aipPaPayLaterCaseBuildingPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(amountLeftToPayInGbp, personalisation.get("feeAmount"));
    }
}