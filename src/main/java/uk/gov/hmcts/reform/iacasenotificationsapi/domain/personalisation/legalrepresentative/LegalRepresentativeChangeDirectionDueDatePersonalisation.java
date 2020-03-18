package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

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
public class LegalRepresentativeChangeDirectionDueDatePersonalisation implements EmailNotificationPersonalisation {

    private static final String legalRepChangeDirectionDueDateSuffix = "_LEGAL_REP_CHANGE_DIRECTION_DUE_DATE";

    private final String legalRepChangeDirectionDueDateTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private final EmailAddressFinder emailAddressFinder;


    public LegalRepresentativeChangeDirectionDueDatePersonalisation(
        @Value("${govnotify.template.changeDirectionDueDate.legalRep.email}") String legalRepChangeDirectionDueDateTemplateId,
        PersonalisationProvider personalisationProvider,
        EmailAddressFinder emailAddressFinder) {

        this.legalRepChangeDirectionDueDateTemplateId = legalRepChangeDirectionDueDateTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId() {
        return legalRepChangeDirectionDueDateTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + legalRepChangeDirectionDueDateSuffix;
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return personalisationProvider.getPersonalisation(callback);
    }
}
