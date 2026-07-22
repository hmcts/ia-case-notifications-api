package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CMR_IS_REMOTE_HEARING;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeCmrRelistingPersonalisationTest {

    private final String templateId = "listAssistCaseEditedTemplateId";
    private final String remoteHearingTemplateId = "listAssistCaseEditedRemoteHearingTemplateId";
    private final String legalRepEmailAddress = "legalRep@example.com";

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private LegalRepresentativeCmrRelistingPersonalisation legalRepresentativeCmrRelistingPersonalisation;

    @BeforeEach
    void setup() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));

        legalRepresentativeCmrRelistingPersonalisation = new LegalRepresentativeCmrRelistingPersonalisation(
            templateId,
            remoteHearingTemplateId,
            personalisationProvider,
            customerServicesProvider
        );
    }

    @Test
    void should_return_given_template_id() {
        when(asylumCase.read(CMR_IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertEquals(templateId, legalRepresentativeCmrRelistingPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(CMR_IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertEquals(remoteHearingTemplateId, legalRepresentativeCmrRelistingPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_CMR_RE_LISTING_LEGAL_REPRESENTATIVE",
            legalRepresentativeCmrRelistingPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        assertTrue(
            legalRepresentativeCmrRelistingPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
    }

    @Test
    void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class,
                () -> legalRepresentativeCmrRelistingPersonalisation.getRecipientsList(asylumCase));
        assertEquals("legalRepresentativeEmailAddress is not present", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> legalRepresentativeCmrRelistingPersonalisation.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    private Map<String, String> getCmrRelistingPersonalisationMap() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "someReferenceNumber")
            .put("ariaListingReference", "someAriaListingReference")
            .put("homeOfficeReferenceNumber", "homeOfficeRefNumber")
            .put("appellantGivenNames", "appellantGivenNames")
            .put("appellantFamilyName", "appellantFamilyName")
            .put("linkToOnlineService", "http://somefrontendurl")
            .put("hearingCentreName", "Taylor House")
            .put("hearingCentreAddress", "some hearing centre address")
            .put("oldHearingCentre", "Manchester")
            .put("oldHearingDate", "19 Aug 2023")
            .put("hearingDate", "19 Sep 2023")
            .put("hearingTime", "10:00")
            .build();
    }
}
