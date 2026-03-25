package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CMR_IS_REMOTE_HEARING;

@Service
public class CaseOfficerCmrRelistingPersonalisation implements EmailNotificationPersonalisation {

    private final String caseOfficerCmrRelistingTemplateId;
    private final String caseOfficerRemoteCmrRelistingTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private final EmailAddressFinder emailAddressFinder;
    private final HearingDetailsFinder hearingDetailsFinder;

    public CaseOfficerCmrRelistingPersonalisation(
            @Value("${govnotify.template.listAssistHearing.caseEdited.caseOfficer.email}") String caseOfficerCmrRelistingTemplateId,
            @Value("${govnotify.template.listAssistHearing.caseEditedRemoteHearing.caseOfficer.email}") String caseOfficerRemoteCmrRelistingTemplateId,

            EmailAddressFinder emailAddressFinder,
            PersonalisationProvider personalisationProvider,
            HearingDetailsFinder hearingDetailsFinder) {
        this.caseOfficerCmrRelistingTemplateId = caseOfficerCmrRelistingTemplateId;
        this.caseOfficerRemoteCmrRelistingTemplateId = caseOfficerRemoteCmrRelistingTemplateId;
        this.emailAddressFinder = emailAddressFinder;
        this.personalisationProvider = personalisationProvider;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (asylumCase.read(CMR_IS_REMOTE_HEARING).orElse(YesOrNo.NO).equals(YesOrNo.YES)) {
            return caseOfficerRemoteCmrRelistingTemplateId;
        } else {
            return caseOfficerCmrRelistingTemplateId;

        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getListCaseCaseOfficerHearingCentreEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CMR_LISTED_CASE_OFFICER";
    }

    //    need to confirm placeholders here
    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final Map<String, String> listCaseFields = new HashMap<>();
        listCaseFields.putAll(personalisationProvider.getPersonalisation(callback));
        listCaseFields.put("hearingCentreAddress", hearingDetailsFinder
                .getHearingCentreLocation(callback.getCaseDetails().getCaseData()));

        return ImmutableMap.copyOf(listCaseFields);
    }
}
