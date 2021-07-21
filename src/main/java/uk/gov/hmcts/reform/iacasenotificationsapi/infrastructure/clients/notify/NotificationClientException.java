package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.notify;

public class NotificationClientException extends Exception {
    private static final long serialVersionUID = 2L;
    private int httpResult;

    public NotificationClientException(Exception ex) {
        super(ex);
    }

    NotificationClientException(int httpResult, String message) {
        super("Status code: " + httpResult + " " + message);
        this.httpResult = httpResult;
    }
}
