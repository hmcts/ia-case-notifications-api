package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.editdocument;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FormattedDocumentListTest {

    @Test
    public void toStringTest() {
        FormattedDocument doc1 = new FormattedDocument("some name", "some desc");
        FormattedDocument doc2 = new FormattedDocument("some other name", "some other desc");
        List<FormattedDocument> formattedDocuments = Arrays.asList(doc1, doc2);
        FormattedDocumentList formattedDocumentList = new FormattedDocumentList(formattedDocuments);

        String actual = formattedDocumentList.toString();
        String expected = "Document: \nsome name\nDescription: \nsome desc" +
            System.getProperty("line.separator") +
            System.getProperty("line.separator") +
            "Document: \nsome other name\nDescription: \nsome other desc";

        assertEquals(expected, actual);
    }
}
