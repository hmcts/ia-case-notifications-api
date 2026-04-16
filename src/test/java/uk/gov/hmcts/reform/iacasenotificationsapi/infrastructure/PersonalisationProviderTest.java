package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PersonalisationProviderTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    AsylumCase asylumCase;
    @Mock
    AsylumCase asylumCaseBefore;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    @Mock
    Direction direction;
    @Mock
    DirectionFinder directionFinder;

    private final String iaExUiFrontendUrl = "http://localhost";

    private final String hearingCentreName = HearingCentre.TAYLOR_HOUSE.toString();

    private final String appealReferenceNumber = "someReferenceNumber";
    private final String legalRepReferenceNumber = "legalRepReferenceNumber";
    private final String ariaListingReference = "someAriaListingReference";
    private final String appellantGivenNames = "appellantGivenNames";
    private final String appellantFamilyName = "appellantFamilyName";
    private final String homeOfficeRefNumber = "homeOfficeRefNumber";

    private final String oldHearingCentreName = HearingCentre.MANCHESTER.toString();

    private final String remoteVideoCallTribunalResponse = "someRemoteVideoCallTribunalResponse";

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

    private final String caseOfficerReviewedRemoteHearingDisplay = "caseOfficerReviewedRemoteHearingDisplay";


    private final String recipientReferenceNumber = "recipientReferenceNumber";
    private final String recipient = "recipient";
    private final String applyForCostsCreationDate = "2023-11-24";

    private static final String homeOffice = "Home office";

    private PersonalisationProvider personalisationProvider;

    @BeforeEach
    public void setUp() {
        when(hearingDetailsFinder.getHearingCentreName(asylumCase)).thenReturn(hearingCentreName);
        String hearingCentreAddress = "some hearing centre address";
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn(hearingCentreAddress);
        when(hearingDetailsFinder.getHearingCentreLocation(asylumCase)).thenReturn(hearingCentreAddress);
        String hearingDateTime = "2019-08-27T14:25:15.000";
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(hearingDetailsFinder.getOldHearingCentreName(asylumCaseBefore)).thenReturn(oldHearingCentreName);
        String oldHearingDateTime = "2019-08-20T14:25:15.000";
        when(asylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of(oldHearingDateTime));

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));

        String hearingDate = "2019-08-27";
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        String oldHearingDate = "2019-08-20";
        when(dateTimeExtractor.extractHearingDate(oldHearingDateTime)).thenReturn(oldHearingDate);
        String hearingTime = "14:25";
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);

        when(asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(remoteVideoCallTribunalResponse));

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

        when(directionFinder.findFirst(asylumCase, DirectionTag.NONE)).thenReturn(Optional.of(direction));
        String directionDueDate = "2019-10-29";
        when(direction.getDateDue()).thenReturn(directionDueDate);
        String directionExplanation = "someExplanation";
        when(direction.getExplanation()).thenReturn(directionExplanation);

        personalisationProvider = new PersonalisationProvider(
            iaExUiFrontendUrl,
            hearingDetailsFinder,
            directionFinder,
            dateTimeExtractor
        );
    }

    @Test
    void should_return_edit_case_listing_personalisation() {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);

        Map<String, String> personalisation = personalisationProvider.getPersonalisation(callback);

        assertTrue(personalisation.get("remoteVideoCallTribunalResponse").contains(remoteVideoCallTribunalResponse));
        assertTrue(personalisation.get("hearingRequirementVulnerabilities").contains(requirementsVulnerabilities));
        assertTrue(personalisation.get("hearingRequirementMultimedia").contains(requirementsMultimedia));
        assertTrue(personalisation.get("hearingRequirementSingleSexCourt").contains(requirementsSingleSexCourt));
        assertTrue(personalisation.get("hearingRequirementInCameraCourt").contains(requirementsInCamera));
        assertTrue(personalisation.get("hearingRequirementOther").contains(requirementsOther));
    }

    @ParameterizedTest
    @CsvSource({
        "true, true",
        "false, true",
        "false, false"
    })
    void should_return_edit_case_listing_personalisation_when_submit_hearing_present(boolean displayFieldsPresent,
                                                                                     boolean responseFieldsPresent) {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(responseFieldsPresent ? Optional.of(caseOfficerReviewedVulnerabilities) : Optional.empty());
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(responseFieldsPresent ? Optional.of(caseOfficerReviewedMultimedia) : Optional.empty());
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(responseFieldsPresent ? Optional.of(caseOfficerReviewedSingleSexCourt) : Optional.empty());
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(responseFieldsPresent ? Optional.of(caseOfficerReviewedInCamera) : Optional.empty());
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(responseFieldsPresent ? Optional.of(caseOfficerReviewedOther) : Optional.empty());
        when(asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(responseFieldsPresent ? Optional.of(remoteVideoCallTribunalResponse) : Optional.empty());

        String caseOfficerReviewedVulnerabilitiesDisplay = "someCaseOfficerReviewedVulnerabilitiesDisplay";
        when(asylumCase.read(VULNERABILITIES_DECISION_FOR_DISPLAY, String.class))
            .thenReturn(displayFieldsPresent ? Optional.of(caseOfficerReviewedVulnerabilitiesDisplay) : Optional.empty());
        String caseOfficerReviewedMultimediaDisplay = "someCaseOfficerReviewedMultimediaDisplay";
        when(asylumCase.read(MULTIMEDIA_DECISION_FOR_DISPLAY, String.class))
            .thenReturn(displayFieldsPresent ? Optional.of(caseOfficerReviewedMultimediaDisplay) : Optional.empty());
        String caseOfficerReviewedSingleSexCourtDisplay = "someCaseOfficerReviewedSingleSexCourtDisplay";
        when(asylumCase.read(SINGLE_SEX_COURT_DECISION_FOR_DISPLAY, String.class))
            .thenReturn(displayFieldsPresent ? Optional.of(caseOfficerReviewedSingleSexCourtDisplay) : Optional.empty());
        String caseOfficerReviewedInCameraDisplay = "someCaseOfficerReviewedInCameraDisplay";
        when(asylumCase.read(IN_CAMERA_COURT_DECISION_FOR_DISPLAY, String.class))
            .thenReturn(displayFieldsPresent ? Optional.of(caseOfficerReviewedInCameraDisplay) : Optional.empty());
        String caseOfficerReviewedOtherDisplay = "someCaseOfficerReviewedOtherDisplay";
        when(asylumCase.read(OTHER_DECISION_FOR_DISPLAY, String.class))
            .thenReturn(displayFieldsPresent ? Optional.of(caseOfficerReviewedOtherDisplay) : Optional.empty());

        Map<String, String> personalisation = personalisationProvider.getPersonalisation(callback);

        String vulnerabilitiesDefaultText = "No special adjustments are being made to accommodate vulnerabilities";
        String multimediaDefaultText = "No multimedia equipment is being provided";
        String singleSexCourtDefaultText = "The court will not be single sex";
        String inCameraCourtDefaultText = "The hearing will be held in public court";
        String otherAdjustmentsDefaultText = "No other adjustments are being made";
        String remoteHearingDefaultText = "";

        assertTrue(personalisation.get("hearingRequirementVulnerabilities").contains(
            displayFieldsPresent ? caseOfficerReviewedVulnerabilitiesDisplay
                : responseFieldsPresent ? caseOfficerReviewedVulnerabilities : vulnerabilitiesDefaultText));

        assertTrue(personalisation.get("hearingRequirementMultimedia").contains(
            displayFieldsPresent ? caseOfficerReviewedMultimediaDisplay
                : responseFieldsPresent ? caseOfficerReviewedMultimedia : multimediaDefaultText));

        assertTrue(personalisation.get("hearingRequirementSingleSexCourt").contains(
            displayFieldsPresent ? caseOfficerReviewedSingleSexCourtDisplay
                : responseFieldsPresent ? caseOfficerReviewedSingleSexCourt : singleSexCourtDefaultText));

        assertTrue(personalisation.get("hearingRequirementInCameraCourt").contains(
            displayFieldsPresent ? caseOfficerReviewedInCameraDisplay
                : responseFieldsPresent ? caseOfficerReviewedInCamera : inCameraCourtDefaultText));

        assertTrue(personalisation.get("hearingRequirementOther").contains(
            displayFieldsPresent ? caseOfficerReviewedOtherDisplay
                : responseFieldsPresent ? caseOfficerReviewedOther : otherAdjustmentsDefaultText));

        assertTrue(personalisation.get("remoteVideoCallTribunalResponse")
            .contains(responseFieldsPresent ? remoteVideoCallTribunalResponse : remoteHearingDefaultText));
    }

    @Test
    void should_return_uploaded_additional_evidence_personalisation() {
        when(callback.getEvent()).thenReturn(Event.UPLOAD_ADDITIONAL_EVIDENCE);

        Map<String, String> personalisation = personalisationProvider.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("legalRepReferenceNumber", legalRepReferenceNumber)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber);
    }

    @Test
    void should_return_non_direction_personalisation() {
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);

        Map<String, String> personalisation = personalisationProvider.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("legalRepReferenceNumber", legalRepReferenceNumber)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber);
    }

    @Test
    void should_return_reviewed_hearing_requirements_personalisation() {

        Map<String, String> personalisation =
            personalisationProvider.getReviewedHearingRequirementsPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName);
    }

    @Test
    void should_return_change_direction_due_date_personalisation() {
        when(callback.getEvent()).thenReturn(Event.CHANGE_DIRECTION_DUE_DATE);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        String directionEditExplanation = "This is edit direction explanation";
        when(asylumCase.read(DIRECTION_EDIT_EXPLANATION, String.class))
            .thenReturn(Optional.of(directionEditExplanation));
        String directionEditDateDue = "2020-02-14";
        when(asylumCase.read(DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.of(directionEditDateDue));

        Map<String, String> personalisation = personalisationProvider.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("legalRepReferenceNumber", legalRepReferenceNumber)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber);
    }

    @Test
    void should_return_legal_rep_header_personalisation_when_all_information_given() {

        Map<String, String> personalisation = personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("legalRepReferenceNumber", legalRepReferenceNumber);
    }

    @Test
    void should_return_home_office_header_personalisation_when_all_information_given() {

        Map<String, String> personalisation = personalisationProvider.getHomeOfficeHeaderPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .containsEntry("ariaListingReference", ariaListingReference);
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = personalisationProvider.getTribunalHeaderPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("ariaListingReference", ariaListingReference);
    }

    @Test
    void should_return_appelant_personalisation_when_all_information_given() {

        Map<String, String> personalisation = personalisationProvider.getAppellantPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber);
    }

    @Test
    void should_return_appelant_credentials_when_all_information_given() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(new IdValue<>("1", new ApplyForCosts("Wasted costs", "Home office", "Respondent", applyForCostsCreationDate)));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        Map<String, String> personalisation = personalisationProvider.getApplyForCostsPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl);
    }

    @Test
    void should_return_home_office_recipient_header_when_all_information_given() {
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));

        Map<String, String> personalisation = personalisationProvider.getHomeOfficeRecipientHeader(asylumCase);

        assertThat(personalisation)
            .containsEntry(recipient, "Home Office")
            .containsEntry(recipientReferenceNumber, homeOfficeRefNumber);
    }

    @Test
    void should_return_legal_rep_recipient_header_when_all_information_given() {
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));

        Map<String, String> personalisation = personalisationProvider.getLegalRepRecipientHeader(asylumCase);

        assertThat(personalisation)
            .containsEntry(recipient, "Your")
            .containsEntry(recipientReferenceNumber, legalRepReferenceNumber);
    }

    @Test
    void should_return_application_number_in_respond_to_costs_when_all_information_given() {
        DynamicList dynamicList = new DynamicList(new Value("1", "Costs 8, Wasted costs, 15 Nov 2023"), List.of(new Value("1", "Costs 8, Wasted costs, 15 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(dynamicList));

        Map<String, String> personalisation = personalisationProvider.retrieveSelectedApplicationId(asylumCase, RESPOND_TO_COSTS_LIST);

            assertEquals("8", personalisation.get("applicationId"));
    }

    @Test
    void should_return_application_number_in_additional_evidence_applicaiton_when_all_information_given() {
        DynamicList dynamicList = new DynamicList(new Value("1", "Costs 8, Wasted costs, 15 Nov 2023"), List.of(new Value("1", "Costs 8, Wasted costs, 15 Nov 2023")));

        when(asylumCase.read(ADD_EVIDENCE_FOR_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(dynamicList));

        Map<String, String> personalisation = personalisationProvider.retrieveSelectedApplicationId(asylumCase, ADD_EVIDENCE_FOR_COSTS_LIST);

            assertEquals("8", personalisation.get("applicationId"));
    }

    @Test
    void should_throw_an_exception_if_respond_to_costs_list_is_not_presented() {
        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
assertThrows(IllegalStateException.class, () -> personalisationProvider.retrieveSelectedApplicationId(asylumCase, RESPOND_TO_COSTS_LIST))
            ;
assertEquals("RESPOND_TO_COSTS_LIST is not present", exception.getMessage());
    }

    @Test
    void should_throw_an_exception_if_additional_evidence_applicaiton_number_not_presented() {
        when(asylumCase.read(ADD_EVIDENCE_FOR_COSTS_LIST, DynamicList.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
assertThrows(IllegalStateException.class, () -> personalisationProvider.retrieveSelectedApplicationId(asylumCase, ADD_EVIDENCE_FOR_COSTS_LIST))
            ;
assertEquals("ADD_EVIDENCE_FOR_COSTS_LIST is not present", exception.getMessage());
    }

    @Test
    void should_return_apply_for_costs_creation_date_when_all_information_given() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(new IdValue<>("1", new ApplyForCosts("Wasted costs", "Home office", "Respondent", applyForCostsCreationDate)));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        Map<String, String> personalisation = personalisationProvider.getApplyToCostsCreationDate(asylumCase);

            assertEquals("24 Nov 2023", personalisation.get("creationDate"));
    }

    @Test
    void should_return_type_for_latest_created_apply_for_costs() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(new IdValue<>("1", new ApplyForCosts("Wasted costs", "Home office", "Respondent", applyForCostsCreationDate)));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        Map<String, String> personalisation = personalisationProvider.getTypeForLatestCreatedApplyForCosts(asylumCase);

            assertEquals("Wasted", personalisation.get("appliedCostsType"));
    }


    @Test
    void should_return_type_for_selected_apply_for_costs() {
        DynamicList dynamicList = new DynamicList(
            new Value("2", "Costs 8, Unreasonable costs, 15 Nov 2023"),
            List.of(
                new Value("1", "Costs 1, Wasted costs, 15 Nov 2023"),
                new Value("2", "Costs 2, Unreasonable costs, 15 Nov 2023"))
        );

        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("1", new ApplyForCosts("Wasted costs", "Home office", "Respondent", applyForCostsCreationDate)),
            new IdValue<>("2", new ApplyForCosts("Unreasonable costs", "Home office", "Respondent", applyForCostsCreationDate))
        );
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));
        when(asylumCase.read(ADD_EVIDENCE_FOR_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(dynamicList));

        Map<String, String> personalisation = personalisationProvider.getTypeForSelectedApplyForCosts(asylumCase, ADD_EVIDENCE_FOR_COSTS_LIST);

            assertEquals("Unreasonable", personalisation.get("appliedCostsType"));
    }

    @Test
    void should_return_costs_decision_when_decideCostsApplicationList_is_present() {
        String applyForCostsDecision = "Order made";
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("1", new ApplyForCosts("Wasted costs", "Home office", "Respondent", "costsType", applyForCostsCreationDate, applyForCostsDecision))
        );
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        DynamicList respondsToCostsList = new DynamicList(new Value("1", "Costs 1, Unreasonable costs, 24 Nov 2023"), List.of(new Value("1", "Costs 1, Unreasonable costs, 24 Nov 2023")));
        when(asylumCase.read(DECIDE_COSTS_APPLICATION_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));

        Map<String, String> personalisation = personalisationProvider.getDecideCostsPersonalisation(asylumCase);

            assertEquals(applyForCostsDecision, personalisation.get("costsDecisionType"));
    }
}
