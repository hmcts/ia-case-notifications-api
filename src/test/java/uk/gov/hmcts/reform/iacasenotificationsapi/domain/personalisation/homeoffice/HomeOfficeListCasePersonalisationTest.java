package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeListCasePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    StringProvider stringProvider;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;

    private final String adaTemplateId = "adaTemplateId";
    private final String nonAdaTemplateId = "nonAdaTemplateId";
    private final String iaExUiFrontendUrl = "http://somefrontendurl";
    private final HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private final String homeOfficeEmailAddress = "homeoffice@example.com";
    private final String hearingCentreAddress = "some hearing centre address";
    //Remote hearing
    private final HearingCentre remoteHearingCentre = HearingCentre.REMOTE_HEARING;

    private final String hearingDate = "2019-08-27";
    private final String hearingTime = "14:25";

    private final String appealReferenceNumber = "someReferenceNumber";
    private final String ariaListingReference = "someAriaListingReference";
    private final String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";

    private final String requirementsVulnerabilities = "someRequirementsVulnerabilities";
    private final String requirementsMultimedia = "someRequirementsMultimedia";
    private final String requirementsSingleSexCourt = "someRequirementsSingleSexCourt";
    private final String requirementsInCamera = "someRequirementsInCamera";
    private final String requirementsOther = "someRequirementsOther";

    private final String caseOfficerReviewedVulnerabilities = "someCaseOfficerReviewedVulnerabilities";
    private final String caseOfficerReviewedMultimedia = "someCaseOfficerReviewedMultimedia";
    private final String caseOfficerReviewedSingleSexCourt = "someCaseOfficerReviewedSingleSexCourt";
    private final String caseOfficerReviewedInCamera = "someCaseOfficerReviewedInCamera";
    private final String caseOfficerReviewedOther = "someCaseOfficerReviewedOther";

    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";

    private final int appellantProvidingAppealArgumentDeadline = 13;
    private final int respondentResponseToAppealArgumentDeadline = 15;

    private HomeOfficeListCasePersonalisation homeOfficeListCasePersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        String hearingDateTime = "2019-08-27T14:25:15.000";
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateTime));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class))
            .thenReturn(Optional.of(requirementsVulnerabilities));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class))
            .thenReturn(Optional.of(requirementsMultimedia));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class))
            .thenReturn(Optional.of(requirementsSingleSexCourt));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class))
            .thenReturn(Optional.of(requirementsInCamera));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class)).thenReturn(Optional.of(requirementsOther));

        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedVulnerabilities));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedMultimedia));
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedSingleSexCourt));
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedInCamera));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedOther));
        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.NO));

        when(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeEmailAddress);

        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(hearingDetailsFinder.getHearingCentreName(asylumCase)).thenReturn(hearingCentre.toString());
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn(hearingCentreAddress);
        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString()))
            .thenReturn(Optional.of(hearingCentreAddress));
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(hearingDetailsFinder.getHearingCentreLocation(asylumCase)).thenReturn(hearingCentreAddress);

        String listAssistHearingTemplateId = "listAssistHearingTemplateId";
        homeOfficeListCasePersonalisation = new HomeOfficeListCasePersonalisation(
            nonAdaTemplateId,
            adaTemplateId,
            listAssistHearingTemplateId,
            iaExUiFrontendUrl,
            appellantProvidingAppealArgumentDeadline,
            respondentResponseToAppealArgumentDeadline,
            dateTimeExtractor,
            emailAddressFinder,
            customerServicesProvider,
            hearingDetailsFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertEquals(adaTemplateId, homeOfficeListCasePersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertEquals(nonAdaTemplateId, homeOfficeListCasePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_CASE_LISTED_HOME_OFFICE", homeOfficeListCasePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_lookup_map() {
        assertTrue(homeOfficeListCasePersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddress));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
        public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> personalisation = homeOfficeListCasePersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl)
            .containsEntry("hearingRequirementVulnerabilities", requirementsVulnerabilities)
            .containsEntry("hearingRequirementMultimedia", requirementsMultimedia)
            .containsEntry("hearingRequirementSingleSexCourt", requirementsSingleSexCourt)
            .containsEntry("hearingRequirementInCameraCourt", requirementsInCamera)
            .containsEntry("hearingRequirementOther", requirementsOther)
            .containsEntry("hearingDate", hearingDate)
            .containsEntry("hearingTime", hearingTime)
            .containsEntry("hearingCentreAddress", hearingCentreAddress);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

        if (isAda.equals(YesOrNo.YES)) {
            String appealArgumentDeadlineDate = LocalDate.now().plusDays(appellantProvidingAppealArgumentDeadline)
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
                assertEquals(appealArgumentDeadlineDate, personalisation.get("appellantProvidingAppealArgumentDeadline"));

            String respondentResponseDeadlineDate = LocalDate.now().plusDays(respondentResponseToAppealArgumentDeadline)
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
                assertEquals(respondentResponseDeadlineDate, personalisation.get("respondentResponseToAppealArgumentDeadline"));
        } else {
            assertFalse(personalisation.containsKey("appellantProvidingAppealArgumentDeadline"));
            assertFalse(personalisation.containsKey("respondentResponseToAppealArgumentDeadline"));
        }
    }

    @Test
    void should_return_personalisation_when_all_information_given_in_remote_hearing_case() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(remoteHearingCentre));

        String hearingCentreEmailAddress = "taylorHouseHearingCentre@example.com";
        when(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)).thenReturn(hearingCentreEmailAddress);

        String remoteHearingCentreAddress = "hearing centre address";
        when(hearingDetailsFinder.getHearingCentreLocation(asylumCase))
            .thenReturn(remoteHearingCentreAddress);

        Map<String, String> personalisation = homeOfficeListCasePersonalisation.getPersonalisation(asylumCase);

            assertEquals(remoteHearingCentreAddress, personalisation.get("hearingCentreAddress"));
    }

    @Test
    public void should_return_personalisation_when_co_records_hearing_response() {

        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));
        Map<String, String> personalisation = homeOfficeListCasePersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl)
            .containsEntry("hearingRequirementVulnerabilities", caseOfficerReviewedVulnerabilities)
            .containsEntry("hearingRequirementMultimedia", caseOfficerReviewedMultimedia)
            .containsEntry("hearingRequirementSingleSexCourt", caseOfficerReviewedSingleSexCourt)
            .containsEntry("hearingRequirementInCameraCourt", caseOfficerReviewedInCamera)
            .containsEntry("hearingRequirementOther", caseOfficerReviewedOther)
            .containsEntry("hearingDate", hearingDate)
            .containsEntry("hearingTime", hearingTime)
            .containsEntry("hearingCentreAddress", hearingCentreAddress);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_return_personalisation_with_requested_and_granted_hearing_requirement() {

        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(VULNERABILITIES_DECISION_FOR_DISPLAY, String.class)).thenReturn(Optional.of("Refused - Refused to vulnerabilities"));
        when(asylumCase.read(MULTIMEDIA_DECISION_FOR_DISPLAY, String.class)).thenReturn(Optional.of("Refused - Refused to multimedia"));
        when(asylumCase.read(SINGLE_SEX_COURT_DECISION_FOR_DISPLAY, String.class)).thenReturn(Optional.of("Granted - Granted to single sex court"));
        when(asylumCase.read(IN_CAMERA_COURT_DECISION_FOR_DISPLAY, String.class)).thenReturn(Optional.of("Granted - Granted to camera court"));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.empty());
        String videoHearingSuitability = "someVideoHearingSuitability";
        when(asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(videoHearingSuitability));

        Map<String, String> personalisation = homeOfficeListCasePersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("hearingRequirementVulnerabilities", "Request Refused - Refused to vulnerabilities")
            .containsEntry("hearingRequirementMultimedia", "Request Refused - Refused to multimedia")
            .containsEntry("hearingRequirementSingleSexCourt", "Request Granted - Granted to single sex court")
            .containsEntry("hearingRequirementInCameraCourt", "Request Granted - Granted to camera court")
            .containsEntry("hearingRequirementOther", "No other adjustments are being made")
            .containsEntry("remoteVideoCallTribunalResponse", videoHearingSuitability);
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = homeOfficeListCasePersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("ariaListingReference", "")
            .containsEntry("homeOfficeReferenceNumber", "")
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);
        assertEquals("No special adjustments are being made to accommodate vulnerabilities",
            personalisation.get("hearingRequirementVulnerabilities"));
        assertThat(personalisation)
            .containsEntry("hearingRequirementMultimedia", "No multimedia equipment is being provided")
            .containsEntry("hearingRequirementSingleSexCourt", "The court will not be single sex");
        assertEquals("The hearing will be held in public court",
            personalisation.get("hearingRequirementInCameraCourt"));
        assertThat(personalisation)
            .containsEntry("hearingRequirementOther", "No other adjustments are being made")
            .containsEntry("hearingDate", hearingDate)
            .containsEntry("hearingTime", hearingTime)
            .containsEntry("hearingCentreAddress", hearingCentreAddress);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
