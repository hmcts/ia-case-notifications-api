package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BailDirectionTest {

    private static String TEST_VALUE = "some-value";

    private BailDirection bailDirection = new BailDirection(
        TEST_VALUE,
        TEST_VALUE,
        TEST_VALUE,
        TEST_VALUE,
        TEST_VALUE,
        TEST_VALUE
    );

    @Test
    public void should_hold_onto_values() {

        assertEquals(TEST_VALUE, bailDirection.getSendDirectionList());
        assertEquals(TEST_VALUE, bailDirection.getSendDirectionDescription());
        assertEquals(TEST_VALUE, bailDirection.getDateOfCompliance());
        assertEquals(TEST_VALUE, bailDirection.getDateSent());
        assertEquals(TEST_VALUE, bailDirection.getDateTimeDirectionCreated());
        assertEquals(TEST_VALUE, bailDirection.getDateTimeDirectionModified());

    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new BailDirection(null, null, null, null, null, null))
            .isExactlyInstanceOf(NullPointerException.class);

    }
}
