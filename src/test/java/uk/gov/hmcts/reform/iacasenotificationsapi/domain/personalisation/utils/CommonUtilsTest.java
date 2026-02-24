package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.CommonUtils.convertAsylumCaseFeeValue;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CommonUtilsTest {

    @ParameterizedTest
    @MethodSource("generateCanHandleScenarios")
    void should_correctly_convert_asylum_case_value_to_amount(String asylumCaseValue, String expectedValue) {
        assertEquals(expectedValue, convertAsylumCaseFeeValue(asylumCaseValue));
    }

    private static Stream<Arguments> generateCanHandleScenarios() {
        return Stream.of(
            Arguments.of("1000", "10.00"),
            Arguments.of("", ""),
            Arguments.of("80", "0.80")
        );
    }

    @Test
    void should_return_false_when_date_is_today() {
        Optional<String> today = Optional.of(LocalDate.now().toString());
        assertFalse(CommonUtils.isLastEditNotificationNotToday(today));
    }

    @Test
    void should_return_true_when_date_is_yesterday() {
        Optional<String> yesterday = Optional.of(LocalDate.now().minusDays(1).toString());
        assertTrue(CommonUtils.isLastEditNotificationNotToday(yesterday));
    }

    @Test
    void should_return_true_when_date_is_empty() {
        Optional<String> empty = Optional.empty();
        assertTrue(CommonUtils.isLastEditNotificationNotToday(empty));
    }

    @Test
    void should_throw_exception_when_date_is_invalid() {
        Optional<String> invalid = Optional.of("not-a-date");
        assertThrows(Exception.class, () -> CommonUtils.isLastEditNotificationNotToday(invalid));
    }
}