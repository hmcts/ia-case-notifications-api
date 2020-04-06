package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.editdocument;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ADDENDUM_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ADDITIONAL_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DRAFT_DECISION_AND_REASONS_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FINAL_DECISION_AND_REASONS_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_RECORDING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.RESPONDENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_DOCUMENTS;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@Service
public class EditDocumentService {

    public List<String> findDocumentIdsGivenCaseAndDocIds(AsylumCase asylumCase, List<String> docIds) {
        getListOfDocumentFields().forEach(field -> {
            Optional<List<IdValue<DocumentWithMetadata>>> idValuesOptional = asylumCase.read(field);
            if (idValuesOptional.isPresent()) {

            }
        });
        return null;

    }

    private List<AsylumCaseDefinition> getListOfDocumentFields() {
        return Arrays.asList(
            ADDITIONAL_EVIDENCE_DOCUMENTS,
            TRIBUNAL_DOCUMENTS,
            HEARING_DOCUMENTS,
            LEGAL_REPRESENTATIVE_DOCUMENTS,
            ADDENDUM_EVIDENCE_DOCUMENTS,
            RESPONDENT_DOCUMENTS,
            DRAFT_DECISION_AND_REASONS_DOCUMENTS,
            FINAL_DECISION_AND_REASONS_DOCUMENTS,
            HEARING_RECORDING_DOCUMENTS);
    }
}
