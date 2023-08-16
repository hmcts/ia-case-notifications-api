package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_REQUEST_HEARING_REQUIREMENTS_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;

@Slf4j
@Service
public class DetentionEngagementTeamRequestHearingRequirementPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String internalDetainedTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetEmailService detEmailService;
    private final PersonalisationProvider personalisationProvider;
    private String subjectPrefix;


    public DetentionEngagementTeamRequestHearingRequirementPersonalisation(
            @Value("${govnotify.template.requestHearingRequirements.detentionEngagementTeam.nonAda.email}") String internalDetainedTemplateId,
            DetEmailService detEmailService,
            DocumentDownloadClient documentDownloadClient,
            @Value("${govnotify.emailPrefix.nonAdaInPerson}") String subjectPrefix,
            PersonalisationProvider personalisationProvider
    ) {
        this.internalDetainedTemplateId = internalDetainedTemplateId;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.subjectPrefix = subjectPrefix;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId() {
        return internalDetainedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return detEmailService.getRecipientsList(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DETAINED_REQUEST_HEARING_REQUIREMENTS_DET";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", subjectPrefix)
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("documentLink", getInternalDetainedCaseListedLetterInJsonObject(asylumCase))
                .build();
    }

    private JSONObject getInternalDetainedCaseListedLetterInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_REQUEST_HEARING_REQUIREMENTS_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal request hearing requirements letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal request hearing requirements letter in compatible format");
        }
    }
}
