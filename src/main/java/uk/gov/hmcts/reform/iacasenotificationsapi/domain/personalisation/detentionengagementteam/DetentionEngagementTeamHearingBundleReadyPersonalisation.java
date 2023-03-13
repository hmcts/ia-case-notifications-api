package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Service
public class DetentionEngagementTeamHearingBundleReadyPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String detHearingBundleReadyTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    private final DetEmailService detEmailService;

    public DetentionEngagementTeamHearingBundleReadyPersonalisation(
        @Value("${govnotify.template.hearingBundleReady.detentionEngagementTeam.email}") String detHearingBundleReadyTemplateId,
        DetEmailService detEmailService,
        DocumentDownloadClient documentDownloadClient
    ) {
        this.detHearingBundleReadyTemplateId = detHearingBundleReadyTemplateId;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getTemplateId() {
        return detHearingBundleReadyTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return Collections.singleton(detEmailService.getAdaDetEmailAddress());
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_HEARING_BUNDLE_IS_READY_DET_ADA_EMAIL";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws NotificationClientException, IOException {
        requireNonNull(asylumCase, "asylumCase must not be null");

        DocumentWithMetadata document = getHearingBundleDocument(asylumCase);

        return ImmutableMap
            .<String, Object>builder()
            .put("subjectPrefix", adaPrefix)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("link_to_hearing_bundle", documentDownloadClient.getJsonObjectFromDocument(document))
            .build();
    }

    private DocumentWithMetadata getHearingBundleDocument(AsylumCase asylumCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> maybeLegalRepresentativeDocuments = asylumCase.read(HEARING_DOCUMENTS);
        List<DocumentWithMetadata> documents = maybeLegalRepresentativeDocuments
                .orElse(Collections.emptyList())
                .stream()
                .map(IdValue::getValue)
                .filter(document -> document.getTag() == DocumentTag.HEARING_BUNDLE)
                .collect(Collectors.toList());

        if (documents.size() == 0) {
            throw new RequiredFieldMissingException("Hearing Bundle is not available");
        }
        DocumentWithMetadata document = documents.get(0);
        return document;
    }
}
