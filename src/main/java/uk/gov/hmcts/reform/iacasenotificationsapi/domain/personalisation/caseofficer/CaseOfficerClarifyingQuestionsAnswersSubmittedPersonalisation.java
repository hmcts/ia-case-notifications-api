package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class CaseOfficerClarifyingQuestionsAnswersSubmittedPersonalisation implements EmailNotificationPersonalisation {

    private final String submitClarifyingQuestionsAnswersCaseOfficerSmsTemplateId;
    private final String iaExUiFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;


    public CaseOfficerClarifyingQuestionsAnswersSubmittedPersonalisation(
        @NotNull(message = "submitClarifyingQuestionsAnswersCaseOfficerSmsTemplateId cannot be null") @Value("${govnotify.template.submitClarifyingQuestionsAnswers.caseOfficer.email}") String submitClarifyingQuestionsAnswersCaseOfficerSmsTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        EmailAddressFinder emailAddressFinder
    ) {
        this.submitClarifyingQuestionsAnswersCaseOfficerSmsTemplateId = submitClarifyingQuestionsAnswersCaseOfficerSmsTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId() {
        return submitClarifyingQuestionsAnswersCaseOfficerSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CLARIFYING_QUESTION_ANSWERS_SUBMITTED_CASE_OFFICER_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase cannot be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Appellant Given names", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Appellant Family name", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("Hyperlink to service", iaExUiFrontendUrl)
                .build();
    }
}
