package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import java.util.List;
import java.util.Objects;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

public class TimeExtension {
    private String requestedDate;
    private String reason;
    private State state;
    private TimeExtensionStatus status;
    private List<IdValue<Document>> evidence;
    private TimeExtensionDecision decision;
    private String decisionReason;
    private String decisionDueDate;

    private TimeExtension() {
    }

    public TimeExtension(String requestedDate, String reason, State state, TimeExtensionStatus status, List<IdValue<Document>> evidence) {
        this(requestedDate, reason, state, status, evidence, null, null, null);
    }

    public TimeExtension(String requestedDate, String reason, State state, TimeExtensionStatus status, List<IdValue<Document>> evidence, TimeExtensionDecision decision, String decisionReason, String decisionDueDate) {
        this.requestedDate = requestedDate;
        this.reason = reason;
        this.state = state;
        this.status = status;
        this.evidence = evidence;
        this.decision = decision;
        this.decisionReason = decisionReason;
        this.decisionDueDate = decisionDueDate;
    }

    public String getRequestedDate() {
        return requestedDate;
    }

    public String getReason() {
        return reason;
    }

    public State getState() {
        return state;
    }

    public TimeExtensionStatus getStatus() {
        return status;
    }

    public List<IdValue<Document>> getEvidence() {
        return evidence;
    }

    public TimeExtensionDecision getDecision() {
        return decision;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public String getDecisionDueDate() {
        return decisionDueDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimeExtension that = (TimeExtension) o;
        return Objects.equals(requestedDate, that.requestedDate)
                && Objects.equals(reason, that.reason)
                && state == that.state
                && status == that.status
                && Objects.equals(evidence, that.evidence)
                && decision == that.decision
                && Objects.equals(decisionReason, that.decisionReason)
                && Objects.equals(decisionDueDate, that.decisionDueDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestedDate, reason, state, status, evidence, decision, decisionReason, decisionDueDate);
    }

    @Override
    public String toString() {
        return "TimeExtension{"
                + "requestedDate='" + requestedDate + '\''
                + ", reason='" + reason + '\''
                + ", state=" + state
                + ", status=" + status
                + ", evidence=" + evidence
                + ", decision=" + decision
                + ", decisionReason='" + decisionReason + '\''
                + ", decisionDueDate='" + decisionDueDate + '\''
                + '}';
    }
}
