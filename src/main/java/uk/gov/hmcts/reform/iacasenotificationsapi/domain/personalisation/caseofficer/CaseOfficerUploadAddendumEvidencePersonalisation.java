package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class CaseOfficerUploadAddendumEvidencePersonalisation implements EmailNotificationPersonalisation {

    private final String caseOfficerUploadedAddendumEvidenceTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private final EmailAddressFinder emailAddressFinder;

    public CaseOfficerUploadAddendumEvidencePersonalisation(
        @Value("${govnotify.template.uploadedAddendumEvidence.caseOfficer.email}") String caseOfficerUploadedAddendumEvidenceTemplateId,
        PersonalisationProvider personalisationProvider,
        EmailAddressFinder emailAddressFinder
    ) {
        this.caseOfficerUploadedAddendumEvidenceTemplateId = caseOfficerUploadedAddendumEvidenceTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UPLOADED_ADDENDUM_EVIDENCE_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return personalisationProvider.getPersonalisation(callback);
    }

    @Override
    public String getTemplateId() {
        return caseOfficerUploadedAddendumEvidenceTemplateId;
    }
}
