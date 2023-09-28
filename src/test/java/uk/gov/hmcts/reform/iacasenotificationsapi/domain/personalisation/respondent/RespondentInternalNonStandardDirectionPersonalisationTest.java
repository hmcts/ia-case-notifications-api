package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixesForInternalAppealByPost;

import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.TestUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HomeOfficeEmailFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class RespondentInternalNonStandardDirectionPersonalisationTest {

    @Mock PersonalisationProvider personalisationProvider;
    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock AsylumCase asylumCase;
    private final String nonAdaPrefix = "IAFT - SERVE BY POST";
    private final String adaPrefix = "ADA - SERVE BY POST";
    @Mock
    HomeOfficeEmailFinder homeOfficeEmailFinder;

    private Long caseId = 12345L;
    private String templateId = "templateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String respondentEmailAddress = "respondent@example.com";
    private final JSONObject jsonObject = new JSONObject("{\"title\": \"JsonDocument\"}");
    DocumentWithMetadata sendDirectionLetter = TestUtils.getDocumentWithMetadata(
            "id", "internal_appeal_submission", "some other desc", DocumentTag.INTERNAL_NON_STANDARD_DIRECTION_RESPONDENT_LETTER);
    IdValue<DocumentWithMetadata> document = new IdValue<>("1", sendDirectionLetter);

    @Mock
    DocumentDownloadClient documentDownloadClient;


    private RespondentInternalNonStandardDirectionPersonalisation respondentInternalNonStandardDirectionPersonalisation;

    @BeforeEach
    public void setup() throws NotificationClientException, IOException {
        respondentInternalNonStandardDirectionPersonalisation = new RespondentInternalNonStandardDirectionPersonalisation(
            templateId,
            documentDownloadClient,
            personalisationProvider,
            homeOfficeEmailFinder
        );
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS)).thenReturn(Optional.of(newArrayList(document)));
        when(documentDownloadClient.getJsonObjectFromDocument(any(DocumentWithMetadata.class))).thenReturn(jsonObject);
    }

    @Test
    public void should_return_give_reference_id() {
        assertEquals(caseId + "_INTERNAL_NON_STANDARD_DIRECTION_RESPONDENT",
            respondentInternalNonStandardDirectionPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, respondentInternalNonStandardDirectionPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_recipient_email_id() {
        when(caseDetails.getState()).thenReturn(State.SUBMIT_HEARING_REQUIREMENTS);
        when(homeOfficeEmailFinder.getRecipientsList(asylumCase)).thenReturn(Collections.singleton(respondentEmailAddress));
        assertEquals(Collections.singleton(respondentEmailAddress), respondentInternalNonStandardDirectionPersonalisation.getRecipientsList(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_given_personalisation(YesOrNo isAda) {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixesForInternalAppealByPost(respondentInternalNonStandardDirectionPersonalisation);
        when(personalisationProvider.getAppellantPersonalisation(asylumCase)).thenReturn(getPersonalisation());

        Map<String, Object> personalisation =
            respondentInternalNonStandardDirectionPersonalisation.getPersonalisationForLink(callback);
        //assert the personalisation map values
        assertThat(personalisation).isEqualToComparingOnlyGivenFields(getPersonalisation());
        assertEquals(jsonObject, personalisation.get("documentLink"));
        assertEquals(isAda == YesOrNo.YES ? adaPrefix : nonAdaPrefix, personalisation.get("subjectPrefix"));
    }

    @Test
    public void should_throw_exception_when_personalisation_when_callback_is_null() {

        assertThatThrownBy(() -> respondentInternalNonStandardDirectionPersonalisation.getPersonalisationForLink((AsylumCase) null))
            .hasMessage("asylumCase must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    private Map<String, String> getPersonalisation() {

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", "PA/12345/001")
            .put("homeOfficeReference", "A1234567")
            .put("appellantGivenNames", "Talha")
            .put("appellantFamilyName", "Awan")
            .build();
    }
}