package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamUploadAppealResponsePersonalisation implements EmailWithLinkNotificationPersonalisation {

    private static final String HOME_OFFICE_RESPONSE = "Home Office Response";
    private static final String WITHDRAWAL_LETTER = "Withdrawal Letter";
    private static final String MAINTAIN = "maintain";
    private static final String WITHDRAW = "withdraw";

    private final CustomerServicesProvider customerServicesProvider;
    private final String detentionEngagementTeamUploadAppealResponseTemplateId;
    private final String adaPrefix;
    private final DetEmailService detEmailService;
    private final DocumentDownloadClient documentDownloadClient;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;

    public DetentionEngagementTeamUploadAppealResponsePersonalisation(
        @Value("${govnotify.template.homeOfficeResponseUploaded.detentionEngagementTeam.email}") String detentionEngagementTeamUploadAppealResponseTemplateId,
        @Value("${govnotify.emailPrefix.ada}") String adaPrefix,
        CustomerServicesProvider customerServicesProvider,
        DetEmailService detEmailService,
        DocumentDownloadClient documentDownloadClient,
        DateTimeExtractor dateTimeExtractor,
        HearingDetailsFinder hearingDetailsFinder
    ) {
        this.detentionEngagementTeamUploadAppealResponseTemplateId = detentionEngagementTeamUploadAppealResponseTemplateId;
        this.adaPrefix = adaPrefix;
        this.customerServicesProvider = customerServicesProvider;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId() {
        return detentionEngagementTeamUploadAppealResponseTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        Optional<String> detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class);
        if (detentionFacility.isEmpty() || !detentionFacility.get().equals("immigrationRemovalCentre")) {
            return Collections.emptySet();
        }

        return Collections.singleton(detEmailService.getDetEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UPLOADED_HO_RESPONSE_DETENTION_ENGAGEMENT_TEAM";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        String ariaListingReferenceIfPresent = asylumCase.read(ARIA_LISTING_REFERENCE, String.class)
            .map(ariaListingReference -> "Listing reference: " + ariaListingReference)
            .orElse("");

        String appealReviewOutcome = getAppealReviewOutcome(asylumCase);
        String documentDownloadTitle = appealReviewOutcome.equals(WITHDRAW) ? WITHDRAWAL_LETTER : HOME_OFFICE_RESPONSE;

        return ImmutableMap
            .<String, Object>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", adaPrefix)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReferenceIfPresent", ariaListingReferenceIfPresent)
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("appealReviewOutcome", appealReviewOutcome)
            .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase)))
            .put("documentDownloadTitle", documentDownloadTitle)
            .put("linkToDownloadDocument", getAppealResponseDocument(asylumCase))
            .build();
    }

    private String getAppealReviewOutcome(AsylumCase asylumCase) {
        AppealReviewOutcome appealReviewOutcome = asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class)
            .orElseThrow(() -> new IllegalStateException("Appeal review outcome is not present"));

        return appealReviewOutcome == AppealReviewOutcome.DECISION_MAINTAINED ? MAINTAIN : WITHDRAW;
    }

    private JSONObject getAppealResponseDocument(AsylumCase asylumCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> optionalRespondentDocuments = asylumCase.read(RESPONDENT_DOCUMENTS);
        DocumentWithMetadata document = optionalRespondentDocuments
            .orElse(Collections.emptyList())
            .stream()
            .map(IdValue::getValue)
            .filter(d -> d.getTag() == DocumentTag.APPEAL_RESPONSE)
            .findFirst().orElseThrow(() -> new IllegalStateException("Home Office response document not available"));

        try {
            return documentDownloadClient.getJsonObjectFromDocument(document);
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Home Office response document in compatible format", e);
            throw new IllegalStateException("Failed to get Home Office response document in compatible format");
        }
    }
}

