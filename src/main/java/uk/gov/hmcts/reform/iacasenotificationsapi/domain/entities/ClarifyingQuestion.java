package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

import static java.util.Objects.requireNonNull;

public class ClarifyingQuestion {
    private String question;

    @JsonCreator
    private ClarifyingQuestion() {
    }

    public ClarifyingQuestion(String question) {
        requireNonNull(question);
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }
}
