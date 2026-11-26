package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantRespondQueryPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;

    @Mock
    RecipientsFinder recipientsFinder;

    private final String appealReferenceNumber = "someReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String aipFrontendUrl = "[somefrontendurl](http://somefrontendurl)";
    private final String templateId = "someTemplateId";

    private AppellantRespondQueryPersonalisationEmail appellantRespondQueryPersonalisationEmail;

    @BeforeEach
    public void setUp() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        appellantRespondQueryPersonalisationEmail = new AppellantRespondQueryPersonalisationEmail(
            templateId,
            aipFrontendUrl,
            recipientsFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, appellantRespondQueryPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_correct_reference() {
        assertEquals("1234_AIP_TRIBUNAL_RESPONDED_QUERY_EMAIL_NOTIFICATION", appellantRespondQueryPersonalisationEmail.getReferenceId(1234L));
    }

    @Test
    public void should_return_from_recipient_finder() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL)).thenReturn(Set.of("email@example.com"));
        appellantRespondQueryPersonalisationEmail.getRecipientsList(asylumCase);

        verify(recipientsFinder).findAll(asylumCase, NotificationType.EMAIL);
        verify(recipientsFinder, never()).findAll(asylumCase, NotificationType.SMS);
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation =
            appellantRespondQueryPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(aipFrontendUrl, personalisation.get("linkToOnlineService"));
        String viewMessagesUrl = aipFrontendUrl + "/view-messages";
        assertEquals(viewMessagesUrl, personalisation.get("viewMessagesUrl"));
    }

    @Test
    public void should_throw_exception_on_personalisation_asylum_case_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantRespondQueryPersonalisationEmail.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}
