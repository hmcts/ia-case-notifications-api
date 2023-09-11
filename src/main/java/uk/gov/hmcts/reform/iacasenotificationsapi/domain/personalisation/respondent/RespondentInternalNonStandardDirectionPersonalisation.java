package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_NON_STANDARD_DIRECTION_RESPONDENT_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HomeOfficeEmailFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class RespondentInternalNonStandardDirectionPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String sendNonStandardDirectionTemplateId;
    @Value("${govnotify.emailPrefix.adaByPost}")
    private String adaPrefix;

    @Value("${govnotify.emailPrefix.nonAdaByPost}")
    private String nonAdaPrefix;
    private final DocumentDownloadClient documentDownloadClient;
    private final PersonalisationProvider personalisationProvider;

    private final HomeOfficeEmailFinder homeOfficeEmailFinder;

    public RespondentInternalNonStandardDirectionPersonalisation(
            @Value("${govnotify.template.nonStandardDirectionInternal.respondent.email}")
            String sendNonStandardDirectionTemplateId,
            DocumentDownloadClient documentDownloadClient,
            PersonalisationProvider personalisationProvider,
            HomeOfficeEmailFinder homeOfficeEmailFinder) {
        this.sendNonStandardDirectionTemplateId = sendNonStandardDirectionTemplateId;
        this.documentDownloadClient = documentDownloadClient;
        this.personalisationProvider = personalisationProvider;
        this.homeOfficeEmailFinder = homeOfficeEmailFinder;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_NON_STANDARD_DIRECTION_RESPONDENT";
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return homeOfficeEmailFinder.getRecipientsList(asylumCase);
    }

    @Override
    public String getTemplateId() {
        return sendNonStandardDirectionTemplateId;
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws IOException, NotificationClientException {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("documentLink", getAppealDecidedLetterJsonObject(asylumCase))
                .build();
    }

    private JSONObject getAppealDecidedLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_NON_STANDARD_DIRECTION_RESPONDENT_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal end appeal letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal automatically end appeal Letter in compatible format");
        }
    }
}
