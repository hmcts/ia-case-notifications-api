package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.editdocument;

import java.util.List;
import lombok.Value;

@Value
public class FormattedDocumentList {
    List<FormattedDocument> formattedDocuments;

    @Override
    public String toString() {
        return formattedDocuments.toString()
            .replace("[", "")
            .replace("]", "")
            .trim();
    }
}
