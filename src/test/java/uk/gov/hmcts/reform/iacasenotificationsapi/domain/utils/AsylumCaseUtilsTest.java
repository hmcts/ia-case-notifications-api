package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ADDENDUM_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_HAS_FIXED_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPLIES_FOR_COSTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ARIA_MIGRATED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_EJP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.RESPOND_TO_COSTS_LIST;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBMISSION_OUT_OF_TIME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.calculateFeeDifference;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.generateAppellantPinIfNotPresent;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getAddendumEvidenceDocuments;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getApplicantAndRespondent;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getApplicationById;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getFtpaDecisionOutcomeType;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLatestAddendumEvidenceDocument;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.hasAppellantAddressInCountryOrOutOfCountry;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.hasHearingChannel;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAgeAssessmentAppeal;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAipJourney;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAppealListed;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAriaMigrated;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isDetainedInFacilityType;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isDetainedInOneOfFacilityTypes;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isFeeExemptAppeal;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isInternalCase;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isLegalRepEjp;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isLoggedUserIsHomeOffice;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isNotInternalOrIsInternalWithLegalRepresentation;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isSubmissionOutOfTime;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.retrieveLatestApplyForCosts;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplyForCosts;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DetentionFacility;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AccessCodeGenerator;

@ExtendWith(MockitoExtension.class)
public class AsylumCaseUtilsTest {

    @Mock
    private AsylumCase asylumCase;
    @Spy
    private AsylumCase asylumCaseSpy;
    @Mock
    private Document document;
    private final MockedStatic<AccessCodeGenerator> generatorMockedStatic = mockStatic(AccessCodeGenerator.class);


    private final String legalOfficerAddendumUploadedByLabel = "TCW";
    private final String legalOfficerAddendumUploadSuppliedByLabel = "The respondent";
    private static final String applyForCostsCreationDate = "2023-11-24";
    private final IdValue<DocumentWithMetadata> addendumOne = new IdValue<>(
        "1",
        new DocumentWithMetadata(
            document,
            "Some description",
            "2018-12-25",
            DocumentTag.ADDENDUM_EVIDENCE,
            legalOfficerAddendumUploadSuppliedByLabel,
            legalOfficerAddendumUploadedByLabel
        )
    );

    private final IdValue<DocumentWithMetadata> addendumTwo = new IdValue<>(
        "2",
        new DocumentWithMetadata(
            document,
            "Some description",
            "2018-12-26", DocumentTag.ADDENDUM_EVIDENCE,
            legalOfficerAddendumUploadSuppliedByLabel,
            legalOfficerAddendumUploadedByLabel
        )
    );

    private final String legalRepEmailEjp = "legalRep@example.com";
    private final String generatedCode = "12345";

    @AfterEach
    void tearDown() {
        generatorMockedStatic.close();
    }

