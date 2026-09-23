package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.serialization;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
public class StdSerializerTest {

    @Mock
    private JsonMapper mapper;

    private StdSerializer<Integer> stdSerializer;

    @BeforeEach
    public void setUp() {
        stdSerializer = new StdSerializer<>(mapper);
    }

    @Test
    public void should_serialize_argument_to_string() {

        Integer source = 123;
        String expectedSerializedSource = "123";

        doReturn(expectedSerializedSource)
            .when(mapper)
            .writeValueAsString(source);

        String actualSerializedSource = stdSerializer.serialize(source);

        assertEquals(expectedSerializedSource, actualSerializedSource);
    }

    @Test
    public void should_convert_checked_exception_to_runtime_on_error() {

        Integer source = 123;

        doThrow(mock(JacksonException.class))
            .when(mapper)
            .writeValueAsString(source);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> stdSerializer.serialize(source));
        assertEquals("Could not serialize data", exception.getMessage());
    }
}
