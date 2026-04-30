package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RespondentFtpaSubmittedPersonalisationTest {

    private final String templateId = "templateId";
    private final String iaExUiFrontendUrl = "http://localhost";
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    EmailAddressFinder emailAddressFinder;
    private RespondentFtpaSubmittedPersonalisation respondentFtpaSubmittedPersonalisation;

    @BeforeEach
    public void setup() {

        respondentFtpaSubmittedPersonalisation = new RespondentFtpaSubmittedPersonalisation(
            templateId,
            iaExUiFrontendUrl,
            personalisationProvider,
            customerServicesProvider,
            emailAddressFinder
        );
    }

    @Test
    public void should_return_give_reference_id() {

        Long caseId = 12345L;
        assertEquals(caseId + "_FTPA_SUBMITTED_RESPONDENT",
            respondentFtpaSubmittedPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, respondentFtpaSubmittedPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_recipient_email_id() {
        String respondentEmailAddress = "respondent@example.com";
        when(emailAddressFinder.getListCaseFtpaHomeOfficeEmailAddress(asylumCase)).thenReturn(respondentEmailAddress);
        assertEquals(Collections.singleton(respondentEmailAddress), respondentFtpaSubmittedPersonalisation.getRecipientsList(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void should_return_given_personalisation(YesOrNo isAda) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(respondentFtpaSubmittedPersonalisation);
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisation());

        Map<String, String> personalisation =
            respondentFtpaSubmittedPersonalisation.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl)
            .containsEntry("subjectPrefix", isAda.equals(YesOrNo.YES) ? "Accelerated detained appeal"
                : "Immigration and Asylum appeal")
            .containsAllEntriesOf(getPersonalisation());
    }

    @Test
    public void should_throw_exception_when_personalisation_when_callback_is_null() {

        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> respondentFtpaSubmittedPersonalisation.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());

    }

    private Map<String, String> getPersonalisation() {

        String customerServicesEmail = "cust.services@example.com";
        String customerServicesTelephone = "555 555 555";
        String ariaListingReference = "someAriaListingReference";
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "PA/12345/001")
            .put("ariaListingReference", ariaListingReference)
            .put("homeOfficeReference", "A1234567")
            .put("appellantGivenNames", "Talha")
            .put("appellantFamilyName", "Awan")
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail)
            .build();
    }
}
