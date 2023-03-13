package uk.gov.hmcts.reform.iacasenotificationsapi;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

public class TestUtils {

    private TestUtils(){

    }

    public static List<IdValue<DocumentWithMetadata>> getDocumentWithMetadataList(String docId, String filename,
                                                                                  String description, DocumentTag tag) {
        return Arrays.asList(new IdValue<>(docId, getDocumentWithMetadata(docId, filename, description, tag)));
    }

    public static DocumentWithMetadata getDocumentWithMetadata(String docId, String filename,
                                                                                  String description, DocumentTag tag) {
        String documentUrl = "http://dm-store/" + docId;
        Document document = new Document(documentUrl, documentUrl + "/binary", filename);

        return new DocumentWithMetadata(document, description, LocalDate.now().toString(), tag);
    }
}
