package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.MAINTAIN_CASE_LINK_APPEAL_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
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

@Slf4j
@Service
public class DetentionEngagementTeamMaintainCaseLinkAppealPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String internalDetMaintainCaseLinkAppealTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetEmailService detEmailService;
    private final PersonalisationProvider personalisationProvider;
    private String adaPrefix;
    private String nonAdaPrefix;

    public DetentionEngagementTeamMaintainCaseLinkAppealPersonalisation(
            @Value("${govnotify.template.maintainCaseLinks.detentionEngagementTeam.linkAppeal.email}") String internalDetMaintainCaseLinkAppealTemplateId,
            DetEmailService detEmailService,
            DocumentDownloadClient documentDownloadClient,
            PersonalisationProvider personalisationProvider, @Value("${govnotify.emailPrefix.adaByPost}") String adaPrefix,
            @Value("${govnotify.emailPrefix.nonAdaByPost}") String nonAdaPrefix
    ) {
        this.internalDetMaintainCaseLinkAppealTemplateId = internalDetMaintainCaseLinkAppealTemplateId;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.personalisationProvider = personalisationProvider;
        this.adaPrefix = adaPrefix;
        this.nonAdaPrefix = nonAdaPrefix;
    }

    @Override
    public String getTemplateId() {
        return internalDetMaintainCaseLinkAppealTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return detEmailService.getRecipientsList(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DET_MAINTAIN_CASE_LINK_APPEAL_EMAIL";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("documentLink", getInternalMaintainCaseLinkAppealDocumentInJsonObject(asylumCase))
                .build();
    }

    private JSONObject getInternalMaintainCaseLinkAppealDocumentInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, MAINTAIN_CASE_LINK_APPEAL_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal Detained Maintain case link appeal document in compatible format", e);
            throw new IllegalStateException("Failed to get Internal Detained Maintain case link appeal document in compatible format");
        }
    }
}
