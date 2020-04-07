package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.editdocument;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.productowner.CaseOfficerEditDocumentsPersonalisationTest.DOC_ID;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

public class EditDocumentServiceTest {

    @Test
    public void findDocumentIdsGivenCaseAndDocIds() {
        EditDocumentService editDocumentService = new EditDocumentService();
        AsylumCase asylumCase = new AsylumCase();
        writeDocument(asylumCase);
        List<String> docIds = Collections.singletonList(DOC_ID);

        FormattedDocumentList actualFormattedDocumentList =
            editDocumentService.getFormattedDocumentsGivenCaseAndDocIds(asylumCase, docIds);

        System.out.println(actualFormattedDocumentList.toString());

        FormattedDocument expectedFormattedDocument =
            new FormattedDocument("some doc name", "some doc desc");
        assertThat(actualFormattedDocumentList.getFormattedDocuments()).containsOnly(expectedFormattedDocument);

        FormattedDocumentList expectedFormattedDocumentList =
            new FormattedDocumentList(Collections.singletonList(expectedFormattedDocument));
        assertThat(actualFormattedDocumentList.toString()).isEqualTo(expectedFormattedDocumentList.toString());
    }

    private void writeDocument(AsylumCase asylumCase) {
        String documentUrl = "http://dm-store/" + DOC_ID;
        Document doc = new Document(documentUrl, documentUrl + "/binary",
            "some doc name");
        DocumentWithMetadata docWithMetadata = new DocumentWithMetadata(doc, "some doc desc",
            LocalDate.now().toString(), DocumentTag.NONE);
        IdValue<DocumentWithMetadata> idDoc = new IdValue<>("1", docWithMetadata);
        List<IdValue<DocumentWithMetadata>> legalDocs = Collections.singletonList(idDoc);
        asylumCase.write(LEGAL_REPRESENTATIVE_DOCUMENTS, legalDocs);
    }

}