package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

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
class PaPayLaterCaseBuildingPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    private Long caseId = 12345L;
    private String paPayLaterCaseBuildingTemplateId = "PaPayLaterCaseBuildingTemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private String feeAmount = "400000";
    private String someTestDateSms = "14/14/2024";
    private PaPayLaterCaseBuildingPersonalisationSms paPayLaterCaseBuildingPersonalisationSms;

    @BeforeEach
    public void setup() {

        paPayLaterCaseBuildingPersonalisationSms = new PaPayLaterCaseBuildingPersonalisationSms(
                paPayLaterCaseBuildingTemplateId,
                iaAipFrontendUrl,
                recipientsFinder
        );
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_PA_PAY_LATER_CASE_BUILDING_SMS",
                paPayLaterCaseBuildingPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(FEE_AMOUNT_GBP, String.class))
                .thenReturn(Optional.of("400000"));

        Map<String, String> personalisation =
                paPayLaterCaseBuildingPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals("4000.00", personalisation.get("feeAmount"));
    }

}