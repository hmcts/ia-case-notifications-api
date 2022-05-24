package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailDirectionSentPersonalisation;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeBailDirectionSentPersonalisationTest {

    private Long caseId = 12345L;
    private String templateIdForDirectRecipient = "someTemplateIdForDirectRecipient";
    private String templateIdForOtherParties = "someTemplateIdForOtherParties";
    private String legalRepEmailAddress = "legalRep@example.com";
    private String sendDirectionDescription = "someDescriptionOfTheDirectionSent";
    private String dateOfCompliance = "2022-05-24";
    @Mock BailCase bailCase;

    private LegalRepresentativeBailDirectionSentPersonalisation legalRepresentativeBailSendDirectionPersonalisation;

    @BeforeEach
    public void setup() {

        when(bailCase.read(SEND_DIRECTION_DESCRIPTION, String.class)).thenReturn(Optional.of(sendDirectionDescription));
        when(bailCase.read(DATE_OF_COMPLIANCE, String.class)).thenReturn(Optional.of(dateOfCompliance));
        when(bailCase.read(LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.of(legalRepEmailAddress));
        when(bailCase.read(SEND_DIRECTION_LIST, String.class)).thenReturn(Optional.of("Legal Representative"));

        legalRepresentativeBailSendDirectionPersonalisation =
            new LegalRepresentativeBailDirectionSentPersonalisation(templateIdForDirectRecipient, templateIdForOtherParties);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateIdForDirectRecipient, legalRepresentativeBailSendDirectionPersonalisation.getTemplateId(bailCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_BAIL_SENT_DIRECTION_LEGAL_REPRESENTATIVE",
            legalRepresentativeBailSendDirectionPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> legalRepresentativeBailSendDirectionPersonalisation.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given_as_direct_recipient() {

        Map<String, String> personalisation =
            legalRepresentativeBailSendDirectionPersonalisation.getPersonalisation(bailCase);

        assertEquals(sendDirectionDescription, personalisation.get("sendDirectionDescription"));
        assertEquals(dateOfCompliance, personalisation.get("dateOfCompliance"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_as_other_party() {
        when(bailCase.read(SEND_DIRECTION_LIST, String.class)).thenReturn(Optional.of("Applicant"));

        Map<String, String> personalisation =
            legalRepresentativeBailSendDirectionPersonalisation.getPersonalisation(bailCase);

        assertEquals(sendDirectionDescription, personalisation.get("sendDirectionDescription"));
        assertEquals(dateOfCompliance, personalisation.get("dateOfCompliance"));
        assertEquals("Applicant", personalisation.get("party"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(SEND_DIRECTION_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(DATE_OF_COMPLIANCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(SEND_DIRECTION_LIST, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            legalRepresentativeBailSendDirectionPersonalisation.getPersonalisation(bailCase);

        assertEquals("", personalisation.get("sendDirectionDescription"));
        assertEquals("", personalisation.get("dateOfCompliance"));
        assertEquals("", personalisation.get("party"));
    }
}
