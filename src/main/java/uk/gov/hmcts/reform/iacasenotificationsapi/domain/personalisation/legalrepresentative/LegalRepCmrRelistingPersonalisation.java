package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

@Service
public class LegalRepCmrRelistingPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativeCmrRelistingTemplateId;
    private final String legalRepresentativeRemoteCmrTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final PersonalisationProvider personalisationProvider;
    private final String iaExUiFrontendUrl;

    public LegalRepCmrRelistingPersonalisation(
            @Value("${govnotify.template.listAssistHearing.caseEdited.legalRep.email}") String legalRepresentativeCmrRelistingTemplateId,
            @Value("${govnotify.template.listAssistHearing.caseListed.remoteHearing.legalRep.email}") String legalRepresentativeRemoteCmrTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            PersonalisationProvider personalisationProvider,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.legalRepresentativeCmrRelistingTemplateId = legalRepresentativeCmrRelistingTemplateId;
        this.legalRepresentativeRemoteCmrTemplateId = legalRepresentativeRemoteCmrTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        if (asylumCase.read(CMR_IS_REMOTE_HEARING, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            return legalRepresentativeRemoteCmrTemplateId;
        } else {
            return legalRepresentativeCmrRelistingTemplateId;
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CMR_RELISTED_OR_REMOTE_LEGAL_REPRESENTATIVE_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
                .<String, String>builder()
                .put("linkToOnlineServices", iaExUiFrontendUrl)
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .putAll(personalisationProvider.getPersonalisation(callback));


        return listCaseFields.build();
    }
}