    @Test
    void should_return_correct_value_for_det() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(isAppellantInDetention(asylumCase));
    }

    @Test
    void should_return_correct_value_for_ada() {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(isAcceleratedDetainedAppeal(asylumCase));
    }

    @Test
    void should_return_correct_value_for_aaa() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));
        assertTrue(isAgeAssessmentAppeal(asylumCase));
    }

    @Test
    void isAdmin_should_return_true() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(isInternalCase(asylumCase));
    }

    @Test
    void isAdmin_should_return_false() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(isInternalCase(asylumCase));
    }

    @Test
    void isNotInternalOrIsInternalWithLegalRepresentation_should_return_true() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertTrue(isNotInternalOrIsInternalWithLegalRepresentation(asylumCase));
    }

    @Test
    void isNotInternalOrIsInternalWithLegalRepresentation_should_return_false() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));

        assertFalse(isNotInternalOrIsInternalWithLegalRepresentation(asylumCase));
    }

    @Test
    void isAriaMigrated_should_return_true() {
        when(asylumCase.read(IS_ARIA_MIGRATED, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(isAriaMigrated(asylumCase));
    }

    @Test
    void isAriaMigrated_should_return_false() {
        when(asylumCase.read(IS_ARIA_MIGRATED, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(isAriaMigrated(asylumCase));
    }

    @Test
    void isAipJourney_should_return_true() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertTrue(isAipJourney(asylumCase));
    }

    @Test
    void isAipJourney_should_return_false() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        assertFalse(isAipJourney(asylumCase));
    }

    @Test
    void getFtpaDecisionOutcomeType_should_return_granted() {
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_GRANTED));
        assertEquals(FtpaDecisionOutcomeType.FTPA_GRANTED, getFtpaDecisionOutcomeType(asylumCase).orElse(null));
    }

    @Test
    void getFtpaDecisionOutcomeType_should_return_refused() {
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_REFUSED));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.empty());
        assertEquals(FtpaDecisionOutcomeType.FTPA_REFUSED, getFtpaDecisionOutcomeType(asylumCase).orElse(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"birmingham", "", "harmondsworth"})
    void isListed_should_return_correct_value(String hearingCentre) {
        Optional<HearingCentre> mayBeListCaseHearingCenter = HearingCentre.from(hearingCentre);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(mayBeListCaseHearingCenter);
        assertEquals(mayBeListCaseHearingCenter.isPresent(), isAppealListed(asylumCase));
    }

    @Test
    void should_get_addendum_document_when_present() {
        List<IdValue<DocumentWithMetadata>> addendumDocuments = new ArrayList<>();
        addendumDocuments.add(addendumOne);
        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(addendumDocuments));

        assertEquals(addendumDocuments, getAddendumEvidenceDocuments(asylumCase));
        assertEquals(Optional.of(addendumOne), getLatestAddendumEvidenceDocument(asylumCase));
    }

    @Test
    void should_get_addendum_documents_when_more_than_one_exists() {
        List<IdValue<DocumentWithMetadata>> addendumDocuments = new ArrayList<>();
        addendumDocuments.add(addendumOne);
        addendumDocuments.add(addendumTwo);
        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(addendumDocuments));

        assertEquals(addendumDocuments, getAddendumEvidenceDocuments(asylumCase));
        assertEquals(2, getAddendumEvidenceDocuments(asylumCase).size());
    }

    @Test
    void should_return_empty_list_when_no_addendum_evidence_documents_present() {
        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.empty());

        assertEquals(Collections.emptyList(), getAddendumEvidenceDocuments(asylumCase));
        assertEquals(Optional.empty(), getLatestAddendumEvidenceDocument(asylumCase));
    }

    @Test
    public void testIsLegalRepEjp() {

        Mockito.when(asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class)).thenReturn(Optional.of(legalRepEmailEjp));
        assertTrue(isLegalRepEjp(asylumCase));
    }

    @Test
    public void testIsNotLegalRepEjp() {
        Mockito.when(asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class)).thenReturn(Optional.empty());
        assertFalse(isLegalRepEjp(asylumCase));
    }

    @Test
    void should_throw_when_applies_for_costs_are_not_present() {
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> retrieveLatestApplyForCosts(asylumCase))
            .hasMessage("Applies for costs are not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_retrieve_latest_created_apply_for_costs() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Home office", "Legal representative", applyForCostsCreationDate)),
            new IdValue<>("1", new ApplyForCosts("Unreasonable costs", "Legal representative", "Home office", applyForCostsCreationDate))
        );
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertEquals(applyForCostsList.get(0).getValue(), retrieveLatestApplyForCosts(asylumCase));
    }

    @Test
    void should_retrieve_application_by_id() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Home office", "Legal representative", applyForCostsCreationDate)),
            new IdValue<>("1", new ApplyForCosts("Unreasonable costs", "Legal representative", "Home office", applyForCostsCreationDate))
        );
        DynamicList respondsToCostsList = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertEquals(applyForCostsList.get(0).getValue(), getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST));
    }

    @Test
    void should_throw_if_applies_are_not_present() {
        DynamicList respondsToCostsList = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST))
            .hasMessage("appliesForCost are not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_if_application_is_not_found_by_id() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Home office", "Legal representative", applyForCostsCreationDate)),
            new IdValue<>("1", new ApplyForCosts("Unreasonable costs", "Legal representative", "Home office", applyForCostsCreationDate))
        );
        DynamicList respondsToCostsList = new DynamicList(new Value("3", "Costs 3, Wasted costs, 24 Nov 2023"), List.of(new Value("3", "Costs 3, Wasted costs, 24 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertThatThrownBy(() -> getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST))
            .hasMessage("Apply for costs with id 3 not found")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_check_if_logged_user_is_home_office() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Home office", "Wasted costs"))
        );
        DynamicList respondsToCostsList = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertTrue(isLoggedUserIsHomeOffice(asylumCase, testFunc -> getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST)));
    }

    @Test
    void should_throw_if_logged_user_is__of_incorrect_type() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Tribunal", "Wasted costs"))
        );
        DynamicList respondsToCostsList = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertThatThrownBy(() -> isLoggedUserIsHomeOffice(asylumCase, testFunc -> getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST)))
            .hasMessage("Correct applicant type is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_build_proper_pair_with_applicant_and_respondent() {
        DynamicList selectedValue = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Legal representative", "Home office", applyForCostsCreationDate))
        );

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(selectedValue));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        ImmutablePair<String, String> getApplicantAndRespondent = getApplicantAndRespondent(asylumCase, testFunc -> getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST));

        assertEquals("Legal representative", getApplicantAndRespondent.getRight());
        assertEquals("Home office", getApplicantAndRespondent.getLeft());
    }

    @Test
    void should_throw_if_applicant_type_is_not_correct() {
        DynamicList selectedValue = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Tribunal", "Case officer", applyForCostsCreationDate))
        );

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(selectedValue));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertThatThrownBy(() -> getApplicantAndRespondent(asylumCase, testFunc -> getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST)))
            .hasMessage("Correct applicant type is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_if_respondent_type_is_not_correct() {
        DynamicList selectedValue = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Case officer", "Tribunal", applyForCostsCreationDate))
        );

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(selectedValue));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertThatThrownBy(() -> getApplicantAndRespondent(asylumCase, testFunc -> getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST)))
            .hasMessage("Correct respondent type is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void generateAppellantPin_return_existing_pin_if_present() {
        PinInPostDetails existingPin = PinInPostDetails.builder()
            .accessCode("123")
            .expiryDate(LocalDate.now().plusDays(30).toString())
            .pinUsed(YesOrNo.NO)
            .build();

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.class))
            .thenReturn(Optional.of(existingPin));

        assertEquals(existingPin, generateAppellantPinIfNotPresent(asylumCase));
    }

    @Test
    void generateAppellantPin_generate_new_pin_if_not_present() {
        generatorMockedStatic.when(() -> AccessCodeGenerator.generateAccessCode())
            .thenReturn(generatedCode);

        PinInPostDetails generatedPinDetails = generateAppellantPinIfNotPresent(asylumCaseSpy);

        assertEquals(generatedCode, generatedPinDetails.getAccessCode());
        assertEquals(LocalDate.now().plusDays(30).toString(), generatedPinDetails.getExpiryDate());
        assertEquals(NO, generatedPinDetails.getPinUsed());
    }

    @Test
    void submissionOutOfTime_should_return_true() {
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(isSubmissionOutOfTime(asylumCase));
    }

    @Test
    void submissionOutOfTime_should_return_false() {
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(isSubmissionOutOfTime(asylumCase));
    }

    @Test
    void should_return_true_if_in_country_is_present() {
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(hasAppellantAddressInCountryOrOutOfCountry(asylumCase));
    }

    @Test
    void should_return_true_if_ooc_is_present() {
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(hasAppellantAddressInCountryOrOutOfCountry(asylumCase));
    }

    @Test
    void should_return_false_if_neither_in_country_nor_ooc_is_present() {
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertFalse(hasAppellantAddressInCountryOrOutOfCountry(asylumCase));
    }

    @Test
    void should_return_true_if_appellant_is_detained_in_other_facility() {
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        assertTrue(hasAppellantAddressInCountryOrOutOfCountry(asylumCase));
    }

    @Test
    void should_return_false_if_appellant_is_detained_in_non_other_facility() {
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        assertFalse(hasAppellantAddressInCountryOrOutOfCountry(asylumCase));
    }

    @ParameterizedTest
    @CsvSource({
        "14000, 8000, 60.00",
        "8000, 14000, 60.00",
        "10000, 10000, 0.00"
    })
    void should_return_absolute_fee_amount_even_when_negative_difference(String originalFeeTotal, String newFeeTotal, String expectedDifference) {
        String feeDifference = calculateFeeDifference(originalFeeTotal, newFeeTotal);
        assertEquals(expectedDifference, feeDifference);
    }

    @Test
    void should_return_true_when_appellant_is_in_detention_and_one_of_facility_types_matches() {
        Mockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        Mockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));

        assertTrue(AsylumCaseUtils.isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC));
        assertTrue(AsylumCaseUtils.isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC, DetentionFacility.PRISON));
    }

    @Test
    void should_return_false_when_appellant_is_in_detention_and_none_of_facility_types_matches() {
        Mockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        Mockito.when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        assertFalse(AsylumCaseUtils.isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC));
        assertFalse(AsylumCaseUtils.isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC, DetentionFacility.PRISON));
    }

    @Test
    void should_return_false_when_appellant_is_not_in_detention_for_multipole_facility_types() {
        Mockito.when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertFalse(AsylumCaseUtils.isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC));
        assertFalse(AsylumCaseUtils.isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC, DetentionFacility.PRISON));
    }

    @Test
    void should_return_true_when_appellant_is_in_detention_and_facility_type_matches() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        
        assertTrue(isDetainedInFacilityType(asylumCase, DetentionFacility.IRC));
    }

    @Test
    void should_return_false_when_appellant_is_not_in_detention() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));
        
        assertFalse(isDetainedInFacilityType(asylumCase, DetentionFacility.IRC));
    }

    @Test
    void should_return_false_when_facility_type_does_not_match() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));
        
        assertFalse(isDetainedInFacilityType(asylumCase, DetentionFacility.IRC));
    }

    @Test
    void should_return_false_when_detention_facility_is_empty() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        
        assertFalse(isDetainedInFacilityType(asylumCase, DetentionFacility.IRC));
    }

    @ParameterizedTest
    @CsvSource({
        "immigrationRemovalCentre, IRC",
        "prison, PRISON",
        "other, OTHER"
    })
    void should_return_true_for_all_facility_types_when_appellant_is_detained(String detentionFacilityValue, DetentionFacility facilityType) {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(detentionFacilityValue));
        
        assertTrue(isDetainedInFacilityType(asylumCase, facilityType));
    }

    @Test
    void should_return_true_when_appellant_is_detained_in_any_of_the_specified_facility_types() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        assertTrue(
            isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC, DetentionFacility.PRISON, DetentionFacility.OTHER));
    }

    @Test
    void should_return_true_when_appellant_is_detained_in_first_specified_facility_type() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));

        assertTrue(
            isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC, DetentionFacility.PRISON));
    }

    @Test
    void should_return_false_when_appellant_is_detained_in_none_of_the_specified_facility_types() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        assertFalse(
            isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC, DetentionFacility.PRISON));
    }

    @Test
    void should_return_false_when_appellant_is_not_detained_for_facility_types_check() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertFalse(
            isDetainedInOneOfFacilityTypes(asylumCase, DetentionFacility.IRC, DetentionFacility.PRISON, DetentionFacility.OTHER));
    }

    @Test
    void should_return_false_when_no_facility_types_specified() {
        assertFalse(isDetainedInOneOfFacilityTypes(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(strings = {"RP", "DC"})
    void should_return_true_for_fee_exempt_appeal_types(String appealTypeValue) {
        AppealType appealType = AppealType.valueOf(appealTypeValue);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        assertTrue(isFeeExemptAppeal(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PA", "EA", "HU", "EU", "AG"})
    void should_return_false_for_non_fee_exempt_appeal_types(String appealTypeValue) {
        AppealType appealType = AppealType.valueOf(appealTypeValue);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        assertFalse(isFeeExemptAppeal(asylumCase));
    }

    @Test
    void should_return_false_when_appeal_type_is_not_present() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());

        assertFalse(isFeeExemptAppeal(asylumCase));
    }

    @Test
    void should_return_true_when_has_hearing_channel() {
        DynamicList hearingChannelList = new DynamicList(
                new Value("some", "Value"),
                List.of(new Value("INTER", "In Person"),
                        new Value("NA", "Not in Attendance"),
                        new Value("VID", "Video"),
                        new Value("TEL", "Telephone"))
        );

        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannelList));

        assertTrue(hasHearingChannel(asylumCase, "INTER"));
    }

    @Test
    void should_return_false_when_missing_hearing_channel() {
        DynamicList hearingChannelList = new DynamicList(
                new Value("some", "Value"),
                List.of(new Value("NA", "Not in Attendance"),
                        new Value("VID", "Video"),
                        new Value("TEL", "Telephone"))
        );

        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannelList));

        assertFalse(hasHearingChannel(asylumCase, "INTER"));
    }

    @Test
    void should_return_false_when_hearing_channel_is_empty_for_video() {
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.empty());

        assertFalse(hasHearingChannel(asylumCase, "INTER"));
    }

}
