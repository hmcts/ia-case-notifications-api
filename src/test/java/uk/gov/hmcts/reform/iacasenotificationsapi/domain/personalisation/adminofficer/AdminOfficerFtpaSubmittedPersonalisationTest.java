package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
public class AdminOfficerFtpaSubmittedPersonalisationTest {

    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;

    private final String templateId = "ftpaSubmittedTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String adminOfficerEmailAddress = "adminOfficer@example.com";

    private AdminOfficerFtpaSubmittedPersonalisation adminOfficerFtpaSubmittedPersonalisation;

    @BeforeEach
    public void setUp() {

        adminOfficerFtpaSubmittedPersonalisation = new AdminOfficerFtpaSubmittedPersonalisation(
            templateId,
            iaExUiFrontendUrl,
            personalisationProvider,
            adminOfficerEmailAddress);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void should_return_given_personalisation(YesOrNo isAda) {

        initializePrefixes(adminOfficerFtpaSubmittedPersonalisation);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisation());
        Map<String, String> personalisation =
            adminOfficerFtpaSubmittedPersonalisation.getPersonalisation(callback);

        String nonAdaPrefix = "Immigration and Asylum appeal";
        String adaPrefix = "Accelerated detained appeal";
        assertThat(personalisation)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl)
            .containsEntry("subjectPrefix", isAda.equals(YesOrNo.YES)
                ? adaPrefix
                : nonAdaPrefix)
            .containsAllEntriesOf(getPersonalisation());
    }

    @Test
    public void should_throw_exception_when_callback_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> adminOfficerFtpaSubmittedPersonalisation.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    public void should_return_given_reference_id() {

        Long caseId = 12345L;
        assertEquals(caseId + "_FTPA_SUBMITTED_ADMIN_OFFICER", adminOfficerFtpaSubmittedPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address() {

        assertEquals(Collections.singleton(adminOfficerEmailAddress), adminOfficerFtpaSubmittedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_template_id() {

        assertEquals(templateId, adminOfficerFtpaSubmittedPersonalisation.getTemplateId());
    }

    private Map<String, String> getPersonalisation() {

        String ariaListingReference = "someAriaListingReference";
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "PA/12345/001")
            .put("ariaListingReference", ariaListingReference)
            .put("appellantGivenNames", "Talha")
            .put("appellantFamilyName", "Awan")
            .build();
    }
}
