package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CaseDetails<T extends CaseData> {

    private long id;
    private String jurisdiction;
    private State state;
    private T caseData;
    private LocalDateTime createdDate;

    private CaseDetails() {
        // noop -- for deserializer
    }

    public CaseDetails(
        long id,
        String jurisdiction,
        State state,
        T caseData,
        LocalDateTime createdDate
    ) {
        this.id = id;
        this.jurisdiction = jurisdiction;
        this.state = state;
        this.caseData = caseData;
        this.createdDate = createdDate;
    }

    public long getId() {
        return id;
    }

    public String getJurisdiction() {

        if (jurisdiction == null) {
            throw new RequiredFieldMissingException("jurisdiction field is required");
        }

        return jurisdiction;
    }

    public State getState() {

        if (state == null) {
            throw new RequiredFieldMissingException("state field is required");
        }

        return state;
    }

    public T getCaseData() {

        if (caseData == null) {
            throw new RequiredFieldMissingException("caseData field is required");
        }

        return caseData;
    }

    public LocalDateTime getCreatedDate() {

        if (createdDate == null) {
            throw new RequiredFieldMissingException("createdDate field is required");
        }

        return createdDate;
    }
}
