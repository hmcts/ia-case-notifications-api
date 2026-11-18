package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

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
public class AppellantRespondQueryPersonalisationSmsTest {

    private final String appealReferenceNumber = "someReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String aipFrontendUrl = "[somefrontendurl](http://somefrontendurl)";
    private final String templateId = "someTemplateId";

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    RecipientsFinder recipientsFinder;
    private AppellantRespondQueryPersonalisationSms appellantRespondQueryPersonalisationSms;

    @BeforeEach
    void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        appellantRespondQueryPersonalisationSms = new AppellantRespondQueryPersonalisationSms(
            templateId,
            aipFrontendUrl,
            recipientsFinder
        );
    }

    @Test
    public void should_return_correct_template_id() {
        assertEquals(templateId, appellantRespondQueryPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals("1234_AIP_TRIBUNAL_RESPONDED_QUERY_SMS_NOTIFICATION",
            appellantRespondQueryPersonalisationSms.getReferenceId(1234L));
    }


    @Test
    public void should_return_from_recipient_finder() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS)).thenReturn(Set.of("sms@example.com"));
        appellantRespondQueryPersonalisationSms.getRecipientsList(asylumCase);

        verify(recipientsFinder).findAll(asylumCase, NotificationType.SMS);
        verify(recipientsFinder, never()).findAll(asylumCase, NotificationType.EMAIL);
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation =
            appellantRespondQueryPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(aipFrontendUrl, personalisation.get("linkToOnlineService"));
        String viewMessagesUrl = aipFrontendUrl + "/view-queries";
        assertEquals(viewMessagesUrl, personalisation.get("viewMessagesUrl"));
    }

    @Test
    public void should_throw_exception_on_personalisation_asylum_case_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantRespondQueryPersonalisationSms.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}
