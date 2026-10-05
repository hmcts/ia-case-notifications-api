package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;


@Service
public class AppellantUploadAdditionalEvidencePersonalisationSms implements SmsNotificationPersonalisation {

    private final String uploadAdditionalEvidenceSmsBeforeListingNotificationTemplateId;
    private final String uploadAdditionalEvidenceSmsAfterListingNotificationTemplateId;
    private final String uploadAdditionalEvidenceSmsAfterListing24WeeksNotificationTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;

    public AppellantUploadAdditionalEvidencePersonalisationSms(
        @Value("${govnotify.template.uploadedAdditionalEvidenceBeforeListing.appellant.sms}") String uploadAdditionalEvidenceSmsBeforeListingNotificationTemplateId,
        @Value("${govnotify.template.uploadedAdditionalEvidenceAfterListing.appellant.sms}") String uploadAdditionalEvidenceSmsAfterListingNotificationTemplateId,
        @Value("${govnotify.template.uploadedAdditionalEvidenceAfterListing.appellant.sms24Weeks}") String uploadAdditionalEvidenceSmsAfterListing24WeeksNotificationTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        RecipientsFinder recipientsFinder
    ) {

        this.uploadAdditionalEvidenceSmsBeforeListingNotificationTemplateId = uploadAdditionalEvidenceSmsBeforeListingNotificationTemplateId;
        this.uploadAdditionalEvidenceSmsAfterListingNotificationTemplateId = uploadAdditionalEvidenceSmsAfterListingNotificationTemplateId;
        this.uploadAdditionalEvidenceSmsAfterListing24WeeksNotificationTemplateId = uploadAdditionalEvidenceSmsAfterListing24WeeksNotificationTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (AsylumCaseUtils.hasStf24WeeksStatus(asylumCase)) {
            return uploadAdditionalEvidenceSmsAfterListing24WeeksNotificationTemplateId;
        }
        return isAppealListed(asylumCase) ? uploadAdditionalEvidenceSmsAfterListingNotificationTemplateId
            : uploadAdditionalEvidenceSmsBeforeListingNotificationTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UPLOADED_ADDITIONAL_EVIDENCE_AIP_APPELLANT_SMS";
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("hyperlink to service", iaAipFrontendUrl)
            .build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}
