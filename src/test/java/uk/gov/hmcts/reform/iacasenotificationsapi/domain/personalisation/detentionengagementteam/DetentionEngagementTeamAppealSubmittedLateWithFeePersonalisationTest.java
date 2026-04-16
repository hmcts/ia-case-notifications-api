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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamAppealSubmittedLateWithFeePersonalisationTest {

    final DocumentWithMetadata feeDecisionLetter = getDocumentWithMetadata(
        "1", "internal-detained-appeal-submitted-out-of-time-with-fee-letter", "Internal detained appeal submitted out of time with fee letter", DocumentTag.INTERNAL_DETAINED_APPEAL_SUBMITTED_OUT_OF_TIME_WITH_FEE_LETTER);
    final IdValue<DocumentWithMetadata> feeDecisionLetterId = new IdValue<>("1", feeDecisionLetter);
    private final String templateId = "someTemplateId";
    private final String nonAdaPrefix = "IAFT - SERVE IN PERSON";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final JSONObject jsonObject = new JSONObject("{\"title\": \"JsonDocument\"}");
    @Mock
    AsylumCase asylumCase;
    @Mock
    DetentionEmailService detentionEmailService;
    @Mock
    DocumentDownloadClient documentDownloadClient;
    private DetentionEngagementTeamAppealSubmittedLateWithFeePersonalisation detentionEngagementTeamAppealSubmittedLateWithFeePersonalisation;

    DetentionEngagementTeamAppealSubmittedLateWithFeePersonalisationTest() {
    }

    @BeforeEach
    void setup() throws NotificationClientException, IOException {
        detentionEngagementTeamAppealSubmittedLateWithFeePersonalisation = new DetentionEngagementTeamAppealSubmittedLateWithFeePersonalisation(
            templateId,
            nonAdaPrefix,
            detentionEmailService,
            documentDownloadClient
        );

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(feeDecisionLetterId)));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonObject);
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_INTERNAL_NON_ADA_APPEAL_SUBMITTED_LATE_WITH_FEE",
            detentionEngagementTeamAppealSubmittedLateWithFeePersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, detentionEngagementTeamAppealSubmittedLateWithFeePersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_det_email_address() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detentionEmailService.getDetentionEmailAddress(asylumCase)).thenReturn(detentionEngagementTeamEmail);

        assertTrue(
            detentionEngagementTeamAppealSubmittedLateWithFeePersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamAppealSubmittedLateWithFeePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamAppealSubmittedLateWithFeePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_personalisation_of_all_information() throws NotificationClientException, IOException {
        Map<String, Object> personalisation = detentionEngagementTeamAppealSubmittedLateWithFeePersonalisation.getPersonalisationForLink(asylumCase);

        assertThat(personalisation)
            .containsEntry("subjectPrefix", nonAdaPrefix)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("documentLink", jsonObject);
    }

    @Test
    void should_return_personalisation_with_empty_strings_when_case_fields_are_empty() throws NotificationClientException, IOException {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, Object> personalisation = detentionEngagementTeamAppealSubmittedLateWithFeePersonalisation.getPersonalisationForLink(asylumCase);

        assertThat(personalisation)
            .containsEntry("subjectPrefix", nonAdaPrefix)
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("homeOfficeReferenceNumber", "")
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("documentLink", jsonObject);
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> detentionEngagementTeamAppealSubmittedLateWithFeePersonalisation.getPersonalisationForLink((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_throw_exception_when_fee_decision_document_is_empty() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> detentionEngagementTeamAppealSubmittedLateWithFeePersonalisation.getPersonalisationForLink(asylumCase));
        assertEquals("internalDetainedAppealSubmittedOutOfTimeWithFeeLetter document not available", exception.getMessage());
    }

    @Test
    public void should_throw_exception_when_notification_client_throws_Exception() throws NotificationClientException, IOException {
        when(documentDownloadClient.getJsonObjectFromDocument(feeDecisionLetter)).thenThrow(new NotificationClientException("File size is more than 2MB"));
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> detentionEngagementTeamAppealSubmittedLateWithFeePersonalisation.getPersonalisationForLink(asylumCase));
        assertEquals("Failed to get Internal 'Appeal can proceed' Letter in compatible format", exception.getMessage());
    }

    @Test
    public void should_throw_exception_when_io_exception_occurs() throws NotificationClientException, IOException {
        when(documentDownloadClient.getJsonObjectFromDocument(feeDecisionLetter)).thenThrow(new IOException("IO Exception occurred"));
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> detentionEngagementTeamAppealSubmittedLateWithFeePersonalisation.getPersonalisationForLink(asylumCase));
        assertEquals("Failed to get Internal 'Appeal can proceed' Letter in compatible format", exception.getMessage());
    }
}