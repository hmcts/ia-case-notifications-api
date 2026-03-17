package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantCmrRelistingPersonalisationSmsTest {

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

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String hearingCentreAddress = "some hearing centre address";

    private String hearingDateTime = "2019-08-27T14:25:15.000";
    private String hearingDate = "2019-08-27";
    private String hearingTime = "14:25";

    private String appellantGivenNames = "appellantGivenNames";
    private String appellantFamilyName = "appellantFamilyName";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";

    private String hearingCentreName = HearingCentre.TAYLOR_HOUSE.toString();
    private String remoteVideoCallTribunalResponse = "some tribunal response";
    private String requirementsVulnerabilities = "someRequirementsVulnerabilities";
    private String requirementsMultimedia = "someRequirementsMultimedia";
    private String requirementsInCamera = "someRequirementsInCamera";
    private String requirementsSingleSexCourt = "someRequirementsSingleSexCourt";
    private String requirementsOther = "someRequirementsOther";

    private AppellantCmrRelistingPersonalisationSms appellantCmrRelistingPersonalisationSms;

    @BeforeEach
    void setup() {
        when(asylumCase.read(CMR_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(CMR_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateTime));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantCmrRelistingPersonalisationSms = new AppellantCmrRelistingPersonalisationSms(
                templateId,
                iaAipFrontendUrl,
                recipientsFinder,
                personalisationProvider
        );
    }

    @Test
    public void should_return_correct_template_id() {
        assertEquals(templateId, appellantCmrRelistingPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CMR_RELISTED_APPELLANT_SMS",
                appellantCmrRelistingPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {
        when(recipientsFinder.findAll(null, NotificationType.SMS))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantCmrRelistingPersonalisationSms.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_given_mobile_list_from_subscribers_in_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        Set<String> response = appellantCmrRelistingPersonalisationSms.getRecipientsList(asylumCase);

        verify(recipientsFinder, times(1)).findAll(asylumCase, NotificationType.SMS);
        assertTrue(response.contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithGivenValues());
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        Map<String, String> personalisation =
                appellantCmrRelistingPersonalisationSms.getPersonalisation(callback);

        assertThat(personalisation).isNotEmpty();
        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_return_personalisation_when_optional_fields_are_blank() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithBlankValues());
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        Map<String, String> personalisation =
                appellantCmrRelistingPersonalisationSms.getPersonalisation(callback);

        assertThat(personalisation).isNotEmpty();
        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", mockedAppealReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("hearingCentreName", hearingCentreName)
                .put("remoteVideoCallTribunalResponse", remoteVideoCallTribunalResponse)
                .put("hearingRequirementVulnerabilities", requirementsVulnerabilities)
                .put("hearingRequirementMultimedia", requirementsMultimedia)
                .put("hearingRequirementSingleSexCourt", requirementsSingleSexCourt)
                .put("hearingRequirementInCameraCourt", requirementsInCamera)
                .put("hearingRequirementOther", requirementsOther)
                .put("oldHearingCentre", hearingDateTime)
                .put("oldHearingDate", hearingDate)
                .put("hearingDate", hearingDate)
                .put("hearingTime", hearingTime)
                .put("hearingCentreAddress", hearingCentreAddress)
                .build();
    }

    private Map<String, String> getPersonalisationMapWithBlankValues() {
        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", "")
                .put("appellantGivenNames", "")
                .put("appellantFamilyName", "")
                .put("hearingCentreName", "")
                .put("remoteVideoCallTribunalResponse", "")
                .put("hearingRequirementVulnerabilities", "")
                .put("hearingRequirementMultimedia", "")
                .put("hearingRequirementSingleSexCourt", "")
                .put("hearingRequirementInCameraCourt", "")
                .put("hearingRequirementOther", "")
                .put("oldHearingCentre", "")
                .put("oldHearingDate", "")
                .put("hearingDate", "")
                .put("hearingTime", "")
                .put("hearingCentreAddress", hearingCentreAddress)
                .build();
    }
}