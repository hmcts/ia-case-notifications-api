package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.serialization;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StdSerializerTest {

    @Mock
    private ObjectMapper mapper;

    private StdSerializer<Integer> stdSerializer;

    @BeforeEach
    public void setUp() {
        stdSerializer = new StdSerializer<>(mapper);
    }

    @Test
    public void should_serialize_argument_to_string() throws JsonProcessingException {

        Integer source = 123;
        String expectedSerializedSource = "123";

        doReturn(expectedSerializedSource)
            .when(mapper)
            .writeValueAsString(source);

        String actualSerializedSource = stdSerializer.serialize(source);

        assertEquals(expectedSerializedSource, actualSerializedSource);
    }

    @Test
    public void should_convert_checked_exception_to_runtime_on_error() throws JsonProcessingException {

        Integer source = 123;

        doThrow(mock(JsonProcessingException.class))
            .when(mapper)
            .writeValueAsString(source);

        assertThatThrownBy(() -> stdSerializer.serialize(source))
            .hasMessage("Could not serialize data")
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
