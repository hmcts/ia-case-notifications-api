package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAgeAssessmentAppeal;


@Service
@Slf4j
public class LegalRepresentativeAppealSubmittedWithLinkPersonalisation implements LegalRepresentativeEmailNotificationWithLinkPersonalisation {

    private final String appealSubmittedLegalRepresentativeTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final DocumentDownloadClient documentDownloadClient;
    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;

    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeAppealSubmittedWithLinkPersonalisation(
            @NotNull(message = "appealSubmittedLegalRepresentativePaidTemplateId cannot be null")
            @Value("${govnotify.template.appealSubmitted.legalRep.paid.emailWithLink}") String appealSubmittedLegalRepresentativeTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider,
            DocumentDownloadClient documentDownloadClient
    ) {
        this.appealSubmittedLegalRepresentativeTemplateId = appealSubmittedLegalRepresentativeTemplateId;
//        this.appealSubmittedLegalRepresentativeAdaOrAaaTemplateId = appealSubmittedLegalRepresentativeAdaOrAaaTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appealSubmittedLegalRepresentativeTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_SUBMITTED_LEGAL_REP_WITH_LINK";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws IOException, NotificationClientException {
        requireNonNull(asylumCase, "asylumCase must not be null");
        DocumentWithMetadata document = getAppealSubmissionDocument(asylumCase);
            return ImmutableMap
                    .<String, Object>builder()
                    .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                    .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                    .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                    .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                    .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                    .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                    .put("linkToOnlineService", iaExUiFrontendUrl)
                    .put("link_to_file", documentDownloadClient.getJsonObjectFromDocument(document))
                    .build();
    }

    private DocumentWithMetadata getAppealSubmissionDocument(AsylumCase asylumCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> maybeLegalRepresentativeDocuments = asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS);
        List<DocumentWithMetadata> documents = maybeLegalRepresentativeDocuments
                .orElse(Collections.emptyList())
                .stream()
                .map(IdValue::getValue)
                .filter(document -> document.getTag() == DocumentTag.APPEAL_SUBMISSION)
                .collect(Collectors.toList());

        DocumentWithMetadata document = documents.get(0);
        return document;
    }

}


