package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseOfficerCmrRelistingPersonalisationTest {

    private final String templateId = "listAssistCaseEditedCaseOfficerTemplateId";
    private final String caseOfficerEmailAddress = "case-officer@example.com";

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    PersonalisationProvider personalisationProvider;

    private CaseOfficerCmrRelistingPersonalisation caseOfficerCmrRelistingPersonalisation;

    @BeforeEach
    void setup() {
        when(emailAddressFinder.getCmrListingCaseOfficerHearingCentreEmailAddress(asylumCase))
            .thenReturn(caseOfficerEmailAddress);

        caseOfficerCmrRelistingPersonalisation = new CaseOfficerCmrRelistingPersonalisation(
            templateId,
            emailAddressFinder,
            personalisationProvider
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, caseOfficerCmrRelistingPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_CMR_RE_LISTING_CASE_OFFICER",
            caseOfficerCmrRelistingPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_lookup() {
        assertTrue(caseOfficerCmrRelistingPersonalisation.getRecipientsList(asylumCase).contains(caseOfficerEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> caseOfficerCmrRelistingPersonalisation.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_return_personalisation_when_all_information_given(YesOrNo isAda) {
        initializePrefixes(caseOfficerCmrRelistingPersonalisation);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getCmrRelistingPersonalisationMap());

        Map<String, String> personalisation = caseOfficerCmrRelistingPersonalisation.getPersonalisation(callback);

        assertFalse(personalisation.isEmpty());
        assertThat(personalisation)
            .containsAllEntriesOf(getCmrRelistingPersonalisationMap())
            .containsEntry("subjectPrefix", isAda.equals(YesOrNo.YES)
                ? "Accelerated detained appeal"
                : "Immigration and Asylum appeal");
    }

    private Map<String, String> getCmrRelistingPersonalisationMap() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "someReferenceNumber")
            .put("ariaListingReference", "someAriaListingReference")
            .put("hearingCentreName", "Taylor House")
            .put("hearingCentreAddress", "some hearing centre address")
            .put("oldHearingCentre", "Manchester")
            .put("oldHearingDate", "19 Aug 2023")
            .put("hearingDate", "19 Sep 2023")
            .put("hearingTime", "10:00")
            .build();
    }
}
