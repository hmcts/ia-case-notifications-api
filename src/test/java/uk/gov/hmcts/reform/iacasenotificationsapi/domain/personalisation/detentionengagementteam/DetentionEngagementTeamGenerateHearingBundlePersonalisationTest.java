package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils.compareStringsAndJsonObjects;
import static uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils.getDocumentWithMetadata;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;

import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DetentionEngagementTeamGenerateHearingBundlePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    DetEmailService detEmailService;
    @Mock
    JSONObject jsonDocument;
    @Mock
    DocumentDownloadClient documentDownloadClient;

    private final String internalAdaGenerateHearingBundleTemplateId = "someTemplateId";
    private final String generateHearingBundlePersonalisationReferenceId = "_INTERNAL_ADA_GENERATE_HEARING_BUNDLE_EMAIL";
    private final String subjectPrefix = "ADA";
    private final String ircName = "Tinsley House";
    private final String detEmailAddress = "some@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    DocumentWithMetadata hearingBundle = getDocumentWithMetadata(
            "1", "ADA appellant hearing bundle", "some other desc", DocumentTag.HEARING_BUNDLE);
    IdValue<DocumentWithMetadata> hearingBundleDocId = new IdValue<>("1", hearingBundle);
    private DetentionEngagementTeamGenerateHearingBundlePersonalisation detentionEngagementTeamGenerateHearingBundlePersonalisation;

    @BeforeEach
    public void setUp() throws NotificationClientException, IOException {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(IRC_NAME, String.class)).thenReturn(Optional.of(ircName));
        when(detEmailService.getDetEmailAddress(asylumCase)).thenReturn(detEmailAddress);

        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(hearingBundleDocId)));
        when(documentDownloadClient.getJsonObjectFromDocument(hearingBundle)).thenReturn(jsonDocument);

        detentionEngagementTeamGenerateHearingBundlePersonalisation =
                new DetentionEngagementTeamGenerateHearingBundlePersonalisation(
                        internalAdaGenerateHearingBundleTemplateId,
                        subjectPrefix,
                        detEmailService,
                        documentDownloadClient
                );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(
                internalAdaGenerateHearingBundleTemplateId,
                detentionEngagementTeamGenerateHearingBundlePersonalisation.getTemplateId()
        );
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + generateHearingBundlePersonalisationReferenceId,
                detentionEngagementTeamGenerateHearingBundlePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertEquals(Collections.singleton(detEmailAddress),
                detentionEngagementTeamGenerateHearingBundlePersonalisation.getRecipientsList(asylumCase)
        );
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
                () -> detentionEngagementTeamGenerateHearingBundlePersonalisation.getPersonalisationForLink((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_request_case_building_document_is_missing() {
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> detentionEngagementTeamGenerateHearingBundlePersonalisation.getPersonalisationForLink(asylumCase))
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessage("Hearing bundle is not present");
    }

    @Test
    public void should_return_personalisation_when_all_information_given_refused() {

        final Map<String, Object> expectedPersonalisation =
                ImmutableMap
                        .<String, Object>builder()
                        .put("subjectPrefix", subjectPrefix)
                        .put("appealReferenceNumber", appealReferenceNumber)
                        .put("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
                        .put("appellantGivenNames", appellantGivenNames)
                        .put("appellantFamilyName", appellantFamilyName)
                        .put("documentLink", jsonDocument)
                        .build();

        Map<String, Object> actualPersonalisation =
                detentionEngagementTeamGenerateHearingBundlePersonalisation.getPersonalisationForLink(asylumCase);

        assertTrue(compareStringsAndJsonObjects(expectedPersonalisation, actualPersonalisation));
    }
}
