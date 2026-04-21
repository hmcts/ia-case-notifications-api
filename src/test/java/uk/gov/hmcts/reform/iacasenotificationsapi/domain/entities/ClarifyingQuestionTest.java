package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ClarifyingQuestionTest {

    private final String question = "When did this happen?";

    private final ClarifyingQuestion clarifyingQuestions = new ClarifyingQuestion(
        question
    );

    @Test
    public void should_hold_onto_values() {
        assertEquals(question, clarifyingQuestions.getQuestion());
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThrows(NullPointerException.class,
            () -> new ClarifyingQuestion(null));

    }
}
