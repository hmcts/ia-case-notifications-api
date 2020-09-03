package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.security;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Qualifier("requestUser")
public class RequestUserAccessTokenProvider implements AccessTokenProvider {

    private static final String AUTHORIZATION = "Authorization";
    private final RequestAttributes requestAttributes;

    public RequestUserAccessTokenProvider() {
        requestAttributes = RequestContextHolder.getRequestAttributes();
    }

    public String getAccessToken() {
        return tryGetAccessToken()
            .orElseThrow(() -> new IllegalStateException("Request access token not present"));
    }

    public Optional<String> tryGetAccessToken() {

        if (RequestContextHolder.getRequestAttributes() != null) {
            return Optional.ofNullable(
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest()
                    .getHeader(AUTHORIZATION)
            );
        }

        throw new IllegalStateException("No current HTTP request");
    }
}
