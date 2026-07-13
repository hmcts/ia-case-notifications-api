package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.editbaildocuments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@Service
public class EditBailDocumentService {

    public List<String> getFormattedDocumentsGivenCaseAndDocNames(BailCase bailCaseBefore,
                                                                           BailCase bailCaseAfter,
                                                                           List<String> docNamesFromCaseNote) {

        List<String> documentsList = new ArrayList<>();

        getListOfDocumentFields().forEach(fieldDefinition -> {

            Optional<List<IdValue<HasDocument>>> maybeDocumentCollectionBefore = bailCaseBefore.read(fieldDefinition);
            if (maybeDocumentCollectionBefore.isPresent()) {
                List<IdValue<HasDocument>> docs = maybeDocumentCollectionBefore.get();
                docs.forEach(doc -> getDocumentNameIfMatch(docNamesFromCaseNote, doc.getValue())
                    .ifPresent(documentsList::add));

                Optional<List<IdValue<HasDocument>>> maybeDocumentCollectionAfter = bailCaseAfter.read(fieldDefinition);
                if (maybeDocumentCollectionAfter.isPresent()) {
                    List<IdValue<HasDocument>> addedDocs = removeDocsWithSameId(maybeDocumentCollectionAfter.get(),
                        maybeDocumentCollectionBefore.get());
                    addedDocs.forEach(doc -> getDocumentNameIfMatch(docNamesFromCaseNote, doc.getValue())
                        .ifPresent(documentsList::add));
                }
            }

        });
        return documentsList;
    }


    private Optional<String> getDocumentNameIfMatch(List<String> docNamesFromCaseNote, HasDocument doc) {
        String documentName = doc.getDocument().getDocumentFilename();
        return docNamesFromCaseNote.contains(documentName) ? Optional.of(documentName) : Optional.empty();
    }

    private List<BailCaseFieldDefinition> getListOfDocumentFields() {
        return Arrays.asList(
            BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA,
            BailCaseFieldDefinition.HOME_OFFICE_DOCUMENTS_WITH_METADATA,
            BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA);
    }

    private List<IdValue<HasDocument>> removeDocsWithSameId(List<IdValue<HasDocument>> minuend,
                                                            List<IdValue<HasDocument>> subtrahend) {
        List<String> subtrahendIds = subtrahend.stream()
            .map(IdValue::getId)
            .collect(Collectors.toList());

        return minuend.stream()
            .filter(idValue -> !subtrahendIds.contains(idValue.getId()))
            .collect(Collectors.toList());
    }

}
