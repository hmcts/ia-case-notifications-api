package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.List;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailDirection;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeBailDirectionSentPersonalisationTest {

    private Long caseId = 12345L;
    private String templateIdForDirectRecipient = "someTemplateIdForDirectRecipient";
    private String templateIdForOtherParties = "someTemplateIdForOtherParties";
    private String homeOfficeEmailAddress = "HO_user@example.com";
    private String sendDirectionDescription = "someDescriptionOfTheDirectionSent";
    private String dateOfCompliance = "2022-05-24";
    @Mock BailCase bailCase;
    @Mock IdValue<BailDirection> oldestDirectionIdValue;
    @Mock IdValue<BailDirection> newestDirectionIdValue;
    @Mock BailDirection newestDirection;

    private HomeOfficeBailDirectionSentPersonalisation homeOfficeBailDirectionSentPersonalisation;

    @BeforeEach
    public void setup() {

        when(oldestDirectionIdValue.getId()).thenReturn("1");
        when(newestDirectionIdValue.getId()).thenReturn("2");
        when(newestDirectionIdValue.getValue()).thenReturn(newestDirection);
        when(bailCase.read(DIRECTIONS)).thenReturn(Optional.of(List.of(oldestDirectionIdValue, newestDirectionIdValue)));

        when(newestDirection.getSendDirectionDescription()).thenReturn(sendDirectionDescription);
        when(newestDirection.getDateOfCompliance()).thenReturn(dateOfCompliance);
        when(newestDirection.getSendDirectionList()).thenReturn("Home Office");

        homeOfficeBailDirectionSentPersonalisation =
            new HomeOfficeBailDirectionSentPersonalisation(templateIdForDirectRecipient, templateIdForOtherParties, homeOfficeEmailAddress);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateIdForDirectRecipient, homeOfficeBailDirectionSentPersonalisation.getTemplateId(bailCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_BAIL_SENT_DIRECTION_HOME_OFFICE",
            homeOfficeBailDirectionSentPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> homeOfficeBailDirectionSentPersonalisation.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given_as_direct_recipient() {

        Map<String, String> personalisation =
            homeOfficeBailDirectionSentPersonalisation.getPersonalisation(bailCase);

        assertEquals(sendDirectionDescription, personalisation.get("sendDirectionDescription"));
        assertEquals(dateOfCompliance, personalisation.get("dateOfCompliance"));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_as_other_party() {
        when(newestDirection.getSendDirectionList()).thenReturn("Applicant");

        Map<String, String> personalisation =
            homeOfficeBailDirectionSentPersonalisation.getPersonalisation(bailCase);

        assertEquals(sendDirectionDescription, personalisation.get("sendDirectionDescription"));
        assertEquals(dateOfCompliance, personalisation.get("dateOfCompliance"));
        assertEquals("Applicant", personalisation.get("party"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(DIRECTIONS)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            homeOfficeBailDirectionSentPersonalisation.getPersonalisation(bailCase);

        assertEquals("", personalisation.get("sendDirectionDescription"));
        assertEquals("", personalisation.get("dateOfCompliance"));
        assertEquals("", personalisation.get("party"));
    }
}
