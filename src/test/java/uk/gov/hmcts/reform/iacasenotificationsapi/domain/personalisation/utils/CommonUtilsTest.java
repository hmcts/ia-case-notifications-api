package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.CommonUtils.bailNotificationAlreadySentToday;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.CommonUtils.convertAsylumCaseFeeValue;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.CommonUtils.notificationAlreadySentToday;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;

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
    void should_return_false_if_asylum_notification_not_already_sent_today() {
        // given
        AsylumCase asylumCase = mock(AsylumCase.class);

        // when
        // then
        assertFalse(notificationAlreadySentToday(asylumCase));
    }

    @Test
    void should_return_true_if_bail_notification_already_sent_today() {
        // given
        BailCase bailCase = mock(BailCase.class);

        // when
        // then
        assertFalse(bailNotificationAlreadySentToday(bailCase));
    }
}