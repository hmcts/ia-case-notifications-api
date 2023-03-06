package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DetEmailService {
    private String emailAddress;

    public DetEmailService(@Value("${detUnrepresentedEmailAddress}") String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmail() {
        return emailAddress;
    }
}
