package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

import java.util.Map;

import static java.util.Objects.requireNonNull;


@Service
public class LegalRepresentativeReListCaseStatutoryTimeframe24WeeksPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String templateId;
    private final PersonalisationProvider personalisationProvider;

    public LegalRepresentativeReListCaseStatutoryTimeframe24WeeksPersonalisation(
        @NotNull(message = "templateId cannot be null")
        @Value("${govnotify.template.reListCase.legalRep24Weeks.email}") String templateId,
        PersonalisationProvider personalisationProvider
    ) {
        this.templateId = templateId;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_STATUTORY_TIMEFRAME_24WEEKS_RE_LIST_CASE_LEGAL_REPRESENTATIVE";
    }

    @Override
    public String getTemplateId() {
        return templateId;
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .putAll(personalisationProvider.getPersonalisation(callback))
                .build();
    }
}
