package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getFtpaDecisionOutcomeType;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final CustomerServicesProvider customerServicesProvider;
    private final String detRespondentFtpaAppliationRefusedNotAdmittedTemplateId;
    private final String detRespondentFtpaAppliationGrantedPartiallyGrantedTemplateId;
    private final String adaPrefix;
    private final DetEmailService detEmailService;
    private final DocumentDownloadClient documentDownloadClient;

    public DetentionEngagementTeamRespondentFtpaApplicationDecidedPersonalisation(
        @Value("${govnotify.template.applicationRefusedOrNotAdmitted.otherParty.detentionEngagementTeam.email}")
        String detRespondentFtpaAppliationRefusedNotAdmittedTemplateId,
        @Value("${govnotify.template.applicationGrantedOrPartiallyGranted.otherParty.detentionEngagementTeam.email}")
        String detRespondentFtpaAppliationGrantedPartiallyGrantedTemplateId,
        @Value("${govnotify.emailPrefix.ada}") String adaPrefix,
        CustomerServicesProvider customerServicesProvider,
        DetEmailService detEmailService,
        DocumentDownloadClient documentDownloadClient
    ) {
        this.detRespondentFtpaAppliationRefusedNotAdmittedTemplateId
            = detRespondentFtpaAppliationRefusedNotAdmittedTemplateId;
        this.detRespondentFtpaAppliationGrantedPartiallyGrantedTemplateId
            = detRespondentFtpaAppliationGrantedPartiallyGrantedTemplateId;
        this.adaPrefix = adaPrefix;
        this.customerServicesProvider = customerServicesProvider;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        FtpaDecisionOutcomeType decision = getFtpaDecisionOutcomeType(asylumCase)
            .orElseThrow(() -> new IllegalStateException("ftpaRespondentDecisionOutcomeType is not present"));

        switch (decision) {
            case FTPA_REFUSED:
            case FTPA_NOT_ADMITTED:
                return detRespondentFtpaAppliationRefusedNotAdmittedTemplateId;
            case FTPA_GRANTED:
            case FTPA_PARTIALLY_GRANTED:
                return detRespondentFtpaAppliationGrantedPartiallyGrantedTemplateId;
            default:
                throw new IllegalStateException("Unsupported ftpa application decision type");
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(detEmailService.getAdaDetEmailAddress());
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_FTPA_APPLICATION_DECISION_DETENTION_ENGAGEMENT_TEAM";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        String ariaListingReferenceIfPresent = asylumCase.read(ARIA_LISTING_REFERENCE, String.class)
            .map(ariaListingReference -> "Listing reference: " + ariaListingReference)
            .orElse("");

        return ImmutableMap
            .<String, Object>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", adaPrefix)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReferenceIfPresent", ariaListingReferenceIfPresent)
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("applicationDecision", getApplicationDecision(asylumCase))
            .put("ftpaDecisionAndReasonsDocumentDownloadLink", getDocument(asylumCase, FTPA_RESPONDENT_DECISION_DOCUMENT))
            .put("homeOfficeFtpaApplicationDownloadLink", getDocument(asylumCase, FTPA_RESPONDENT_GROUNDS_DOCUMENTS))
            .build();
    }

    private JSONObject getDocument(AsylumCase asylumCase, AsylumCaseDefinition caseDefinition) {

        Optional<List<IdValue<DocumentWithDescription>>> optionalRespondentDocuments = asylumCase
            .read(caseDefinition);
        DocumentWithMetadata document = optionalRespondentDocuments
            .orElse(Collections.emptyList())
            .stream()
            .map(IdValue::getValue)
            .filter(d -> d.getDocument().isPresent())
            .map(d -> new DocumentWithMetadata(
                d.getDocument().get(), d.getDescription().orElse(""), null, null
            ))
            .findFirst().orElseThrow(() -> new IllegalStateException(String.format("%s is not present", caseDefinition.value())));

        try {
            return documentDownloadClient.getJsonObjectFromDocument(document);
        } catch (IOException | NotificationClientException e) {
            String errorMsg = String.format("Failed to get %s in compatible format", caseDefinition.value());
            log.error(errorMsg, e);
            throw new IllegalStateException(errorMsg);
        }
    }

    private String getApplicationDecision(AsylumCase asylumCase) {
        FtpaDecisionOutcomeType decision = getFtpaDecisionOutcomeType(asylumCase)
            .orElseThrow(() -> new IllegalStateException("ftpaRespondentDecisionOutcomeType is not present"));

        switch (decision) {
            case FTPA_REFUSED:
                return "been refused";
            case FTPA_NOT_ADMITTED:
                return "not been admitted";
            case FTPA_GRANTED:
                return "granted";
            case FTPA_PARTIALLY_GRANTED:
                return "partially granted";
            default:
                throw new IllegalStateException("Unsupported ftpa application decision type");
        }
    }
}

