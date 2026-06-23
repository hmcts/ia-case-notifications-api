package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepPaPayLaterCaseBuildingPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    private Long caseId = 12345L;
    private String legalRepPaPayLaterCaseBuildingTemplateId = "LegalRepPaPayLaterCaseBuildingTemplateId";
    private String feeAmount = "4000.00";
    private String appealReferenceNumber = "appealReferenceNumber";
    private LegalRepPaPayLaterCaseBuildingPersonalisationEmail legalRepPaPayLaterCaseBuildingPersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));

        legalRepPaPayLaterCaseBuildingPersonalisationEmail = new LegalRepPaPayLaterCaseBuildingPersonalisationEmail(
                legalRepPaPayLaterCaseBuildingTemplateId
        );
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_LR_PA_PAY_LATER_CASE_BUILDING_EMAIL",
                legalRepPaPayLaterCaseBuildingPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_approved_template_id() {
        assertTrue(legalRepPaPayLaterCaseBuildingPersonalisationEmail.getTemplateId(asylumCase).contains(legalRepPaPayLaterCaseBuildingTemplateId));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
                () -> legalRepPaPayLaterCaseBuildingPersonalisationEmail.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(FEE_AMOUNT_GBP, String.class))
                .thenReturn(Optional.of("400000"));

        Map<String, String> personalisation =
                legalRepPaPayLaterCaseBuildingPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(feeAmount, personalisation.get("feeAmount"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
    }

}
