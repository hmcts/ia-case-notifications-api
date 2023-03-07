package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * This bean provides the email address for Detention Engagement Team for unrepresented ADA cases.
 */
@Service
public class DetEmailService {
    private String adaDetEmailAddress;

    public DetEmailService(@Value("${adaDetUnrepresentedEmailAddress}") String adaDetEmailAddress) {
        this.adaDetEmailAddress = adaDetEmailAddress;
    }

    public String getAdaDetEmailAddress() {
        return adaDetEmailAddress;
    }
}
