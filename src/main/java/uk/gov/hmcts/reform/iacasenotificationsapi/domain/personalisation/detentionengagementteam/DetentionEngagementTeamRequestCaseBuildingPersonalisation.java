package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;


@Service
@Slf4j
public class DetentionEngagementTeamRequestCaseBuildingPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String internalAdaRequestCaseBuildingTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private String adaPrefix;
    private final DetEmailService detEmailService;

    public DetentionEngagementTeamRequestCaseBuildingPersonalisation(
            @Value("${govnotify.template.requestCaseBuilding.detentionEngagementTeam.email}") String templateId,
            @Value("${govnotify.emailPrefix.ada}") String adaPrefix,
            DetEmailService detEmailService,
            DocumentDownloadClient documentDownloadClient
    ) {
        this.internalAdaRequestCaseBuildingTemplateId = templateId;
        this.adaPrefix = adaPrefix;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getTemplateId() {
        return internalAdaRequestCaseBuildingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        Optional<String> detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class);
        if (detentionFacility.isEmpty() || detentionFacility.get().equals("other")) {
            return Collections.emptySet();
        }

        return Collections.singleton(detEmailService.getDetEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_ADA_REQUEST_CASE_BUILDING_EMAIL";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", adaPrefix)
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("documentLink", getRequestCaseBuildingDocumentInJsonObject(asylumCase))
                .build();
    }

    private JSONObject getRequestCaseBuildingDocumentInJsonObject(AsylumCase asylumCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> maybeDocuments = asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS);
        List<DocumentWithMetadata> documents = maybeDocuments
                .orElse(Collections.emptyList())
                .stream()
                .map(IdValue::getValue)
                .filter(document -> document.getTag() == DocumentTag.REQUEST_CASE_BUILDING)
                .collect(Collectors.toList());

        if (documents.size() == 0) {
            throw new RequiredFieldMissingException("Request case building document is not present");
        }
        DocumentWithMetadata document = documents.get(0);
        try {
            return documentDownloadClient.getJsonObjectFromDocument(document);
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Request case building document in compatible format", e);
            throw new IllegalStateException("Failed to get Request case building document in compatible format");
        }
    }
}
