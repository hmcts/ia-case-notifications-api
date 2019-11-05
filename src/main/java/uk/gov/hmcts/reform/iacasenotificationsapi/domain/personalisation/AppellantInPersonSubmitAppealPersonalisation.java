package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_PERSON_EMAIL_ADDRESS;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;

@Service
public class AppellantInPersonSubmitAppealPersonalisation implements NotificationPersonalisation {

    private final String appealSubmittedAppellantInPersonTemplateId;

    public AppellantInPersonSubmitAppealPersonalisation(
        @Value("${govnotify.template.appealSubmittedAppellantInPerson}") String appealSubmittedAppellantInPersonTemplateId
    ) {
        this.appealSubmittedAppellantInPersonTemplateId = appealSubmittedAppellantInPersonTemplateId;
    }

    @Override
    public String getTemplateId() {
        return appealSubmittedAppellantInPersonTemplateId;
    }

    @Override
    public String getEmailAddress(AsylumCase asylumCase) {
        return asylumCase
            .read(APPELLANT_IN_PERSON_EMAIL_ADDRESS, String.class)
            .orElseThrow(() -> new IllegalStateException("appellantInPersonEmailAddress is not present"));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_SUBMITTED_APPELLANT_IN_PERSON";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        //TODO: Add See your appeal details link
        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("HO Ref Number", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Given names", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Family name", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .build();
    }
}
