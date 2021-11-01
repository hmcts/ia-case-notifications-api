package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@EqualsAndHashCode
@ToString
public class Document {

    private String documentUrl;
    private String documentBinaryUrl;
    private String documentFilename;
    private String documentHash;

    private Document() {
        // noop -- for deserializer
    }

    public Document(
        String documentUrl,
        String documentBinaryUrl,
        String documentFilename,
        String documentHash
    ) {
        requireNonNull(documentUrl);
        requireNonNull(documentBinaryUrl);
        requireNonNull(documentFilename);
        requireNonNull(documentHash);

        this.documentUrl = documentUrl;
        this.documentBinaryUrl = documentBinaryUrl;
        this.documentFilename = documentFilename;
        this.documentHash = documentHash;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public String getDocumentBinaryUrl() {
        return documentBinaryUrl;
    }

    public String getDocumentFilename() {
        return documentFilename;
    }

    public String getDocumentHash() {
        return documentHash;
    }
}
