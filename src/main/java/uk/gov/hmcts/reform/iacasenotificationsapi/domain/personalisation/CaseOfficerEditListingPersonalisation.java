package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.BasePersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class CaseOfficerEditListingPersonalisation implements NotificationPersonalisation {

    private final String caseOfficerCaseEditedTemplateId;
    private final BasePersonalisationProvider basePersonalisationProvider;
    private final EmailAddressFinder emailAddressFinder;

    public CaseOfficerEditListingPersonalisation(
        @Value("${govnotify.template.caseOfficerCaseEdited}") String caseOfficerCaseEditedTemplateId,
        EmailAddressFinder emailAddressFinder,
        BasePersonalisationProvider basePersonalisationProvider) {
        this.caseOfficerCaseEditedTemplateId = caseOfficerCaseEditedTemplateId;
        this.emailAddressFinder = emailAddressFinder;
        this.basePersonalisationProvider = basePersonalisationProvider;
    }

    @Override
    public String getTemplateId() {
        return caseOfficerCaseEditedTemplateId;
    }

    @Override
    public String getEmailAddress(AsylumCase asylumCase) {
        return emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_RE_LISTED_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return basePersonalisationProvider.getEditCaseListingPersonalisation(callback);

    }
}
