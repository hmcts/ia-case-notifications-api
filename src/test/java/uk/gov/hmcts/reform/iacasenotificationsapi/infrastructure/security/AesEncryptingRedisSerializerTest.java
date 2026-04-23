package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.model.idam.UserInfo;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AesEncryptingRedisSerializerTest {

    private static final String VALID_KEY_256 = Base64.getEncoder()
        .encodeToString("01234567890123456789012345678901".getBytes());

    private AesEncryptingRedisSerializer<String> serializer;
    private Jackson2JsonRedisSerializer<String> delegateSerializer;

    @BeforeEach
    void setUp() {
        delegateSerializer = new Jackson2JsonRedisSerializer<>(String.class);
        serializer = new AesEncryptingRedisSerializer<>(delegateSerializer, VALID_KEY_256);
    }

    @Test
    void serialize_null_returnsNull() {
        assertNull(serializer.serialize(null));
    }

    @Test
    void deserialize_null_returnsNull() {
        assertNull(serializer.deserialize(null));
    }

    @Test
    void roundTrip_plainString() {
        String original = "Bearer eyJhbGciOiJSUzI1NiJ9.sometoken";

        byte[] serialized = serializer.serialize(original);
        String deserialized = serializer.deserialize(serialized);

        assertEquals(original, deserialized);
    }

    @Test
    void roundTrip_pojoDelegate() {
        Jackson2JsonRedisSerializer<UserInfo> userInfoDelegate =
            new Jackson2JsonRedisSerializer<>(UserInfo.class);
        AesEncryptingRedisSerializer<UserInfo> userInfoSerializer =
            new AesEncryptingRedisSerializer<>(userInfoDelegate, VALID_KEY_256);

        UserInfo userInfo = new UserInfo();

        byte[] serialized = userInfoSerializer.serialize(userInfo);
        UserInfo deserialized = userInfoSerializer.deserialize(serialized);

        assertEquals(userInfo.getEmail(), deserialized.getEmail());
        assertEquals(userInfo.getName(), deserialized.getName());
    }

    @Test
    void serialize_doesNotContainPlaintext() {
        String token = "Bearer token";

        byte[] encrypted = serializer.serialize(token);

        assertFalse(new String(encrypted).contains("token"));
    }

    @Test
    void serialize_sameInputProducesDifferentCiphertexts() {
        String token = "Bearer sometoken";

        byte[] first = serializer.serialize(token);
        byte[] second = serializer.serialize(token);

        // Different encrypted text, even for the same token
        assertNotEquals(second, first);
    }

    @Test
    void differentKeys_cannotDecryptEachOther() {
        String keyA = Base64.getEncoder().encodeToString("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA".getBytes());
        String keyB = Base64.getEncoder().encodeToString("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB".getBytes());

        AesEncryptingRedisSerializer<String> serializerA =
            new AesEncryptingRedisSerializer<>(delegateSerializer, keyA);
        AesEncryptingRedisSerializer<String> serializerB =
            new AesEncryptingRedisSerializer<>(delegateSerializer, keyB);

        byte[] encryptedWithA = serializerA.serialize("Bearer sometoken");

        // only key A can decrypt
        assertThrows(SerializationException.class, () -> serializerB.deserialize(encryptedWithA));
    }
}
