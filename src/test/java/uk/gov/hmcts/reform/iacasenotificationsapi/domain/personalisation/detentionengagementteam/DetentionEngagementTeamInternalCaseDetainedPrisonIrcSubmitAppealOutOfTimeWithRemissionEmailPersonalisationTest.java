package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils.getDocumentWithMetadata;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamInternalCaseDetainedPrisonIrcSubmitAppealOutOfTimeWithRemissionEmailPersonalisationTest {

    private static final String TEMPLATE_ID = "template123";
    private static final String NON_ADA_PREFIX = "[NON-ADA]";
    private static final long CASE_ID = 1234L;
    final DocumentWithMetadata internalAppealSubmissionDoc = getDocumentWithMetadata(
        "id", "internal_appeal_submission", "some other desc", DocumentTag.INTERNAL_DETAINED_OUT_OF_TIME_REMISSION_IRC_PRISON_LETTER);
    final IdValue<DocumentWithMetadata> appealSubmittedBundle = new IdValue<>("1", internalAppealSubmissionDoc);
    private final JSONObject jsonObject = new JSONObject("{\"title\": \"JsonDocument\"}");
    @Mock
    private DetentionEmailService detentionEmailService;

    @Mock
    private DocumentDownloadClient documentDownloadClient;

    @Mock
    private AsylumCase asylumCase;

    private DetentionEngagementTeamInternalCaseDetainedPrisonIrcSubmitAppealOutOfTimeWithRemissionEmailPersonalisation personalisation;

    @BeforeEach
    void setUp() {
        personalisation =
            new DetentionEngagementTeamInternalCaseDetainedPrisonIrcSubmitAppealOutOfTimeWithRemissionEmailPersonalisation(
                TEMPLATE_ID,
                NON_ADA_PREFIX,
                detentionEmailService,
                documentDownloadClient
            );
    }

    @Test
    void should_return_correct_reference_id() {
        String referenceId = personalisation.getReferenceId(CASE_ID);
        assertEquals("1234_INTERNAL_NON_ADA_APPEAL_SUBMITTED_OUT_OF_TIME_WITH_REMISSION", referenceId);
    }

    @Test
    void should_return_recipients_list() {
        String detentionEmailAddress = "detention-email@example.com";
        when(detentionEmailService.getDetentionEmailAddress(asylumCase)).thenReturn(detentionEmailAddress);

        assertEquals(Collections.singleton(detentionEmailAddress), personalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_correct_template_id() {
        assertEquals(TEMPLATE_ID, personalisation.getTemplateId());
    }

    @Test
    void should_return_personalisation_when_all_information_given() throws NotificationClientException, IOException {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("someReferenceNumber"));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("someHomeOfficeReferenceNumber"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("someAppellantGivenNames"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("someAppellantFamilyName"));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(Collections.singletonList(appealSubmittedBundle)));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonObject);

        Map<String, Object> personalisation = this.personalisation.getPersonalisationForLink(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", "someReferenceNumber")
            .containsEntry("homeOfficeReferenceNumber", "someHomeOfficeReferenceNumber")
            .containsEntry("appellantGivenNames", "someAppellantGivenNames")
            .containsEntry("appellantFamilyName", "someAppellantFamilyName")
            .containsEntry("subjectPrefix", NON_ADA_PREFIX)
            .containsEntry("documentLink", jsonObject);
    }

    @Test
    void should_return_personalisation_when_only_mandatory_information_given() throws NotificationClientException, IOException {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(Collections.singletonList(appealSubmittedBundle)));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonObject);

        Map<String, Object> personalisation = this.personalisation.getPersonalisationForLink(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("homeOfficeReferenceNumber", "")
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("subjectPrefix", NON_ADA_PREFIX)
            .containsEntry("documentLink", jsonObject);
    }

    @Test
    void should_throw_exception_when_appeal_submission_document_is_empty() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> personalisation.getPersonalisationForLink(asylumCase));
        assertEquals("internalDetainedOutOfTimeRemissionIrcPrisonLetter document not available", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_document_download_client_throws_exception() throws IOException, NotificationClientException {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(Collections.singletonList(appealSubmittedBundle)));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class)))
            .thenThrow(new NotificationClientException("Download failed"));

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> personalisation.getPersonalisationForLink(asylumCase));
        assertEquals("Failed to get Internal Appeal submission Letter in compatible format", exception.getMessage());
    }
}
