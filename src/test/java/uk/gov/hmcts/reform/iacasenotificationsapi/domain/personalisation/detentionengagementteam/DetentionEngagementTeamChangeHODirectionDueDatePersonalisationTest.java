package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils.compareStringsAndJsonObjects;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamChangeHODirectionDueDatePersonalisationTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    private DocumentDownloadClient documentDownloadClient;
    @Mock
    private DetEmailService detEmailService;
    @Mock
    private PersonalisationProvider personalisationProvider;
    @Mock
    JSONObject jsonDocument;
    private String templateId = "templateId";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String adaPrefix = "ADA - SERVE BY POST";
    private final String nonAdaPrefix = "IAFT - SERVE BY POST";
    private final Long caseId = 12345L;
    private DetentionEngagementTeamChangeHODirectionDueDatePersonalisation detentionEngagementTeamChangeHODirectionDueDatePersonalisation;

    DocumentWithMetadata changeHODirectionDueDateLetter = TestUtils.getDocumentWithMetadata(
            "id", "detained-appellant-change-ho-direction-due-date-letter", "some other desc", DocumentTag.INTERNAL_HO_CHANGE_DIRECTION_DUE_DATE_LETTER);
    IdValue<DocumentWithMetadata> notificationDocs = new IdValue<>("1", changeHODirectionDueDateLetter);

    DetentionEngagementTeamChangeHODirectionDueDatePersonalisationTest() {
    }

    @BeforeEach
    public void setup() throws NotificationClientException, IOException {
        Map<String, String> appelantInfo = new HashMap<>();
        appelantInfo.put("appellantGivenNames", appellantGivenNames);
        appelantInfo.put("appellantFamilyName", appellantFamilyName);
        appelantInfo.put("homeOfficeReferenceNumber", homeOfficeReferenceNumber);
        appelantInfo.put("appealReferenceNumber", appealReferenceNumber);

        when(personalisationProvider.getAppellantPersonalisation(asylumCase)).thenReturn(appelantInfo);
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(notificationDocs)));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonDocument);

        detentionEngagementTeamChangeHODirectionDueDatePersonalisation = new DetentionEngagementTeamChangeHODirectionDueDatePersonalisation(
                templateId,
                detEmailService,
                personalisationProvider,
                documentDownloadClient,
                adaPrefix, nonAdaPrefix);
    }

    @Test
    public void should_return_given_template_id_detained() {
        assertEquals(
                templateId,
                detentionEngagementTeamChangeHODirectionDueDatePersonalisation.getTemplateId(asylumCase)
        );
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_INTERNAL_DET_CHANGE_HO_DIRECTION_DUE_DATE_EMAIL",
                detentionEngagementTeamChangeHODirectionDueDatePersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_det_email_address() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(detentionEngagementTeamEmail));

        assertTrue(
                detentionEngagementTeamChangeHODirectionDueDatePersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamChangeHODirectionDueDatePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamChangeHODirectionDueDatePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() throws NotificationClientException, IOException {

        final Map<String, Object> expectedPersonalisation =
                ImmutableMap
                        .<String, Object>builder()
                        .put("subjectPrefix", nonAdaPrefix)
                        .put("appealReferenceNumber", appealReferenceNumber)
                        .put("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
                        .put("appellantGivenNames", appellantGivenNames)
                        .put("appellantFamilyName", appellantFamilyName)
                        .put("documentLink", jsonDocument)
                        .build();

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(NO));

        Map<String, Object> actualPersonalisation =
                detentionEngagementTeamChangeHODirectionDueDatePersonalisation.getPersonalisationForLink(asylumCase);

        assertTrue(compareStringsAndJsonObjects(expectedPersonalisation, actualPersonalisation));
    }

    @Test
    void should_return_personalisation_if_decision_dismissed_for_nonAda() throws NotificationClientException, IOException {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        Map<String, Object> personalisation = detentionEngagementTeamChangeHODirectionDueDatePersonalisation.getPersonalisationForLink(asylumCase);

        assertEquals(adaPrefix, personalisation.get("subjectPrefix"));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> detentionEngagementTeamChangeHODirectionDueDatePersonalisation.getPersonalisationForLink((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_when_appeal_submission_is_empty() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> detentionEngagementTeamChangeHODirectionDueDatePersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("internalHoChangeDirectionDueDateLetter document not available");
    }

    @Test
    public void should_throw_exception_when_notification_client_throws_Exception() throws NotificationClientException, IOException {
        when(documentDownloadClient.getJsonObjectFromDocument(changeHODirectionDueDateLetter)).thenThrow(new NotificationClientException("File size is more than 2MB"));
        assertThatThrownBy(() -> detentionEngagementTeamChangeHODirectionDueDatePersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to get Internal Change HO Direction Due Date Letter in compatible format");
    }
}
