package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;


import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.*;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamAdaSuitabilityReviewPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String documentDownloadTitle = "Accelerated Detained Appeal Suitability Decision document";
    private final String detentionEngagementTeamAdaSuitabilityReviewTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final String adaPrefix;
    private final DetEmailService detEmailService;
    private final DocumentDownloadClient documentDownloadClient;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;



    public DetentionEngagementTeamAdaSuitabilityReviewPersonalisation(
            @NotNull(message = "adaSuitabilityUnsuitableLegalRepresentativeTemplateId cannot be null")
            @Value("${govnotify.template.adaSuitabilityReview.detentionEngagementTeam.email}") String detentionEngagementTeamAdaSuitabilityReviewTemplateId,
            @Value("${govnotify.emailPrefix.ada}") String adaPrefix,
            CustomerServicesProvider customerServicesProvider,
            DetEmailService detEmailService,
            DocumentDownloadClient documentDownloadClient,
            DateTimeExtractor dateTimeExtractor,
            HearingDetailsFinder hearingDetailsFinder
    ) {
        this.detentionEngagementTeamAdaSuitabilityReviewTemplateId = detentionEngagementTeamAdaSuitabilityReviewTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.adaPrefix = adaPrefix;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return detentionEngagementTeamAdaSuitabilityReviewTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(detEmailService.getAdaDetEmailAddress());
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_ADA_SUITABILITY_DETERMINED_DET";
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
                .put("adaSuitability", getAdaSuitabilityDecision(asylumCase).toString())
                .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase)))

                .put("hearingRequirementVulnerabilities", asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class)
                        .orElse("No special adjustments are being made to accommodate vulnerabilities"))
                .put("hearingRequirementMultimedia", asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class)
                        .orElse("No multimedia equipment is being provided"))
                .put("hearingRequirementSingleSexCourt", asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class)
                        .orElse("The court will not be single sex"))
                .put("hearingRequirementInCameraCourt", asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class)
                        .orElse("The hearing will be held in public court"))
                .put("hearingRequirementOther", asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class)
                        .orElse("No other adjustments are being made"))
                .put("remoteVideoCallTribunalResponse", asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class)
                        .orElse("No adjustments have been made for a remote hearing"))
                .put("pastExperienceTribunalResponse", asylumCase.read(PAST_EXPERIENCES_TRIBUNAL_RESPONSE, String.class)
                        .orElse("No adjustments for previous experiences are being made"))

                .put("documentDownloadTitle", documentDownloadTitle)
                .put("linkToDownloadDocument", getAdaSuitabilityDocument(asylumCase))
                .build();

    }

    private AdaSuitabilityReviewDecision getAdaSuitabilityDecision(AsylumCase asylumCase) {
        return asylumCase.read(SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)
                .orElseThrow(() -> new RequiredFieldMissingException("ADA suitability decision missing"));
    }

    private JSONObject getAdaSuitabilityDocument(AsylumCase asylumCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> optionalRespondentDocuments = asylumCase.read(TRIBUNAL_DOCUMENTS);
        DocumentWithMetadata document = optionalRespondentDocuments
                .orElse(Collections.emptyList())
                .stream()
                .map(IdValue::getValue)
                .filter(d -> d.getTag() == DocumentTag.ADA_SUITABILITY)
                .findFirst().orElseThrow(() -> new IllegalStateException("ADA suitability document not available"));

        try {
            return documentDownloadClient.getJsonObjectFromDocument(document);
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get ADA suitability document in compatible format", e);
            throw new IllegalStateException("Failed to get ADA suitability document in compatible format");
        }
    }
}