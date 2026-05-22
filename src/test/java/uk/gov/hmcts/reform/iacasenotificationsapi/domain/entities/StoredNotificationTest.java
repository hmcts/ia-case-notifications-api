package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class StoredNotificationTest {

    private final String notificationId = "someId";
    private final String notificationDateSent = "someDateSent";
    private final String notificationSentTo = "someSentTo";
    private final String notificationMethod = "someMethod";
    private final String notificationStatus = "someStatus";
    private final String notificationBody = "someBody";
    private final String notificationReference = "someReference";
    private final String notificationSubject = "someSubject";
    private final Document document = mock(Document.class);

    private StoredNotification storedNotification;

    @BeforeEach
    public void setUp() {
        storedNotification =
            StoredNotification.builder()
                .notificationId(notificationId)
                .notificationDateSent(notificationDateSent)
                .notificationSentTo(notificationSentTo)
                .notificationBody(notificationBody)
                .notificationMethod(notificationMethod)
                .notificationStatus(notificationStatus)
                .notificationReference(notificationReference)
                .notificationSubject(notificationSubject)
                .build();
    }

    @Test
    void should_hold_onto_values() {
        assertEquals(notificationId, storedNotification.getNotificationId());
        assertEquals(notificationMethod, storedNotification.getNotificationMethod());
        assertEquals(notificationSentTo, storedNotification.getNotificationSentTo());
        assertEquals(notificationStatus, storedNotification.getNotificationStatus());
        assertEquals(notificationBody, storedNotification.getNotificationBody());
        assertEquals(notificationDateSent, storedNotification.getNotificationDateSent());
        assertEquals(notificationReference, storedNotification.getNotificationReference());
        assertEquals(notificationSubject, storedNotification.getNotificationSubject());
        assertNull(storedNotification.getNotificationDocument());
        storedNotification.setNotificationDocument(document);
        assertEquals(document, storedNotification.getNotificationDocument());
    }

    @Test
    void should_not_allow_null_arguments_other_than_document() {

        StoredNotification.StoredNotificationBuilder builder = StoredNotification.builder();
        assertThrows(NullPointerException.class,
            () -> builder.notificationId(null));
        assertThrows(NullPointerException.class,
            () -> builder.notificationDateSent(null));
        assertThrows(NullPointerException.class,
            () -> builder.notificationSentTo(null));
        assertThrows(NullPointerException.class,
            () -> builder.notificationBody(null));
        assertThrows(NullPointerException.class,
            () -> builder.notificationMethod(null));
        assertThrows(NullPointerException.class,
            () -> builder.notificationStatus(null));
        assertThrows(NullPointerException.class,
            () -> builder.notificationReference(null));
        assertThrows(NullPointerException.class,
            () -> builder.notificationSubject(null));
    }
}
