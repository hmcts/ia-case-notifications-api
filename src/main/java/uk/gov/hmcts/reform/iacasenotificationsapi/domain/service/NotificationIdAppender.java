package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@Component
@Slf4j
@SuppressWarnings("unchecked")
public class NotificationIdAppender {

    public void appendAllAsylum(final AsylumCase asylumCase, final String referenceId, final List<String> notificationIds) {
        List<IdValue<String>> existingNotificationSent =
            asylumCase.read(AsylumCaseDefinition.NOTIFICATIONS_SENT, List.class).orElseGet(ArrayList::new);
        asylumCase.write(AsylumCaseDefinition.NOTIFICATIONS_SENT,
            appendAll(existingNotificationSent, notificationIds, referenceId));
    }

    public void appendAllBail(final BailCase bailCase, final String referenceId, final List<String> notificationIds) {
        List<IdValue<String>> existingNotificationSent =
            bailCase.read(BailCaseFieldDefinition.NOTIFICATIONS_SENT, List.class).orElseGet(ArrayList::new);
        bailCase.write(BailCaseFieldDefinition.NOTIFICATIONS_SENT,
            appendAll(existingNotificationSent, notificationIds, referenceId));
    }

    public List<IdValue<String>> append(
        List<IdValue<String>> existingNotificationsSent,
        String notificationReference,
        String notificationId
    ) {
        long numberOfExistingNotificationsOfSameKind =
            existingNotificationsSent
                .stream()
                .map(IdValue::getId)
                .filter(existingNotificationReference -> existingNotificationReference.startsWith(notificationReference))
                .count();
        long timestamp = Instant.now().toEpochMilli();
        String qualifiedNotificationReference = numberOfExistingNotificationsOfSameKind > 0
            ?
            (notificationReference + "_" + UUID.randomUUID().toString() + "_" + timestamp)
            : notificationReference + "_" + timestamp;

        final List<IdValue<String>> newNotificationsSent = new ArrayList<>(existingNotificationsSent);

        newNotificationsSent.add(new IdValue<>(qualifiedNotificationReference, notificationId));

        return newNotificationsSent;
    }

    private List<IdValue<String>> appendAll(List<IdValue<String>> existingNotificationSent,
                                            List<String> notificationIds,
                                            String referenceId) {
        List<IdValue<String>> updatedNotificationsSent = new ArrayList<>(existingNotificationSent);
        for (String notificationId : notificationIds) {
            updatedNotificationsSent = append(
                updatedNotificationsSent,
                referenceId,
                notificationId
            );
        }
        return updatedNotificationsSent;
    }
}
