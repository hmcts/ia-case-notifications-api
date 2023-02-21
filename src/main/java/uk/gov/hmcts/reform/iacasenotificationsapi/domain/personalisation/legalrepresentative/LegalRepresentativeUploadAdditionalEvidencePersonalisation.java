package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeUploadAdditionalEvidencePersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepUploadedAdditionalEvidenceBeforeListingTemplateId;
    private final String legalRepUploadedAdditionalEvidenceAfterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeUploadAdditionalEvidencePersonalisation(
        @Value("${govnotify.template.uploadedAdditionalEvidenceBeforeListing.legalRep.email}") String legalRepUploadedAdditionalEvidenceBeforeListingTemplateId,
        @Value("${govnotify.template.uploadedAdditionalEvidenceAfterListing.legalRep.email}") String legalRepUploadedAdditionalEvidenceAfterListingTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.legalRepUploadedAdditionalEvidenceBeforeListingTemplateId = legalRepUploadedAdditionalEvidenceBeforeListingTemplateId;
        this.legalRepUploadedAdditionalEvidenceAfterListingTemplateId = legalRepUploadedAdditionalEvidenceAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
            ? legalRepUploadedAdditionalEvidenceAfterListingTemplateId : legalRepUploadedAdditionalEvidenceBeforeListingTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UPLOADED_ADDITIONAL_EVIDENCE_LEGAL_REP";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder();
        listCaseFields.putAll(customerServicesProvider.getCustomerServicesPersonalisation());
        listCaseFields.put("subjectPrefix", isAcceleratedDetainedAppeal(callback.getCaseDetails().getCaseData())
            ? adaPrefix
            : nonAdaPrefix);
        listCaseFields.put("linkToOnlineService", iaExUiFrontendUrl);
        listCaseFields.putAll(personalisationProvider.getPersonalisation(callback));

        return listCaseFields.build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}
