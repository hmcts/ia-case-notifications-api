package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class CaseOfficerUploadAdditionalEvidencePersonalisation implements EmailNotificationPersonalisation {

    private final String caseOfficerUploadedAdditionalEvidenceBeforeListingTemplateId;
    private final String caseOfficerUploadedAdditionalEvidenceAfterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final EmailAddressFinder emailAddressFinder;
    private final FeatureToggler featureToggler;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public CaseOfficerUploadAdditionalEvidencePersonalisation(
            @Value("${govnotify.template.uploadedAdditionalEvidenceBeforeListing.caseOfficer.email}") String caseOfficerUploadedAdditionalEvidenceBeforeListingTemplateId,
            @Value("${govnotify.template.uploadedAdditionalEvidenceAfterListing.caseOfficer.email}") String caseOfficerUploadedAdditionalEvidenceAfterListingTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            PersonalisationProvider personalisationProvider,
            EmailAddressFinder emailAddressFinder,
            FeatureToggler featureToggler) {
        this.caseOfficerUploadedAdditionalEvidenceBeforeListingTemplateId = caseOfficerUploadedAdditionalEvidenceBeforeListingTemplateId;
        this.caseOfficerUploadedAdditionalEvidenceAfterListingTemplateId = caseOfficerUploadedAdditionalEvidenceAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.emailAddressFinder = emailAddressFinder;
        this.featureToggler = featureToggler;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return featureToggler.getValue("tcw-notifications-feature", false)
                ? Collections.singleton(emailAddressFinder.getHearingCentreEmailAddress(asylumCase))
                : Collections.emptySet();
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
            ? caseOfficerUploadedAdditionalEvidenceAfterListingTemplateId : caseOfficerUploadedAdditionalEvidenceBeforeListingTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UPLOADED_ADDITIONAL_EVIDENCE_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("subjectPrefix", isAcceleratedDetainedAppeal(callback.getCaseDetails().getCaseData())
                ? adaPrefix
                : nonAdaPrefix)
            .putAll(personalisationProvider.getPersonalisation(callback));

        return listCaseFields.build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}
