package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PersonalisationUtilsTest {

    @Test
    public void defaultDateFormat_returns_correct_date_format() {
        String input = "2022-01-01";
        String expected = "1 Jan 2022";
        assertEquals(expected, PersonalisationUtils.defaultDateFormat(input));
    }

    @Test
    public void defaultDateFormat_returns_given_input_for_invalid_date() {
        String input = "2022-13/35";
        assertEquals(input, PersonalisationUtils.defaultDateFormat(input));
    }

    @Test
    public void formatCaseId_returns_correct_caseId_format() {
        long input = 1234555577779999L;
        String expected = "1234-5555-7777-9999";
        assertEquals(expected, PersonalisationUtils.formatCaseId(input));
    }

    @Test
    public void formatCaseId_returns_given_input_as_string_for_invalid_caseId() {
        long input = 12345555L;
        String expected = "12345555";
        assertEquals(expected, PersonalisationUtils.formatCaseId(input));
    }

}