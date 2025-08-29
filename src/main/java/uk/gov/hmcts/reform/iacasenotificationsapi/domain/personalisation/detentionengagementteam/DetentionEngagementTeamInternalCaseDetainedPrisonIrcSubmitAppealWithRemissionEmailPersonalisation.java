package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionFacilityEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_DETAINED_PRISON_IRC_APPEAL_SUBMISSION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;

@Slf4j
@Service
public class DetentionEngagementTeamInternalCaseDetainedPrisonIrcSubmitAppealWithRemissionEmailPersonalisation implements EmailWithLinkNotificationPersonalisation {
    private final String appealSubmittedNonAdaInTimeDetainedPrisonIrcTemplateId;
    private final String nonAdaPrefix;
    private final DetentionFacilityEmailService detentionFacilityEmailService;
    private final DocumentDownloadClient documentDownloadClient;

    public DetentionEngagementTeamInternalCaseDetainedPrisonIrcSubmitAppealWithRemissionEmailPersonalisation(
        @Value("${govnotify.template.appealSubmitted.adminOfficer.nonAdaInTimeDetainedPrisonIrc.email}")
        String appealSubmittedNonAdaInTimeDetainedPrisonIrcTemplateId,
        @Value("${govnotify.emailPrefix.nonAdaInPerson}") String nonAdaPrefix,
        DetentionFacilityEmailService detentionFacilityEmailService,
        DocumentDownloadClient documentDownloadClient
    ) {
        this.appealSubmittedNonAdaInTimeDetainedPrisonIrcTemplateId = appealSubmittedNonAdaInTimeDetainedPrisonIrcTemplateId;
        this.nonAdaPrefix = nonAdaPrefix;
        this.detentionFacilityEmailService = detentionFacilityEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_NON_ADA_APPEAL_SUBMITTED";
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(detentionFacilityEmailService.getDetentionEmailAddress(asylumCase));
    }

    @Override
    public String getTemplateId() {
        return appealSubmittedNonAdaInTimeDetainedPrisonIrcTemplateId;
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws IOException, NotificationClientException {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, Object>builder()
            .put("subjectPrefix", nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("documentLink", getAppealSubmittedLetterJsonObject(asylumCase))
            .build();
    }

    private JSONObject getAppealSubmittedLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_DETAINED_PRISON_IRC_APPEAL_SUBMISSION));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal Appeal submission Letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal Appeal submission Letter in compatible format");
        }
    }
}
