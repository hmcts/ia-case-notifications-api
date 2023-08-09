package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_END_APPEAL_AUTOMATICALLY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer.AdminOfficerPersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamEndAppealAutomaticallyPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String endAppealAutomaticallyDueToNonPaymentTemplateId;
    private final String nonAdaPrefix;
    private final DetEmailService detEmailService;
    private final DocumentDownloadClient documentDownloadClient;
    private final AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    public DetentionEngagementTeamEndAppealAutomaticallyPersonalisation(
            @Value("${govnotify.template.endAppealAutomatically.detentionEngagementTeam.nonAda.email}")
            String endAppealAutomaticallyDueToNonPaymentTemplateId,
            @Value("${govnotify.emailPrefix.nonAdaInPerson}") String nonAdaPrefix,
            DetEmailService detEmailService,
            DocumentDownloadClient documentDownloadClient,
            AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider
    ) {
        this.endAppealAutomaticallyDueToNonPaymentTemplateId = endAppealAutomaticallyDueToNonPaymentTemplateId;
        this.nonAdaPrefix = nonAdaPrefix;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.adminOfficerPersonalisationProvider = adminOfficerPersonalisationProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_NON_ADA_END_APPEAL_AUTOMATICALLY";
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
    public String getTemplateId() {
        return endAppealAutomaticallyDueToNonPaymentTemplateId;
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws IOException, NotificationClientException {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", nonAdaPrefix)
                .putAll(adminOfficerPersonalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("documentLink", getAppealDecidedLetterJsonObject(asylumCase))
                .build();
    }

    private JSONObject getAppealDecidedLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_END_APPEAL_AUTOMATICALLY));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal end appeal letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal automatically end appeal Letter in compatible format");
        }
    }
}
