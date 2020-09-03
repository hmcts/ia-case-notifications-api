package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.security;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Qualifier("requestUser")
public class RequestUserAccessTokenProvider implements AccessTokenProvider {

    private static final String AUTHORIZATION = "Authorization";

    public String getAccessToken() {
        return tryGetAccessToken()
            .orElseThrow(() -> new IllegalStateException("Request access token not present"));
    }

    public Optional<String> tryGetAccessToken() {

        return Optional
            .ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            .map(ServletRequestAttributes::getRequest)
            .map(request -> request.getHeader(AUTHORIZATION));
    }
}
