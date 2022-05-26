package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static java.util.Objects.requireNonNull;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class BailDirection {

    private String sendDirectionDescription;
    private String sendDirectionList;
    private String dateOfCompliance;
    private String dateSent;


    private BailDirection() {
    }

    public BailDirection(
        String sendDirectionDescription,
        String sendDirectionList,
        String dateOfCompliance,
        String dateSent
    ) {
        this.sendDirectionDescription = requireNonNull(sendDirectionDescription);
        this.sendDirectionList = requireNonNull(sendDirectionList);
        this.dateOfCompliance = requireNonNull(dateOfCompliance);
        this.dateSent = requireNonNull(dateSent);
    }


    public String getSendDirectionDescription() {
        return requireNonNull(sendDirectionDescription);
    }

    public String getSendDirectionList() {
        return requireNonNull(sendDirectionList);
    }

    public String getDateOfCompliance() {
        return requireNonNull(dateOfCompliance);
    }

    public String getDateSent() {
        return requireNonNull(dateSent);
    }


}
