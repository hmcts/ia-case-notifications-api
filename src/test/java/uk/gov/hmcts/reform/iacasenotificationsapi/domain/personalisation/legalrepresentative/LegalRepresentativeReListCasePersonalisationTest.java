package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegalRepresentativeReListCasePersonalisationTest {

    private final String templateId = "someTemplateId";

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    PersonalisationProvider personalisationProvider;
    private LegalRepresentativeReListCasePersonalisation legalRepresentativeReListCasePersonalisation;

    @BeforeEach
    void setup() {

        legalRepresentativeReListCasePersonalisation = new LegalRepresentativeReListCasePersonalisation(
            templateId,
            personalisationProvider
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeReListCasePersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {

        Long caseId = 12345L;
        assertEquals(caseId + "_RE_LIST_CASE_LEGAL_REPRESENTATIVE",
            legalRepresentativeReListCasePersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> legalRepresentativeReListCasePersonalisation.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithGivenValues());

        Map<String, String> personalisation = legalRepresentativeReListCasePersonalisation.getPersonalisation(callback);

        assertFalse(personalisation.isEmpty());
        assertThat(personalisation)
            .containsAllEntriesOf(personalisationProvider.getPersonalisation(callback));
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        String customerServicesEmail = "cust.services@example.com";
        String customerServicesTelephone = "555 555 555";
        String requirementsOther = "someRequirementsOther";
        String requirementsSingleSexCourt = "someRequirementsSingleSexCourt";
        String requirementsInCamera = "someRequirementsInCamera";
        String requirementsMultimedia = "someRequirementsMultimedia";
        String requirementsVulnerabilities = "someRequirementsVulnerabilities";
        String remoteVideoCallTribunalResponse = "some tribunal response";
        String homeOfficeRefNumber = "homeOfficeRefNumber";
        String appellantFamilyName = "appellantFamilyName";
        String appellantGivenNames = "appellantGivenNames";
        String ariaListingReference = "someAriaListingReference";
        String appealReferenceNumber = "someReferenceNumber";
        String hearingCentreAddress = "some hearing centre address";
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("ariaListingReference", ariaListingReference)
            .put("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("linkToOnlineService", "http://localhost")
            .put("hearingCentreName", HearingCentre.TAYLOR_HOUSE.toString())
            .put("remoteVideoCallTribunalResponse", remoteVideoCallTribunalResponse)
            .put("hearingRequirementVulnerabilities", requirementsVulnerabilities)
            .put("hearingRequirementMultimedia", requirementsMultimedia)
            .put("hearingRequirementSingleSexCourt", requirementsSingleSexCourt)
            .put("hearingRequirementInCameraCourt", requirementsInCamera)
            .put("hearingRequirementOther", requirementsOther)
            .put("oldHearingCentre", HearingCentre.MANCHESTER.toString())
            .put("hearingCentreAddress", hearingCentreAddress)
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail)
            .build();
    }
}
