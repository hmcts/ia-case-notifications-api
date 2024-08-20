package uk.gov.hmcts.reform.iacasenotificationsapi.domain;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;

import java.util.Map;


public interface NotificationSender<T extends CaseData> {

    String sendEmail(
        String templateId,
        String emailAddress,
        Map<String, String> personalisation,
        String reference
    );

    String sendSms(
        String templateId,
        String phoneNumber,
        Map<String, String> personalisation,
        String reference,
        Callback<T> callback
    );

}
