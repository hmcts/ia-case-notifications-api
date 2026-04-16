package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils.getDocumentWithMetadata;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.io.IOException;
import java.util.Collections;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamRecordOutOfTimeDecisionCanProceedPersonalisationTest {
    final DocumentWithMetadata responseLetter = getDocumentWithMetadata(
        "1", "non-ada-record-out-of-time-can-proceed", "some other desc", DocumentTag.RECORD_OUT_OF_TIME_DECISION_DOCUMENT);
    final IdValue<DocumentWithMetadata> responseLetterId = new IdValue<>("1", responseLetter);
    private final String nonAdaPrefix = "IAFT - SERVE IN PERSON";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final JSONObject jsonObject = new JSONObject("{\"title\": \"JsonDocument\"}");
    @Mock
    AsylumCase asylumCase;
    @Mock
    DetEmailService detEmailService;
    @Mock
    DocumentDownloadClient documentDownloadClient;
    private DetentionEngagementTeamRecordOutOfTimeDecisionCanProceedPersonalisation detentionEngagementTeamRecordOutOfTimeDecisionCanProceedPersonalisation;

    DetentionEngagementTeamRecordOutOfTimeDecisionCanProceedPersonalisationTest() {
    }

    @BeforeEach
    void setup() throws NotificationClientException, IOException {
        String templateId = "someTemplateId";
        detentionEngagementTeamRecordOutOfTimeDecisionCanProceedPersonalisation = new DetentionEngagementTeamRecordOutOfTimeDecisionCanProceedPersonalisation(
            templateId,
            nonAdaPrefix,
            detEmailService,
            documentDownloadClient
        );
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(responseLetterId)));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonObject);
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_INTERNAL_NON_ADA_RECORD_OUT_OF_TIME_DECISION_CAN_PROCEED",
            detentionEngagementTeamRecordOutOfTimeDecisionCanProceedPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_det_email_address() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(detentionEngagementTeamEmail));

        assertTrue(
            detentionEngagementTeamRecordOutOfTimeDecisionCanProceedPersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamRecordOutOfTimeDecisionCanProceedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamRecordOutOfTimeDecisionCanProceedPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_personalisation_of_all_information() throws NotificationClientException, IOException {
        Map<String, Object> personalisation = detentionEngagementTeamRecordOutOfTimeDecisionCanProceedPersonalisation.getPersonalisationForLink(asylumCase);

        assertThat(personalisation)
            .containsEntry("subjectPrefix", nonAdaPrefix)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("documentLink", jsonObject);
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> detentionEngagementTeamRecordOutOfTimeDecisionCanProceedPersonalisation.getPersonalisationForLink((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_throw_exception_when_hearing_bundle_is_empty() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> detentionEngagementTeamRecordOutOfTimeDecisionCanProceedPersonalisation.getPersonalisationForLink(asylumCase));
        assertEquals("recordOutOfTimeDecisionDocument document not available", exception.getMessage());
    }

    @Test
    public void should_throw_exception_when_notification_client_throws_Exception() throws NotificationClientException, IOException {
        when(documentDownloadClient.getJsonObjectFromDocument(responseLetter)).thenThrow(new NotificationClientException("File size is more than 2MB"));
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> detentionEngagementTeamRecordOutOfTimeDecisionCanProceedPersonalisation.getPersonalisationForLink(asylumCase));
        assertEquals("Failed to get Internal 'Appeal can proceed' Letter in compatible format", exception.getMessage());
    }
}