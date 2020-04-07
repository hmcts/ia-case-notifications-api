package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.editdocument;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.editdocument.CaseOfficerEditDocumentsPersonalisationTest.DOC_ID;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.editdocument.CaseOfficerEditDocumentsPersonalisationTest.DOC_ID2;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@RunWith(JUnitParamsRunner.class)
public class EditDocumentServiceTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    private EditDocumentService editDocumentService = new EditDocumentService();

    @Test
    @Parameters(method = "generateOneFileEditedScenarios")
    public void getFormattedDocumentsGivenCaseAndDocIds(AsylumCase asylumCase, List<String> docIds,
                                                        FormattedDocumentList expectedFormattedDocumentList) {
        FormattedDocumentList actualFormattedDocumentList =
            editDocumentService.getFormattedDocumentsGivenCaseAndDocIds(asylumCase, docIds);

        System.out.println(actualFormattedDocumentList.toString());
        assertThat(actualFormattedDocumentList.toString()).isEqualTo(expectedFormattedDocumentList.toString());
    }

    private Object[] generateOneFileEditedScenarios() {
        AsylumCase asylumCase = new AsylumCase();
        IdValue<DocumentWithMetadata> idDoc = getDocumentWithMetadata(
            DOC_ID, "some name", "some desc");
        IdValue<DocumentWithMetadata> idDoc2 = getDocumentWithMetadata(
            DOC_ID2, "some other name", "some other desc");
        asylumCase.write(LEGAL_REPRESENTATIVE_DOCUMENTS, Arrays.asList(idDoc, idDoc2));

        List<String> doc2IsEdited = Collections.singletonList(DOC_ID2);
        List<String> doc1IsEdited = Collections.singletonList(DOC_ID);

        FormattedDocument expectedFormattedDocumentIsDoc2 =
            new FormattedDocument("some other name", "some other desc");

        FormattedDocument expectedFormattedDocumentIsDoc1 =
            new FormattedDocument("some name", "some desc");

        return new Object[]{
            new Object[]{asylumCase, doc2IsEdited,
                new FormattedDocumentList(Collections.singletonList(expectedFormattedDocumentIsDoc2))},
            new Object[]{asylumCase, doc1IsEdited,
                new FormattedDocumentList(Collections.singletonList(expectedFormattedDocumentIsDoc1))}
        };
    }

    private IdValue<DocumentWithMetadata> getDocumentWithMetadata(String docId, String filename,
                                                                  String description) {
        String documentUrl = "http://dm-store/" + docId;
        Document doc = new Document(documentUrl, documentUrl + "/binary", filename);
        DocumentWithMetadata docWithMetadata = new DocumentWithMetadata(doc, description, LocalDate.now().toString(),
            DocumentTag.NONE);
        return new IdValue<>("1", docWithMetadata);
    }

}