package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;


import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepCmrRelistingPersonalisationTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateTimeExtractor dateTimeExtractor;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private HearingDetailsFinder hearingDetailsFinder;

    private LegalRepCmrRelistingPersonalisation personalisation;

    private final String templateId = "templateId";
    private final String remoteTemplateId = "remoteTemplateId";
    private final String frontendUrl = "http://frontend";
    private final String hearingDateTime = "2024-01-01T10:00:00";
    private final String hearingDate = "2024-01-01";
    private final String hearingTime = "10:00";
    private final String hearingCentreAddress = "Taylor House";

    @BeforeEach
    void setup() {

        personalisation = new LegalRepCmrRelistingPersonalisation(
                templateId,
                remoteTemplateId,
                frontendUrl,
                dateTimeExtractor,
                customerServicesProvider,
                hearingDetailsFinder
        );

        ReflectionTestUtils.setField(personalisation, "adaPrefix", "Accelerated detained appeal");
        ReflectionTestUtils.setField(personalisation, "nonAdaPrefix", "Immigration and Asylum appeal");

        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);

        when(hearingDetailsFinder.getHearingCentreLocation(asylumCase))
                .thenReturn(hearingCentreAddress);

        when(customerServicesProvider.getCustomerServicesPersonalisation())
                .thenReturn(Map.of(
                        "customerServicesTelephone", "555",
                        "customerServicesEmail", "test@test.com"
                ));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of("ref"));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class))
                .thenReturn(Optional.of("aria"));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of("lrn"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of("John"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("Smith"));

        when(asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class))
                .thenReturn(Optional.of("Video hearing"));

        when(asylumCase.read(HEARING_REQUIREMENT_VULNERABILITIES, String.class))
                .thenReturn(Optional.of("vulnerabilities"));
        when(asylumCase.read(HEARING_REQUIREMENT_MULTIMEDIA, String.class))
                .thenReturn(Optional.of("multimedia"));
        when(asylumCase.read(HEARING_REQUIREMENT_SINGLE_SEX_COURT, String.class))
                .thenReturn(Optional.of("single sex"));
        when(asylumCase.read(HEARING_REQUIREMENT_IN_CAMERA_COURT, String.class))
                .thenReturn(Optional.of("in camera"));
        when(asylumCase.read(HEARING_REQUIREMENT_OTHER, String.class))
                .thenReturn(Optional.of("other"));
    }

    @Test
    void should_return_template_id_for_non_remote_hearing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        assertEquals(templateId, personalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_remote_template_id_for_remote_hearing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));

        assertEquals(remoteTemplateId, personalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_reference_id() {

        Long caseId = 123L;

        assertEquals(
                "123_CMR_RELISTED_OR_REMOTE_LEGAL_REPRESENTATIVE_EMAIL",
                personalisation.getReferenceId(caseId)
        );
    }

    @Test
    void should_build_personalisation() {

        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertEquals("ref", result.get("appealReferenceNumber"));
        assertEquals("aria", result.get("ariaListingReference"));
        assertEquals("lrn", result.get("legalRepReferenceNumber"));
        assertEquals("John", result.get("appellantGivenNames"));
        assertEquals("Smith", result.get("appellantFamilyName"));

        assertEquals(frontendUrl, result.get("linkToOnlineService"));

        assertEquals(hearingDate, result.get("hearingDate"));
        assertEquals(hearingTime, result.get("hearingTime"));
        assertEquals(hearingCentreAddress, result.get("hearingCentreAddress"));

        assertEquals("Video hearing", result.get("remoteVideoCallTribunalResponse"));

        assertEquals("No special adjustments are being made to accommodate vulnerabilities", result.get("hearingRequirementVulnerabilities"));
        assertEquals("No multimedia equipment is being provided", result.get("hearingRequirementMultimedia"));
        assertEquals("The court will not be single sex", result.get("hearingRequirementSingleSexCourt"));
        assertEquals("The hearing will be held in public court", result.get("hearingRequirementInCameraCourt"));
        assertEquals("No other adjustments are being made", result.get("hearingRequirementOther"));
    }
}