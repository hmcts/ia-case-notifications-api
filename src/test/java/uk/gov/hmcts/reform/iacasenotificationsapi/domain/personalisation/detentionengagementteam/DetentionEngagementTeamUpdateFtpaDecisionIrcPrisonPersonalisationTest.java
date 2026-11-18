package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils.getDocumentWithMetadata;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamUpdateFtpaDecisionIrcPrisonPersonalisationTest {

    private final DocumentWithMetadata updateFtpaDecisionLetter = getDocumentWithMetadata(
        "1",
        "ftpa-decision-updated-letter",
        "Internal detained IRC prison update FTPA decision letter",
        DocumentTag.INTERNAL_DETAINED_IRC_PRISON_UPDATE_FTPA_DECISION_LETTER);
    private final IdValue<DocumentWithMetadata> updateFtpaDecisionLetterId = new IdValue<>("1", updateFtpaDecisionLetter);
    private final String templateId = "someTemplateId";
    private final String nonAdaPrefix = "IAFT - SERVE IN PERSON";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String detentionEngagementTeamEmail = "det@email.com";
    private final JSONObject jsonObject = new JSONObject("{\"title\": \"JsonDocument\"}");

    @Mock
    AsylumCase asylumCase;
    @Mock
    DetentionEmailService detentionEmailService;
    @Mock
    DocumentDownloadClient documentDownloadClient;

    private DetentionEngagementTeamUpdateFtpaDecisionIrcPrisonPersonalisation personalisation;

    @BeforeEach
    void setup() throws NotificationClientException, IOException {
        personalisation = new DetentionEngagementTeamUpdateFtpaDecisionIrcPrisonPersonalisation(
            templateId,
            nonAdaPrefix,
            detentionEmailService,
            documentDownloadClient
        );

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(updateFtpaDecisionLetterId)));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonObject);
        when(detentionEmailService.getDetentionEmailAddress(asylumCase)).thenReturn(detentionEngagementTeamEmail);
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_INTERNAL_DETAINED_UPDATE_FTPA_DECISION_IRC_PRISON_DET", personalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, personalisation.getTemplateId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"immigrationRemovalCentre", "prison"})
    void should_return_det_email_address_for_irc_and_prison(String detentionFacility) {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(detentionFacility));

        assertThat(personalisation.getRecipientsList(asylumCase)).containsExactly(detentionEngagementTeamEmail);
    }

    @Test
    void should_return_empty_set_when_appellant_not_in_detention() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertEquals(Collections.emptySet(), personalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_empty_set_when_detention_facility_missing() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());

        assertEquals(Collections.emptySet(), personalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_empty_set_when_detention_facility_is_other() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        assertEquals(Collections.emptySet(), personalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_personalisation_with_document_link() throws NotificationClientException, IOException {
        Map<String, Object> result = personalisation.getPersonalisationForLink(asylumCase);

        assertThat(result)
            .containsEntry("subjectPrefix", nonAdaPrefix)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("documentLink", jsonObject)
            .hasSize(6);
    }

    @Test
    void should_return_empty_strings_when_case_fields_missing() throws NotificationClientException, IOException {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        assertThat(personalisation.getPersonalisationForLink(asylumCase))
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("homeOfficeReferenceNumber", "")
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("documentLink", jsonObject);
    }

    @Test
    void should_throw_when_asylum_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> personalisation.getPersonalisationForLink((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_throw_when_letter_not_on_case() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> personalisation.getPersonalisationForLink(asylumCase));
        assertEquals("internalDetainedIrcPrisonUpdateFtpaDecisionLetter document not available", exception.getMessage());
    }

    @Test
    void should_throw_when_notification_client_exception() throws NotificationClientException, IOException {
        when(documentDownloadClient.getJsonObjectFromDocument(updateFtpaDecisionLetter))
            .thenThrow(new NotificationClientException("File size is more than 2MB"));

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> personalisation.getPersonalisationForLink(asylumCase));
        assertEquals("Failed to get Update FTPA decision letter in compatible format", exception.getMessage());
    }

    @Test
    void should_throw_when_io_exception() throws NotificationClientException, IOException {
        when(documentDownloadClient.getJsonObjectFromDocument(updateFtpaDecisionLetter))
            .thenThrow(new IOException("IO Exception occurred"));

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> personalisation.getPersonalisationForLink(asylumCase));
        assertEquals("Failed to get Update FTPA decision letter in compatible format", exception.getMessage());
    }
}
