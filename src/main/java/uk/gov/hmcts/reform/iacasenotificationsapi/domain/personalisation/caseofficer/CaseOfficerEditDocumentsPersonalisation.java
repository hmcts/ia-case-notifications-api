package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class CaseOfficerEditDocumentsPersonalisation implements EmailNotificationPersonalisation {

    private final String appealDocumentDeletedTemplateId;
    private final EmailAddressFinder emailAddressFinder;

    public CaseOfficerEditDocumentsPersonalisation(
        @NotNull(message = "appealDocumentDeletedTemplateId cannot be null")
        @Value("${govnotify.template.appealDocumentDeleted.caseOfficer.email}") String appealDocumentDeletedTemplateId,
        EmailAddressFinder emailAddressFinder) {

        this.appealDocumentDeletedTemplateId = appealDocumentDeletedTemplateId;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_DOCUMENT_DELETED";
    }

    @Override
    public String getTemplateId() {
        return appealDocumentDeletedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getEmailAddress(asylumCase));
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return ImmutableMap.<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(
                AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("reasonForDeletion", getReasonForDeletion(asylumCase))
            .build();
    }

    private String getReasonForDeletion(AsylumCase asylumCase) {
        Optional<List<IdValue<CaseNote>>> caseNotesOptional = asylumCase.read(CASE_NOTES);
        if (caseNotesOptional.isPresent()) {
            List<IdValue<CaseNote>> caseNotes = caseNotesOptional.get();
            String caseNoteDesc = caseNotes.get(0).getValue().getCaseNoteDescription();
            return StringUtils.substringAfter(caseNoteDesc, "reason:").trim();
        }
        return "";
    }

}
