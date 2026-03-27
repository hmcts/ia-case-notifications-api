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
public class CaseOfficerCmrListingPersonalisation implements EmailNotificationPersonalisation {

    private final String caseOfficerCmrListingTemplateId;
    private final String caseOfficerRemoteCmrListingTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private final EmailAddressFinder emailAddressFinder;
    private final HearingDetailsFinder hearingDetailsFinder;

    public CaseOfficerCmrListingPersonalisation(
            @Value("${govnotify.template.listAssistHearing.caseListed.caseOfficer.email}") String caseOfficerCmrListingTemplateId,
            @Value("${govnotify.template.listAssistHearing.caseListed.remoteHearing.caseOfficer.email}") String caseOfficerRemoteCmrListingTemplateId,

            EmailAddressFinder emailAddressFinder,
            PersonalisationProvider personalisationProvider,
            HearingDetailsFinder hearingDetailsFinder) {
        this.caseOfficerCmrListingTemplateId = caseOfficerCmrListingTemplateId;
        this.caseOfficerRemoteCmrListingTemplateId = caseOfficerRemoteCmrListingTemplateId;
        this.emailAddressFinder = emailAddressFinder;
        this.personalisationProvider = personalisationProvider;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (asylumCase.read(CMR_IS_REMOTE_HEARING).orElse(YesOrNo.NO).equals(YesOrNo.YES)) {
            return caseOfficerRemoteCmrListingTemplateId;
        } else {
            return caseOfficerCmrListingTemplateId;

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
    public Map<String, String> getPersonalisation(Callback<   AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final Map<String, String> listCaseFields = new HashMap<>();
        listCaseFields.putAll(personalisationProvider.getPersonalisation(callback));
        listCaseFields.put("hearingCentreAddress", hearingDetailsFinder
                .getHearingCentreLocation(callback.getCaseDetails().getCaseData()));

        return ImmutableMap.copyOf(listCaseFields);
    }
}
