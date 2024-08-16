package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isRule31ReasonUpdatingDecision;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamUpdateTribunalDecisionPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String internalDetainedUpdateTribunalDecisionTemplateId;
    private final String internalDetainedUpdateTribunalDecisionWithoutDecisionAndReasonsTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetEmailService detEmailService;
    private final PersonalisationProvider personalisationProvider;
    private String adaPrefix;
    private String nonAdaPrefix;

    public DetentionEngagementTeamUpdateTribunalDecisionPersonalisation(
        @Value("${govnotify.template.updateTribunalDecision.detentionEngagementTeam.decisionAndReasonsUpdated.email}") String internalDetainedUpdateTribunalDecisionTemplateId,
        @Value("${govnotify.template.updateTribunalDecision.detentionEngagementTeam.decisionAndReasonsNotUpdated.email}") String internalDetainedUpdateTribunalDecisionWithoutDecisionAndReasonsTemplateId,
        DetEmailService detEmailService,
        DocumentDownloadClient documentDownloadClient,
        @Value("${govnotify.emailPrefix.adaInPerson}") String adaPrefix,
        @Value("${govnotify.emailPrefix.nonAdaInPerson}") String nonAdaPrefix,
        PersonalisationProvider personalisationProvider
    ) {
        this.internalDetainedUpdateTribunalDecisionTemplateId = internalDetainedUpdateTribunalDecisionTemplateId;
        this.internalDetainedUpdateTribunalDecisionWithoutDecisionAndReasonsTemplateId = internalDetainedUpdateTribunalDecisionWithoutDecisionAndReasonsTemplateId;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.adaPrefix = adaPrefix;
        this.nonAdaPrefix = nonAdaPrefix;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        boolean newDecisionAndReasonsUploadCheck = asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false);

        if (newDecisionAndReasonsUploadCheck) {
            return internalDetainedUpdateTribunalDecisionTemplateId;
        } else {
            return internalDetainedUpdateTribunalDecisionWithoutDecisionAndReasonsTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (!isAppellantInDetention(asylumCase)) {
            return Collections.emptySet();
        }


        return detEmailService.getRecipientsList(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DETAINED_UPDATE_TRIBUNAL_DECISION_DET";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws IOException, NotificationClientException {
        requireNonNull(asylumCase, "asylumCase must not be null");

        Optional<Boolean> updateDecisionAndReasonsCheck = asylumCase
            .read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class)
            .map(flag -> flag.equals(YesOrNo.YES));

        ImmutableMap.Builder<String, Object> immutableMap = ImmutableMap.<String, Object>builder()

            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
            .put("documentLink", getInternalDetainedRule31Or32LetterInJsonObject(asylumCase));

        if (updateDecisionAndReasonsCheck.orElse(false)) {
            immutableMap.put("decisionsAndReasonsDocumentLink", getInternalDetainedUpdateDecisionAndReasonsLetterInJsonObject(asylumCase));
        }
        return immutableMap.build();
    }

    private JSONObject getInternalDetainedRule31Or32LetterInJsonObject(AsylumCase asylumCase) {
        try {
            if (isRule31ReasonUpdatingDecision(asylumCase)) {
                return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_DETAINED_UPDATE_TRIBUNAL_DECISION_R31_LETTER));
            } else if (isRule32ReasonUpdatingDecision(asylumCase)) {
                return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_DETAINED_UPDATE_TRIBUNAL_DECISION_R32_LETTER));
            }
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal detained update tribunal decision letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal detained update tribunal decision letter in compatible format");
        }
        return null;
    }

    private JSONObject getInternalDetainedUpdateDecisionAndReasonsLetterInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getdecisionAndReasonsLetterForNotification(asylumCase, UPDATED_FINAL_DECISION_AND_REASONS_PDF));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal detained updated decision and reasons doc in compatible format", e);
            throw new IllegalStateException("Failed to get Internal updated decision and reasons letter in compatible format");
        }
    }
}