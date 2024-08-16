package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamUpdateTribunalDecisionPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    private DocumentDownloadClient documentDownloadClient;
    @Mock
    private DetEmailService detEmailService;
    @Mock
    JSONObject jsonDocument;
    @Mock
    PersonalisationProvider personalisationProvider;

    private String templateIdWithDecision = "templateId";
    private String templateIdWithoutDecision = "templateId";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String nonAdaPrefix = "IAFT - SERVE IN PERSON";
    private final String adaPrefix = "ADA - SERVE IN PERSON";
    private final Long caseId = 12345L;
    private DetentionEngagementTeamUpdateTribunalDecisionPersonalisation detentionEngagementTeamUpdateTribunalDecisionPersonalisation;

    DocumentWithMetadata updateTribunalDecisionRule31Doc = TestUtils.getDocumentWithMetadata(
        "id", "internal-update-tribunal-decision-r31-letter", "some other desc", DocumentTag.INTERNAL_DETAINED_UPDATE_TRIBUNAL_DECISION_R31_LETTER);
    IdValue<DocumentWithMetadata> updateTribunalDecisionR31Bundle = new IdValue<>("1", updateTribunalDecisionRule31Doc);

    DocumentWithMetadata updateTribunalDecisionRule32Doc = TestUtils.getDocumentWithMetadata(
        "id", "internal-update-tribunal-decision-r32-letter", "some other desc", DocumentTag.INTERNAL_DETAINED_UPDATE_TRIBUNAL_DECISION_R32_LETTER);
    IdValue<DocumentWithMetadata> updateTribunalDecisionR32Bundle = new IdValue<>("1", updateTribunalDecisionRule32Doc);

    DocumentWithMetadata internalUpdateDecisionAndReasonlLetter = getDocumentWithMetadata(
        "1", "Update decision and reasons letter", "some other desc", DocumentTag.UPDATED_FINAL_DECISION_AND_REASONS_PDF);
    IdValue<DocumentWithMetadata> internalUpdateDecisionAndReasonLetterId = new IdValue<>("1", internalUpdateDecisionAndReasonlLetter);

    @BeforeEach
    public void setUp() throws NotificationClientException, IOException {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(documentDownloadClient.getJsonObjectFromDocument(updateTribunalDecisionRule31Doc)).thenReturn(jsonDocument);
        when(documentDownloadClient.getJsonObjectFromDocument(updateTribunalDecisionRule32Doc)).thenReturn(jsonDocument);
        when(documentDownloadClient.getJsonObjectFromDocument(internalUpdateDecisionAndReasonlLetter)).thenReturn(jsonDocument);

        detentionEngagementTeamUpdateTribunalDecisionPersonalisation =
            new DetentionEngagementTeamUpdateTribunalDecisionPersonalisation(
                templateIdWithDecision,
                templateIdWithoutDecision,
                detEmailService,
                documentDownloadClient,
                adaPrefix,
                nonAdaPrefix,
                personalisationProvider
            );
    }

    @Test
    public void should_return_given_template_id_with_or_without_newDecisonAndReason_detained() {
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(detentionEngagementTeamUpdateTribunalDecisionPersonalisation.getTemplateId(asylumCase).contains(templateIdWithDecision));

        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
            .thenReturn(Optional.empty());
        assertTrue(detentionEngagementTeamUpdateTribunalDecisionPersonalisation.getTemplateId(asylumCase).contains(templateIdWithoutDecision));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_INTERNAL_DETAINED_UPDATE_TRIBUNAL_DECISION_DET",
            detentionEngagementTeamUpdateTribunalDecisionPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_det_email_address() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(detEmailService.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(detentionEngagementTeamEmail));

        assertTrue(
            detentionEngagementTeamUpdateTribunalDecisionPersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_no_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), detentionEngagementTeamUpdateTribunalDecisionPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_empty_set_email_address_from_asylum_case_other_detention_facility() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        assertEquals(Collections.emptySet(), detentionEngagementTeamUpdateTribunalDecisionPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> detentionEngagementTeamUpdateTribunalDecisionPersonalisation.getPersonalisationForLink((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_correct_personalisation_if_rule31() throws NotificationClientException, IOException {
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, String.class)).thenReturn(Optional.of("underRule31"));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(updateTribunalDecisionR31Bundle)));

        Map<String, Object> personalisation = detentionEngagementTeamUpdateTribunalDecisionPersonalisation.getPersonalisationForLink(asylumCase);

        assertEquals(nonAdaPrefix, personalisation.get("subjectPrefix"));
        assertEquals(jsonDocument, personalisation.get("documentLink"));
    }

    @Test
    void should_return_correct_personalisation_if_rule32() throws NotificationClientException, IOException {
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, String.class)).thenReturn(Optional.of("underRule32"));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(updateTribunalDecisionR32Bundle)));

        Map<String, Object> personalisation = detentionEngagementTeamUpdateTribunalDecisionPersonalisation.getPersonalisationForLink(asylumCase);

        assertEquals(nonAdaPrefix, personalisation.get("subjectPrefix"));
        assertEquals(jsonDocument, personalisation.get("documentLink"));
    }

    @Test
    void should_return_correct_personalisation_if_rule31_update_decision_and_reason() throws NotificationClientException, IOException {
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, String.class)).thenReturn(Optional.of("underRule31"));
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(updateTribunalDecisionR31Bundle)));
        when(asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENTS)).thenReturn(Optional.of(newArrayList(internalUpdateDecisionAndReasonLetterId)));

        Map<String, Object> personalisation = detentionEngagementTeamUpdateTribunalDecisionPersonalisation.getPersonalisationForLink(asylumCase);

        assertEquals(nonAdaPrefix, personalisation.get("subjectPrefix"));
        assertEquals(jsonDocument, personalisation.get("documentLink"));
        assertEquals(jsonDocument, personalisation.get("decisionsAndReasonsDocumentLink"));
    }
}