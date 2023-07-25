package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_APPEAL_SUBMISSION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class AdminOfficerAppealSubmittedInTimePersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String appealSubmittedNonAdaInTimeDetainedOnlyTemplateId;
    private final String nonAdaPrefix;
    private final DetEmailService detEmailService;
    private final DocumentDownloadClient documentDownloadClient;

    public AdminOfficerAppealSubmittedInTimePersonalisation(
            @Value("${govnotify.template.appealSubmitted.adminOfficer.nonAdaInTimeDetainedOnly.email}")
            String appealSubmittedNonAdaInTimeDetainedOnlyTemplateId,
            @Value("${govnotify.emailPrefix.nonAdaInPerson}") String nonAdaPrefix,
            DetEmailService detEmailService,
            DocumentDownloadClient documentDownloadClient) {
        this.appealSubmittedNonAdaInTimeDetainedOnlyTemplateId = appealSubmittedNonAdaInTimeDetainedOnlyTemplateId;
        this.nonAdaPrefix = nonAdaPrefix;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_NON_ADA_APPEAL_SUBMITTED";
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
        return appealSubmittedNonAdaInTimeDetainedOnlyTemplateId;
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
                .put("documentLink", getAppealDecidedLetterJsonObject(asylumCase))
                .build();
    }

    private JSONObject getAppealDecidedLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_APPEAL_SUBMISSION));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal Appeal submission Letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal Appeal submission Letter in compatible format");
        }
    }
}
