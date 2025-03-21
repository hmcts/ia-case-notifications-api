package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class NotificationIdAppenderTest {

    private final NotificationIdAppender notificationIdAppender = new NotificationIdAppender();

    private final IdValue<String> existingNotification1 = new IdValue<>("foo", "111-222");
    private final IdValue<String> existingNotification2 = new IdValue<>("bar", "333-444");

    private final List<IdValue<String>> existingNotificationsSent =
        Arrays.asList(
            existingNotification1,
            existingNotification2
        );

    @Test
    public void should_append_first_notification_without_qualifier() {
        final String timestampRegex = "([0-9]{13})";

        List<IdValue<String>> actualNotificationsSent =
            notificationIdAppender.append(
                existingNotificationsSent,
                "something",
                "555-666"
            );

        assertEquals(3, actualNotificationsSent.size());
        assertEquals(existingNotification1, actualNotificationsSent.get(0));
        assertEquals(existingNotification2, actualNotificationsSent.get(1));

        assertThat(actualNotificationsSent.get(2).getId()).startsWith("something_");
        assertThat(actualNotificationsSent.get(2).getId()
                .substring("something_".length()))
                .matches(timestampRegex);
        assertEquals("555-666", actualNotificationsSent.get(2).getValue());
    }

    @Test
    public void should_append_subsequent_notifications_with_qualifiers() {

        final String uuidRegex = "([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}_[0-9]{13})";

        List<IdValue<String>> actualNotificationsSent1 =
            notificationIdAppender.append(
                existingNotificationsSent,
                "foo",
                "555-666"
            );

        List<IdValue<String>> actualNotificationsSent2 =
            notificationIdAppender.append(
                actualNotificationsSent1,
                "foo",
                "777-888"
            );

        assertEquals(4, actualNotificationsSent2.size());
        assertEquals(existingNotification1, actualNotificationsSent2.get(0));
        assertEquals(existingNotification2, actualNotificationsSent2.get(1));

        final IdValue<String> actualNotificationsSent3 = actualNotificationsSent2.get(2);
        final IdValue<String> actualNotificationsSent4 = actualNotificationsSent2.get(3);

        assertThat(actualNotificationsSent3.getId()).startsWith("foo_");
        assertThat(actualNotificationsSent3.getId()
            .substring("foo_".length()))
            .matches(uuidRegex);
        assertEquals("555-666", actualNotificationsSent3.getValue());

        assertThat(actualNotificationsSent4.getId()).startsWith("foo_");
        assertThat(actualNotificationsSent4.getId()
            .substring("foo_".length()))
            .matches(uuidRegex);
        assertEquals("777-888", actualNotificationsSent4.getValue());
    }
}
