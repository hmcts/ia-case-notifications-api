package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealReviewOutcome;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DetentionEngagementTeamRequestResponseReviewPersonalisationTest {

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
    private final String hearingDate = "2023-03-15T10:13:38.410992";
    private final String hearingDateFormatted = "15 March 2023";
    private final String detEmailAddress = "legalrep@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String listingReference = "listingReference";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "customer.services@example.com";
    private JSONObject appealResponseJsonDocument;

    private DetentionEngagementTeamRequestResponseReviewPersonalisation
        detentionEngagementTeamRequestResponseReviewPersonalisation;

    @BeforeEach
    public void setUp() throws NotificationClientException, IOException {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDate));
        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class))
            .thenReturn(Optional.of(AppealReviewOutcome.DECISION_WITHDRAWN));

        List<IdValue<DocumentWithMetadata>> appealResponseDocuments = TestUtils.getDocumentWithMetadataList("docId", "filename", "description", DocumentTag.APPEAL_RESPONSE);
        appealResponseJsonDocument =  new JSONObject("{\"title\": \"Home Office Response JsonDocument\"}");
        when(asylumCase.read(RESPONDENT_DOCUMENTS)).thenReturn(Optional.of(appealResponseDocuments));

        when(customerServicesProvider.getCustomerServicesTelephone()).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getCustomerServicesEmail()).thenReturn(customerServicesEmail);
        when(detEmailService.getAdaDetEmailAddress()).thenReturn(detEmailAddress);
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(appealResponseJsonDocument);
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingDate(hearingDate)).thenReturn(hearingDateFormatted);

        detentionEngagementTeamRequestResponseReviewPersonalisation =
            new DetentionEngagementTeamRequestResponseReviewPersonalisation(
                templateId,
                iaExUiFrontendUrl,
                adaPrefix,
                customerServicesProvider,
                detEmailService,
                documentDownloadClient,
                dateTimeExtractor,
                hearingDetailsFinder
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, detentionEngagementTeamRequestResponseReviewPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_DETENTION_ENGAGEMENT_TEAM_REQUEST_RESPONSE_REVIEW",
            detentionEngagementTeamRequestResponseReviewPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(detentionEngagementTeamRequestResponseReviewPersonalisation.getRecipientsList(asylumCase)
            .contains(detEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> detentionEngagementTeamRequestResponseReviewPersonalisation.getPersonalisationForLink((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_appeal_review_outcome_is_missing() {
        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> detentionEngagementTeamRequestResponseReviewPersonalisation.getPersonalisationForLink(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appeal review outcome is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_appeal_response_document_is_missing() {
        when(asylumCase.read(RESPONDENT_DOCUMENTS)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> detentionEngagementTeamRequestResponseReviewPersonalisation.getPersonalisationForLink(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Home Office response document not available");
    }

    @Test
    public void should_return_personalisation_when_all_information_given_maintain() throws NotificationClientException, IOException {

        appealResponseJsonDocument =  new JSONObject("{\"title\": \"Home Office Response JsonDocument\"}");
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(appealResponseJsonDocument);

        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class))
            .thenReturn(Optional.of(AppealReviewOutcome.DECISION_MAINTAINED));

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
                .put("appealReviewOutcome", "maintain")
                .put("hearingDate", hearingDateFormatted)
                .put("documentDownloadTitle", "Home Office Response")
                .put("linkToDownloadDocument", appealResponseJsonDocument)
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();

        Map<String, Object> actualPersonalisation =
            detentionEngagementTeamRequestResponseReviewPersonalisation.getPersonalisationForLink(asylumCase);

        assertThat(actualPersonalisation).isEqualToComparingOnlyGivenFields(expectedPersonalisation);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_return_personalisation_when_all_information_given_withdrawn() throws NotificationClientException, IOException {

        appealResponseJsonDocument =  new JSONObject("{\"title\": \"Withdrawal letter JsonDocument\"}");
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(appealResponseJsonDocument);

        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class))
            .thenReturn(Optional.of(AppealReviewOutcome.DECISION_WITHDRAWN));

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
                .put("appealReviewOutcome", "withdraw")
                .put("hearingDate", hearingDateFormatted)
                .put("documentDownloadTitle", "Withdrawal Letter")
                .put("linkToDownloadDocument", appealResponseJsonDocument)
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();

        Map<String, Object> actualPersonalisation =
            detentionEngagementTeamRequestResponseReviewPersonalisation.getPersonalisationForLink(asylumCase);

        assertThat(actualPersonalisation).isEqualToComparingOnlyGivenFields(expectedPersonalisation);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
