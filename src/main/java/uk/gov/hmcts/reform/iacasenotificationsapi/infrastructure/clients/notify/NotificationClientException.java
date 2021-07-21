package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.notify;

public class NotificationClientException extends Exception {
    private static final long serialVersionUID = 2L;
    private int httpResult;

    public NotificationClientException(Exception ex) {
        super(ex);
    }

    public NotificationClientException(String message) {
        super(message);
        this.httpResult = 400;
    }

    public NotificationClientException(String message, Throwable cause) {
        super(message, cause);
    }

    NotificationClientException(int httpResult, String message) {
        super("Status code: " + httpResult + " " + message);
        this.httpResult = httpResult;
    }

    public int getHttpResult() {
        return this.httpResult;
    }
}
