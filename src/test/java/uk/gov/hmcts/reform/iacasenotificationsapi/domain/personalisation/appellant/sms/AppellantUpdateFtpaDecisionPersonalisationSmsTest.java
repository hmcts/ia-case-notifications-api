package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
class AppellantUpdateFtpaDecisionPersonalisationSmsTest {

    private final Long caseId = 12345L;
    private final String templateId = "someTemplateId";
    private final String iaAipFrontendUrl = "http://localhost/aip";
    private final String appealReferenceNumber = "PA/12345/2026";

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;

    private AppellantUpdateFtpaDecisionPersonalisationSms personalisation;

    @BeforeEach
    void setUp() {
        personalisation = new AppellantUpdateFtpaDecisionPersonalisationSms(templateId, iaAipFrontendUrl, recipientsFinder);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, personalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_UPDATE_FTPA_DECISION_APPELLANT_SMS", personalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_appellant_mobile_recipients() {
        Set<String> mobiles = Collections.singleton("07700900000");
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS)).thenReturn(mobiles);

        assertEquals(mobiles, personalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));

        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertThat(result)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("linkToService", iaAipFrontendUrl)
            .hasSize(2);
    }

    @Test
    void should_return_empty_appeal_reference_when_missing() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertThat(personalisation.getPersonalisation(asylumCase)).containsEntry("appealReferenceNumber", "");
    }

    @Test
    void should_throw_exception_when_asylum_case_is_null() {
        assertThrows(NullPointerException.class, () -> personalisation.getPersonalisation((AsylumCase) null));
    }
}
