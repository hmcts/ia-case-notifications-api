package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CMR_IS_REMOTE_HEARING;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeCmrRelistingPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String listAssistHearingLegalRepresentativeCaseEditedTemplateId;
    private final String listAssistHearingLegalRepresentativeCaseEditedRemoteHearingTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeCmrRelistingPersonalisation(
        @Value("${govnotify.template.listAssistHearing.caseEdited.legalRep.email}") String listAssistHearingLegalRepresentativeCaseEditedTemplateId,
        @Value("${govnotify.template.listAssistHearing.caseEditedRemoteHearing.legalRep.email}") String listAssistHearingLegalRepresentativeCaseEditedRemoteHearingTemplateId,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.listAssistHearingLegalRepresentativeCaseEditedTemplateId = listAssistHearingLegalRepresentativeCaseEditedTemplateId;
        this.listAssistHearingLegalRepresentativeCaseEditedRemoteHearingTemplateId = listAssistHearingLegalRepresentativeCaseEditedRemoteHearingTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        boolean isRemote = asylumCase.read(CMR_IS_REMOTE_HEARING, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES;
        return isRemote
            ? listAssistHearingLegalRepresentativeCaseEditedRemoteHearingTemplateId
            : listAssistHearingLegalRepresentativeCaseEditedTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CMR_RE_LISTING_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation(callback))
            .putAll(personalisationProvider.getPersonalisation(callback))
            .put("subjectPrefix", isAcceleratedDetainedAppeal(callback.getCaseDetails().getCaseData())
                ? adaPrefix
                : nonAdaPrefix)
            .build();
    }
}
