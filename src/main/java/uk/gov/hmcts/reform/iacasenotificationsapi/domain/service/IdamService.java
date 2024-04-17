package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.model.idam.UserInfo;

@Component
public class IdamService {

    private final IdamApi idamApi;

    public IdamService(
        IdamApi idamApi
    ) {
        this.idamApi = idamApi;
    }

    @Cacheable(value = "userInfoCache")
    public UserInfo getUserInfo(String accessToken) {
        return idamApi.userInfo(accessToken);
    }
}
