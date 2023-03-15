package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils.getDocumentWithMetadata;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamAdaSuitabilityReviewPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    DetEmailService detEmailService;
    @Mock
    DocumentDownloadClient documentDownloadClient;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;

    private final String templateId = "someTemplateId";
    private final String iaExUiFrontendUrl = "http://somefrontendurl";
    private final String adaPrefix = "Accelerated detained appeal";
    private final String hearingDateTime = "2019-08-27T14:25:15.000";
    private String hearingDate = "2019-08-27";
    private String hearingTime = "14:25";
    private final String detEmailAddress = "legalrep@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String listingReference = "listingReference";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "customer.services@example.com";
    private final String hearingRequirementVulnerabilities = "No special adjustments are being made to accommodate vulnerabilities";
    private final String hearingRequirementMultimedia = "No multimedia equipment is being provided";
    private final String hearingRequirementSingleSexCourt = "The court will not be single sex";
    private final String hearingRequirementInCameraCourt = "The hearing will be held in public court";
    private final String hearingRequirementOther = "No other adjustments are being made";
    private final String remoteVideoCallTribunalResponse = "No adjustments have been made for a remote hearing";
    private final String pastExperienceTribunalResponse = "No adjustments for previous experiences are being made";
    private final JSONObject jsonObject = new JSONObject();
    DocumentWithMetadata adaSuitabilityDoc = getDocumentWithMetadata(
            "id", "ada_suitability", "some other desc", DocumentTag.ADA_SUITABILITY);
    IdValue<DocumentWithMetadata> adaSuitabilityDocId = new IdValue<>("1", adaSuitabilityDoc);

    private DetentionEngagementTeamAdaSuitabilityReviewPersonalisation
            detentionEngagementTeamAdaSuitabilityReviewPersonalisation;

    DetentionEngagementTeamAdaSuitabilityReviewPersonalisationTest() {
    }

    @BeforeEach
    public void setUp() throws NotificationClientException, IOException {

        detentionEngagementTeamAdaSuitabilityReviewPersonalisation =
                new DetentionEngagementTeamAdaSuitabilityReviewPersonalisation(
                        templateId,
                        iaExUiFrontendUrl,
                        adaPrefix,
                        customerServicesProvider,
                        detEmailService,
                        documentDownloadClient,
                        dateTimeExtractor,
                        hearingDetailsFinder
                );

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));

        when(asylumCase.read(SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)).thenReturn(Optional.of(AdaSuitabilityReviewDecision.SUITABLE));

        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(hearingRequirementVulnerabilities));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(hearingRequirementMultimedia));
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(hearingRequirementSingleSexCourt));
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(hearingRequirementInCameraCourt));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(hearingRequirementOther));
        when(asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(remoteVideoCallTribunalResponse));
        when(asylumCase.read(PAST_EXPERIENCES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(pastExperienceTribunalResponse));

        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);

        when(asylumCase.read(TRIBUNAL_DOCUMENTS)).thenReturn(Optional.of(newArrayList(adaSuitabilityDocId)));
        when(documentDownloadClient.getJsonObjectFromDocument(adaSuitabilityDoc)).thenReturn(jsonObject);

        when(customerServicesProvider.getCustomerServicesTelephone()).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getCustomerServicesEmail()).thenReturn(customerServicesEmail);
        when(detEmailService.getAdaDetEmailAddress()).thenReturn(detEmailAddress);
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_ADA_SUITABILITY_DETERMINED_DET",
                detentionEngagementTeamAdaSuitabilityReviewPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(detentionEngagementTeamAdaSuitabilityReviewPersonalisation.getRecipientsList(asylumCase)
                .contains(detEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
                () -> detentionEngagementTeamAdaSuitabilityReviewPersonalisation.getPersonalisationForLink((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_suitability_review_is_missing() {
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)).thenReturn(Optional.empty());
        assertThatThrownBy(
                () -> detentionEngagementTeamAdaSuitabilityReviewPersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessage("ADA suitability decision missing");
    }

    @ParameterizedTest
    @EnumSource(value = AsylumCaseDefinition.class, names = {
        "VULNERABILITIES_TRIBUNAL_RESPONSE",
        "MULTIMEDIA_TRIBUNAL_RESPONSE",
        "SINGLE_SEX_COURT_TRIBUNAL_RESPONSE",
        "IN_CAMERA_COURT_TRIBUNAL_RESPONSE",
        "ADDITIONAL_TRIBUNAL_RESPONSE",
        "REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE",
        "PAST_EXPERIENCES_TRIBUNAL_RESPONSE"
    })
    public void should_default_hearing_requirements_tribunal_responses_when_missing(AsylumCaseDefinition asylumCaseDefinition) throws NotificationClientException, IOException {
        when(asylumCase.read(asylumCaseDefinition, String.class)).thenReturn(Optional.empty());

        Map<String, Object> personalisation =
                detentionEngagementTeamAdaSuitabilityReviewPersonalisation.getPersonalisationForLink(asylumCase);
        if (asylumCaseDefinition.equals(VULNERABILITIES_TRIBUNAL_RESPONSE)) {
            assertEquals(hearingRequirementVulnerabilities, personalisation.get("hearingRequirementVulnerabilities"));
        } else if (asylumCaseDefinition.equals(MULTIMEDIA_TRIBUNAL_RESPONSE)) {
            assertEquals(hearingRequirementMultimedia, personalisation.get("hearingRequirementMultimedia"));

        } else if (asylumCaseDefinition.equals(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE)) {
            assertEquals(hearingRequirementSingleSexCourt, personalisation.get("hearingRequirementSingleSexCourt"));

        } else if (asylumCaseDefinition.equals(IN_CAMERA_COURT_TRIBUNAL_RESPONSE)) {
            assertEquals(hearingRequirementInCameraCourt, personalisation.get("hearingRequirementInCameraCourt"));

        } else if (asylumCaseDefinition.equals(ADDITIONAL_TRIBUNAL_RESPONSE)) {
            assertEquals(hearingRequirementOther, personalisation.get("hearingRequirementOther"));

        } else if (asylumCaseDefinition.equals(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE)) {
            assertEquals(remoteVideoCallTribunalResponse, personalisation.get("remoteVideoCallTribunalResponse"));

        } else if (asylumCaseDefinition.equals(PAST_EXPERIENCES_TRIBUNAL_RESPONSE)) {
            assertEquals(pastExperienceTribunalResponse, personalisation.get("pastExperienceTribunalResponse"));
        }
    }

    @Test
    public void should_throw_exception_on_personalisation_when_ada_suitability_document_is_missing() throws NotificationClientException, IOException {
        when(asylumCase.read(TRIBUNAL_DOCUMENTS)).thenReturn(Optional.empty());
        when(documentDownloadClient.getJsonObjectFromDocument(adaSuitabilityDoc)).thenReturn(jsonObject);


        assertThatThrownBy(
                () -> detentionEngagementTeamAdaSuitabilityReviewPersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("ADA suitability document not available");
    }

    @ParameterizedTest
    @EnumSource(value = AdaSuitabilityReviewDecision.class)
    public void should_return_personalisation_when_all_information_given_maintain(AdaSuitabilityReviewDecision adaSuitabilityReviewDecision) throws NotificationClientException, IOException {
        final Map<String, Object> expectedPersonalisation =
                ImmutableMap
                        .<String, Object>builder()
                        .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                        .put("subjectPrefix", adaPrefix)
                        .put("appealReferenceNumber", appealReferenceNumber)
                        .put("ariaListingReferenceIfPresent", listingReference)
                        .put("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
                        .put("appellantGivenNames", appellantGivenNames)
                        .put("appellantFamilyName", appellantFamilyName)
                        .put("hearingDate", hearingDateTime)
                        .put("adaSuitability", adaSuitabilityReviewDecision)
                        .put("hearingRequirementVulnerabilities", hearingRequirementVulnerabilities)
                        .put("hearingRequirementMultimedia", hearingRequirementMultimedia)
                        .put("hearingRequirementSingleSexCourt", hearingRequirementSingleSexCourt)
                        .put("hearingRequirementInCameraCourt", hearingRequirementInCameraCourt)
                        .put("hearingRequirementOther", hearingRequirementOther)
                        .put("remoteVideoCallTribunalResponse", remoteVideoCallTribunalResponse)
                        .put("pastExperienceTribunalResponse", pastExperienceTribunalResponse)
                        .put("documentDownloadTitle", "some title")
                        .put("linkToDownloadDocument", "some link")
                        .put("linkToOnlineService", iaExUiFrontendUrl)
                        .build();

        Map<String, Object> actualPersonalisation =
                detentionEngagementTeamAdaSuitabilityReviewPersonalisation.getPersonalisationForLink(asylumCase);

        assertThat(actualPersonalisation).isEqualToComparingOnlyGivenFields(expectedPersonalisation);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}