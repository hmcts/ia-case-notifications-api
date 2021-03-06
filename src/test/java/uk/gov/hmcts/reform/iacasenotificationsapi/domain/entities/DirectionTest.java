package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

public class DirectionTest {

    private final String explanation = "Do the thing";
    private final Parties parties = Parties.RESPONDENT;
    private final String dateDue = "2018-12-31T12:34:56";
    private final String dateSent = "2018-12-25";
    private DirectionTag tag = DirectionTag.LEGAL_REPRESENTATIVE_REVIEW;
    private List<IdValue<PreviousDates>> previousDates = Collections.emptyList();
    private List<IdValue<ClarifyingQuestion>> clarifyingQuestions = Collections.emptyList();
    private Direction direction = new Direction(
        explanation,
        parties,
        dateDue,
        dateSent,
        tag,
        previousDates
    );

    private Direction directionWithQuestions = new Direction(
        explanation,
        parties,
        dateDue,
        dateSent,
        tag,
        previousDates,
        clarifyingQuestions
    );

    @Test
    public void should_hold_onto_values() {

        assertEquals(explanation, direction.getExplanation());
        assertEquals(parties, direction.getParties());
        assertEquals(dateDue, direction.getDateDue());
        assertEquals(dateSent, direction.getDateSent());
        assertEquals(tag, direction.getTag());
        assertEquals(previousDates, direction.getPreviousDates());
    }

    @Test
    public void direction_should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new Direction(null, parties, dateDue, dateSent, tag, previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, null, dateDue, dateSent, tag, previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, parties, null, dateSent, tag, previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, parties, dateDue, null, tag, previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, parties, dateDue, dateSent, null, previousDates))
            .isExactlyInstanceOf(NullPointerException.class);
    }


    @Test
    public void direction_with_questions_should_hold_onto_values() {

        assertEquals(explanation, directionWithQuestions.getExplanation());
        assertEquals(parties, directionWithQuestions.getParties());
        assertEquals(dateDue, directionWithQuestions.getDateDue());
        assertEquals(dateSent, directionWithQuestions.getDateSent());
        assertEquals(tag, directionWithQuestions.getTag());
        assertEquals(previousDates, directionWithQuestions.getPreviousDates());
        assertEquals(clarifyingQuestions, directionWithQuestions.getClarifyingQuestions());
    }

    @Test
    public void direction_with_questions_should_not_allow_null_arguments() {

        assertThatThrownBy(
            () -> new Direction(null, parties, dateDue, dateSent, tag, previousDates, clarifyingQuestions))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> new Direction(explanation, null, dateDue, dateSent, tag, previousDates, clarifyingQuestions))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> new Direction(explanation, parties, null, dateSent, tag, previousDates, clarifyingQuestions))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> new Direction(explanation, parties, dateDue, null, tag, previousDates, clarifyingQuestions))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> new Direction(explanation, parties, dateDue, dateSent, null, previousDates, clarifyingQuestions))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, parties, dateDue, dateSent, tag, null, clarifyingQuestions))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
