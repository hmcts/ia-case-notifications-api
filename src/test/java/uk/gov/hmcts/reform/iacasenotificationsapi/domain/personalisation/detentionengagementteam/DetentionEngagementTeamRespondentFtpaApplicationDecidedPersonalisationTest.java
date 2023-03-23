package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_REFUSED;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Arrays;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DetentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    DetEmailService detEmailService;
    @Mock
    DocumentDownloadClient documentDownloadClient;

    private final String applicationRefusedOrNotAdmittedTemplateId = "someTemplateId";
    private final String adaPrefix = "Accelerated detained appeal";
    private final String detEmailAddress = "legalrep@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String listingReference = "listingReference";
    String listingReferenceIfPresent = "Listing reference: " + listingReference;
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private JSONObject jsonDocument;
    private DetentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation
        detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation;

    @BeforeEach
    public void setUp() throws NotificationClientException, IOException {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(listingReference));
        when(detEmailService.getAdaDetEmailAddress()).thenReturn(detEmailAddress);
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FTPA_REFUSED));

        String customerServicesTelephone = "555 555 555";
        String customerServicesEmail = "customer.services@example.com";
        when(customerServicesProvider.getCustomerServicesTelephone()).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getCustomerServicesEmail()).thenReturn(customerServicesEmail);

        List<IdValue<DocumentWithDescription>> documents = Arrays.asList(
            new IdValue<>("docId", getDocumentWithDescription("docId", "filename", "description"))
        );
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_DOCUMENT)).thenReturn(Optional.of(documents));
        when(asylumCase.read(FTPA_RESPONDENT_GROUNDS_DOCUMENTS)).thenReturn(Optional.of(documents));

        jsonDocument =  new JSONObject("{\"title\": \"FTPA Document\"}");
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class)))
            .thenReturn(jsonDocument);
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class)))
            .thenReturn(jsonDocument);

        detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation =
            new DetentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation(
                applicationRefusedOrNotAdmittedTemplateId,
                adaPrefix,
                customerServicesProvider,
                detEmailService,
                documentDownloadClient
            );
    }

    @Test
    public void should_return_given_template_id() {
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FTPA_REFUSED));

        assertEquals(applicationRefusedOrNotAdmittedTemplateId, detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_RESPONDENT_FTPA_APPLICATION_DECISION_DETENTION_ENGAGEMENT_TEAM",
            detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getRecipientsList(asylumCase)
            .contains(detEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getPersonalisationForLink((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_ftpa_respondent_outcome_type_is_missing() {
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());


        assertThatThrownBy(
            () -> detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getPersonalisationForLink(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaRespondentDecisionOutcomeType is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_decision_and_reason_document_is_missing() {
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_DOCUMENT)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getPersonalisationForLink(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaRespondentDecisionDocument is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_respondent_grounds_document_is_missing() {
        when(asylumCase.read(FTPA_RESPONDENT_GROUNDS_DOCUMENTS)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getPersonalisationForLink(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaRespondentGroundsDocuments is not present");
    }

    @Test
    public void should_return_personalisation_when_all_information_given_refused() {

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(FTPA_REFUSED));
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.empty());

        final Map<String, Object> expectedPersonalisation =
            ImmutableMap
                .<String, Object>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("subjectPrefix", adaPrefix)
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("ariaListingReferenceIfPresent", listingReferenceIfPresent)
                .put("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("applicationDecision", "been refused")
                .put("ftpaDecisionAndReasonsDocumentDownloadLink", jsonDocument)
                .put("homeOfficeFtpaApplicationDownloadLink", jsonDocument)
                .build();

        Map<String, Object> actualPersonalisation =
            detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getPersonalisationForLink(asylumCase);

        assertTrue(compareStringsAndJsonObjects(expectedPersonalisation, actualPersonalisation));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_not_admitted() {

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(FTPA_NOT_ADMITTED));

        final Map<String, Object> expectedPersonalisation =
            ImmutableMap
                .<String, Object>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("subjectPrefix", adaPrefix)
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("ariaListingReferenceIfPresent", listingReferenceIfPresent)
                .put("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("applicationDecision", "not been admitted")
                .put("ftpaDecisionAndReasonsDocumentDownloadLink", jsonDocument)
                .put("homeOfficeFtpaApplicationDownloadLink", jsonDocument)
                .build();

        Map<String, Object> actualPersonalisation =
            detentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation.getPersonalisationForLink(asylumCase);

        assertTrue(compareStringsAndJsonObjects(expectedPersonalisation, actualPersonalisation));
    }
}
