package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils.compareStringsAndJsonObjects;
import static uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils.getDocumentWithMetadata;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;

import java.io.IOException;
import java.util.*;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamMarkAppealAsAdaPersonalisationTest {
    final DocumentWithMetadata markAsAdaLetter = getDocumentWithMetadata(
        "1", "mark-as-ada", "some other desc", DocumentTag.INTERNAL_DET_MARK_AS_ADA_LETTER);
    final IdValue<DocumentWithMetadata> markAsAdaLetterId = new IdValue<>("1", markAsAdaLetter);
    private final String templateId = "someTemplateId";
    private final String adaPrefix = "Accelerated detained appeal";
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    DetEmailService detEmailService;
    @Mock
    DocumentDownloadClient documentDownloadClient;
    @Mock
    AsylumCase asylumCase;
    private JSONObject markAsAdaLetterJsonDocument;

    private DetentionEngagementTeamMarkAppealAsAdaPersonalisation detentionEngagementTeamMarkAppealAsAdaPersonalisation;

    @BeforeEach
    public void setUp() throws NotificationClientException, IOException {
        Map<String, String> appelantInfo = new HashMap<>();
        String appellantGivenNames = "someAppellantGivenNames";
        appelantInfo.put("appellantGivenNames", appellantGivenNames);
        String appellantFamilyName = "someAppellantFamilyName";
        appelantInfo.put("appellantFamilyName", appellantFamilyName);
        String homeOfficeReferenceNumber = "1234-1234-1234-1234";
        appelantInfo.put("homeOfficeReferenceNumber", homeOfficeReferenceNumber);
        String appealReferenceNumber = "someReferenceNumber";
        appelantInfo.put("appealReferenceNumber", appealReferenceNumber);

        when(personalisationProvider.getAppellantPersonalisation(asylumCase)).thenReturn(appelantInfo);
        String detEmailAddress = "legalrep@example.com";
        when(detEmailService.getDetEmailAddress(asylumCase)).thenReturn(detEmailAddress);
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(markAsAdaLetterJsonDocument);

        TestUtils.getDocumentWithMetadataList("docId", "filename", "description", DocumentTag.INTERNAL_DET_MARK_AS_ADA_LETTER);
        markAsAdaLetterJsonDocument = new JSONObject("{\"title\": \"JsonDocument\"}");
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(markAsAdaLetterId)));
        when(documentDownloadClient.getJsonObjectFromDocument(markAsAdaLetter)).thenReturn(markAsAdaLetterJsonDocument);

        detentionEngagementTeamMarkAppealAsAdaPersonalisation =
            new DetentionEngagementTeamMarkAppealAsAdaPersonalisation(
                templateId,
                adaPrefix,
                detEmailService,
                personalisationProvider,
                documentDownloadClient
            );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, detentionEngagementTeamMarkAppealAsAdaPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_INTERNAL_MARK_APPEAL_AS_ADA",
            detentionEngagementTeamMarkAppealAsAdaPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(detentionEngagementTeamEmail));

        assertTrue(
            detentionEngagementTeamMarkAppealAsAdaPersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @Test
    void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamMarkAppealAsAdaPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> detentionEngagementTeamMarkAppealAsAdaPersonalisation.getPersonalisationForLink((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_personalisation_when_letter_for_notification_is_not_found() {
        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> detentionEngagementTeamMarkAppealAsAdaPersonalisation.getPersonalisationForLink((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given_maintain() throws NotificationClientException, IOException {

        final Map<String, Object> expectedPersonalisation =
            ImmutableMap
                .<String, Object>builder()
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("subjectPrefix", adaPrefix)
                .put("documentLink", markAsAdaLetterJsonDocument)
                .build();

        Map<String, Object> actualPersonalisation =
            detentionEngagementTeamMarkAppealAsAdaPersonalisation.getPersonalisationForLink(asylumCase);

        assertTrue(compareStringsAndJsonObjects(expectedPersonalisation, actualPersonalisation));
    }

}