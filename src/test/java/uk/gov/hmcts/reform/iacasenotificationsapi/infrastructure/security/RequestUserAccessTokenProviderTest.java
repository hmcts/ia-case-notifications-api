package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
public class RequestUserAccessTokenProviderTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    private RequestUserAccessTokenProvider requestUserAccessTokenProvider =
        new RequestUserAccessTokenProvider();

    @BeforeEach
    public void setUp() {

        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(httpServletRequest)
        );
    }

    @Test
    public void get_access_token_from_http_request() {

        String expectedAccessToken = "access-token";

        when(httpServletRequest.getHeader("Authorization")).thenReturn(expectedAccessToken);

        String actualAccessToken = requestUserAccessTokenProvider.getAccessToken();

        assertEquals(expectedAccessToken, actualAccessToken);
    }

    @Test
    public void get_missing_access_token_from_http_request_throws_if_not_a_try_attempt() {

        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        assertThatThrownBy(() -> requestUserAccessTokenProvider.getAccessToken())
            .hasMessage("Request access token not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void try_get_access_token_from_http_request() {

        String expectedAccessToken = "access-token";

        when(httpServletRequest.getHeader("Authorization")).thenReturn(expectedAccessToken);

        Optional<String> optionalAccessToken = requestUserAccessTokenProvider.tryGetAccessToken();

        assertTrue(optionalAccessToken.isPresent());
        assertEquals(expectedAccessToken, optionalAccessToken.get());
    }

    @Test
    public void try_get_missing_access_token_from_http_request_returns_empty() {

        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        Optional<String> optionalAccessToken = requestUserAccessTokenProvider.tryGetAccessToken();

        assertFalse(optionalAccessToken.isPresent());
    }
}
