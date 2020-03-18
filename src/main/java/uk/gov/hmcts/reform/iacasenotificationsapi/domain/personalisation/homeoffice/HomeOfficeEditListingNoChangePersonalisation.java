package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

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
public class HomeOfficeEditListingNoChangePersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeCaseEditedNoChangeTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private EmailAddressFinder emailAddressFinder;

    public HomeOfficeEditListingNoChangePersonalisation(
        @Value("${govnotify.template.caseEditedNoChange.homeOffice.email}") String homeOfficeCaseEditedNoChangeTemplateId,
        EmailAddressFinder emailAddressFinder,
        PersonalisationProvider personalisationProvider
    ) {
        this.homeOfficeCaseEditedNoChangeTemplateId = homeOfficeCaseEditedNoChangeTemplateId;
        this.emailAddressFinder = emailAddressFinder;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId() {
        return homeOfficeCaseEditedNoChangeTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_RE_LISTED_NO_CHANGE_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return personalisationProvider.getPersonalisation(callback);
    }
}
