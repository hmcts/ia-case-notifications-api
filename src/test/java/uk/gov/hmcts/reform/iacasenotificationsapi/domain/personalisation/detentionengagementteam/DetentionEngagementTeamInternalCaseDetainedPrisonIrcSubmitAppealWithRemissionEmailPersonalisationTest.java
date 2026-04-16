package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamInternalCaseDetainedPrisonIrcSubmitAppealWithRemissionEmailPersonalisationTest {

    private static final String TEMPLATE_ID = "template123";
    private static final String NON_ADA_PREFIX = "[NON-ADA]";
    private static final long CASE_ID = 1234L;
    private final JSONObject jsonObject = new JSONObject("{\"title\": \"JsonDocument\"}");
    final DocumentWithMetadata internalAppealSubmissionDoc = TestUtils.getDocumentWithMetadata(
        "id", "internal_appeal_submission", "some other desc", DocumentTag.INTERNAL_DETAINED_PRISON_IRC_APPEAL_SUBMISSION);
    final IdValue<DocumentWithMetadata> appealSubmittedBundle = new IdValue<>("1", internalAppealSubmissionDoc);

    @Mock
    private DetentionEmailService detentionEmailService;

    @Mock
    private DocumentDownloadClient documentDownloadClient;

    @Mock
    private AsylumCase asylumCase;

    private DetentionEngagementTeamInternalCaseDetainedPrisonIrcSubmitAppealWithRemissionEmailPersonalisation personalisation;

    @BeforeEach
    void setUp() {
        personalisation =
            new DetentionEngagementTeamInternalCaseDetainedPrisonIrcSubmitAppealWithRemissionEmailPersonalisation(
                TEMPLATE_ID,
                NON_ADA_PREFIX,
                detentionEmailService,
                documentDownloadClient
            );
    }

    @Test
    void should_return_correct_reference_id() {
        String referenceId = personalisation.getReferenceId(CASE_ID);
        assertEquals("1234_INTERNAL_NON_ADA_APPEAL_SUBMITTED", referenceId);
    }

    @Test
    void should_return_recipients_list() {
        when(detentionEmailService.getDetentionEmailAddress(asylumCase)).thenReturn("detention@example.com");

        Set<String> recipients = personalisation.getRecipientsList(asylumCase);

        assertTrue(recipients.contains("detention@example.com"));
    }

    @Test
    void should_return_template_id() {
        assertEquals(TEMPLATE_ID, personalisation.getTemplateId());
    }

    @Test
    void should_return_personalisation_for_link() throws Exception {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("A123"));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("HO123"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Doe"));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(appealSubmittedBundle)));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonObject);

        JSONObject documentJson = new JSONObject().put("url", "http://someurl");
        when(documentDownloadClient.getJsonObjectFromDocument(any())).thenReturn(documentJson);

        Map<String, Object> result = personalisation.getPersonalisationForLink(asylumCase);

        assertThat(result)
            .containsEntry("subjectPrefix", NON_ADA_PREFIX)
            .containsEntry("appealReferenceNumber", "A123")
            .containsEntry("homeOfficeReferenceNumber", "HO123")
            .containsEntry("appellantGivenNames", "John")
            .containsEntry("appellantFamilyName", "Doe")
            .containsEntry("documentLink", documentJson);
    }

    @Test
    public void should_throw_exception_when_appeal_submission_is_empty() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> personalisation.getPersonalisationForLink(asylumCase));
        assertEquals("internalDetainedPrisonIrcAppealSubmission document not available", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_notification_client_throws_Exception() throws Exception {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(appealSubmittedBundle)));
        when(documentDownloadClient.getJsonObjectFromDocument(internalAppealSubmissionDoc)).thenThrow(new NotificationClientException("File size is more than 2MB"));

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> personalisation.getPersonalisationForLink(asylumCase));
        assertEquals("Failed to get Internal Appeal submission Letter in compatible format", exception.getMessage());
    }
}
