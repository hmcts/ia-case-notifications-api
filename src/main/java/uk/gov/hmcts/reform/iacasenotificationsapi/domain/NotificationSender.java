package uk.gov.hmcts.reform.iacasenotificationsapi.domain;

import java.util.Map;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.CaseType;


public interface NotificationSender {

    String sendEmail(
        String templateId,
        String emailAddress,
        Map<String, String> personalisation,
        String reference,
        CaseType caseType
    );

    String sendSms(
        String templateId,
        String phoneNumber,
        Map<String, String> personalisation,
        String reference,
        CaseType caseType
    );
}
