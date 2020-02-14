package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import com.launchdarkly.client.LDClient;
import com.launchdarkly.client.LDUser;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;

@Service
public class LaunchDarklyFeatureToggler implements FeatureToggler {

    private LDClient ldClient;
    private UserDetailsProvider userDetailsProvider;

    public LaunchDarklyFeatureToggler(LDClient ldClient,
                                      UserDetailsProvider userDetailsProvider) {
        this.ldClient = ldClient;
        this.userDetailsProvider = userDetailsProvider;
    }

    public boolean getValue(String key, Boolean defaultValue) {

        UserDetails userDetails = userDetailsProvider.getUserDetails();

        return ldClient.boolVariation(
            key,
            new LDUser.Builder(userDetails.getId())
                .firstName(userDetails.getForename())
                .lastName(userDetails.getSurname())
                .email(userDetails.getEmailAddress())
                .build(),
            defaultValue
        );
    }

}
