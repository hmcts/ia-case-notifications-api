package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_DETAINED_APPEAL_SUBMITTED_WITH_EXEMPTION_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionFacilityEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamAppealSubmittedWithExemptionPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String detentionEngagementTeamTemplateId;
    private final String nonAdaPrefix;
    private final DetentionEmailService detentionEmailService;
    private final DocumentDownloadClient documentDownloadClient;

    public DetentionEngagementTeamAppealSubmittedWithExemptionPersonalisation(
            @Value("${govnotify.template.detained-iaft-email-template}") String detentionEngagementTeamTemplateId,
            @Value("${govnotify.emailPrefix.nonAdaInPerson}") String nonAdaPrefix,
            DetentionEmailService detentionEmailService,
            DocumentDownloadClient documentDownloadClient
    ) {
        this.detentionEngagementTeamTemplateId = detentionEngagementTeamTemplateId;
        this.nonAdaPrefix = nonAdaPrefix;
        this.detentionEmailService = detentionEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_NON_ADA_APPEAL_SUBMITTED_WITH_EXEMPTION";
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
    public String getTemplateId() {
        return detentionEngagementTeamTemplateId;
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws IOException, NotificationClientException {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", nonAdaPrefix)
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("documentLink", getAppealCanProceedLetterJsonObject(asylumCase))
                .build();
    }

    private JSONObject getAppealCanProceedLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_DETAINED_APPEAL_SUBMITTED_WITH_EXEMPTION_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal 'Appeal can proceed' Letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal 'Appeal can proceed' Letter in compatible format");
        }
    }
}
