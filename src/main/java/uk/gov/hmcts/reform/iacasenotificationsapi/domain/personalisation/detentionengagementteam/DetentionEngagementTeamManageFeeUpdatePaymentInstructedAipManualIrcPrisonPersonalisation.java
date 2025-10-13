package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_DETAINED_MANAGE_FEE_UPDATE_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;

@Slf4j
@Service
public class DetentionEngagementTeamManageFeeUpdatePaymentInstructedAipManualIrcPrisonPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String templateId;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetentionEmailService detentionEmailService;
    private final PersonalisationProvider personalisationProvider;


    public DetentionEngagementTeamManageFeeUpdatePaymentInstructedAipManualIrcPrisonPersonalisation(
            @Value("${govnotify.template.det-email-template}") String templateId,
            DetentionEmailService detentionEmailService,
            DocumentDownloadClient documentDownloadClient,
            PersonalisationProvider personalisationProvider
    ) {
        this.templateId = templateId;
        this.detentionEmailService = detentionEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId() {
        return templateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (!isAppellantInDetention(asylumCase)) {
            return Collections.emptySet();
        }
        return Collections.singleton(detentionEmailService.getDetentionEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DETAINED_MANAGE_FEE_UPDATE_DET";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", "IAFT - SERVE IN PERSON")
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("documentLink", getInternalDetainedManageFeeUpdateLetterInJsonObject(asylumCase))
                .build();
    }

    private JSONObject getInternalDetainedManageFeeUpdateLetterInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_DETAINED_MANAGE_FEE_UPDATE_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal detained manage fee update letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal detained manage fee update letter in compatible format");
        }
    }
}
