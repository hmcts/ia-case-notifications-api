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
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.CMR_HEARING_CANCELLED_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;

@Slf4j
@Service
public class DetentionEngagementTeamCmrCancelledPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String cmrCancelledDetainedTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetentionEmailService detentionEmailService;
    private final PersonalisationProvider personalisationProvider;
    private final String subjectPrefix;

    public DetentionEngagementTeamCmrCancelledPersonalisation(
            @Value("${govnotify.template.cmrCancelled.detentionEngagementTeam.email}") String cmrCancelledDetainedTemplateId,
            DetentionEmailService detentionEmailService,
            DocumentDownloadClient documentDownloadClient,
            @Value("${govnotify.emailPrefix.nonAdaInPerson}") String subjectPrefix,
            PersonalisationProvider personalisationProvider
    ) {
        this.cmrCancelledDetainedTemplateId = cmrCancelledDetainedTemplateId;
        this.detentionEmailService = detentionEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.subjectPrefix = subjectPrefix;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId() {
        return cmrCancelledDetainedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (!isAppellantInDetention(asylumCase)) {
            return Collections.emptySet();
        }

        Optional<String> detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class);
        if (detentionFacility.isEmpty() || detentionFacility.get().equals("other")) {
            return Collections.emptySet();
        }

        return Collections.singleton(detentionEmailService.getDetentionEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_DETAINED_CMR_CANCELLED_DET";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", subjectPrefix)
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("documentLink", getDetainedCmrCancelledLetterInJsonObject(asylumCase))
                .build();
    }

    private JSONObject getDetainedCmrCancelledLetterInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, CMR_HEARING_CANCELLED_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal detained CMR cancelled letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal detained CMR cancelled letter in compatible format");
        }
    }
}

