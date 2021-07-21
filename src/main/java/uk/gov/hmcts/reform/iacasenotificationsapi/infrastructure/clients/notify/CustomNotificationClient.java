package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.notify;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import uk.gov.service.notify.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;


public class CustomNotificationClient implements NotificationClientApi {

    private static final Logger LOGGER = Logger.getLogger(NotificationClient.class.toString());
    private static final String LIVE_BASE_URL = "https://api.notifications.service.gov.uk";

    private final String apiKey;
    private final String serviceId;
    private final String baseUrl;
    private final Proxy proxy;
    private final String version;

    /**
     * This client constructor is used for testing on other environments, used by the GOV.UK Notify team.
     *
     * @param apiKey  Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     * @param baseUrl base URL, defaults to https://api.notifications.service.gov.uk
     */
    public CustomNotificationClient(final String apiKey, final String baseUrl) {
        this(
            apiKey,
            baseUrl,
            null
        );
    }


    /**
     * @param apiKey  Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     * @param baseUrl base URL, defaults to https://api.notifications.service.gov.uk
     * @param proxy   Proxy used on the http requests
     */
    public CustomNotificationClient(final String apiKey, final String baseUrl, final Proxy proxy) {
        this(
            apiKey,
            baseUrl,
            proxy,
            null
        );
        try {
            setDefaultSSLContext();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    public CustomNotificationClient(final String apiKey,
                                    final String baseUrl,
                                    final Proxy proxy,
                                    final SSLContext sslContext) {

        this.apiKey = extractApiKey(apiKey);
        this.serviceId = extractServiceId(apiKey);
        this.baseUrl = baseUrl;
        this.proxy = proxy;
        if (sslContext != null) {
            setCustomSSLContext(sslContext);
        }
        this.version = getVersion();
    }

    public String getUserAgent() {
        return "NOTIFY-API-JAVA-CLIENT/" + version;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public SendEmailResponse sendEmail(String templateId,
                                       String emailAddress,
                                       Map<String, ?> personalisation,
                                       String reference) throws NotificationClientException {
        return sendEmail(templateId, emailAddress, personalisation, reference, "");
    }

    @Override
    public SendEmailResponse sendEmail(String templateId,
                                       String emailAddress,
                                       Map<String, ?> personalisation,
                                       String reference,
                                       String emailReplyToId) throws NotificationClientException {

        JSONObject body = createBodyForPostRequest(templateId,
            null,
            emailAddress,
            personalisation,
            reference,
            null,
            null);

        if (emailReplyToId != null && !emailReplyToId.isEmpty()) {
            body.put("email_reply_to_id", emailReplyToId);
        }

        HttpURLConnection conn = createConnectionAndSetHeaders(baseUrl + "/v2/notifications/email", "POST");
        String response = performPostRequest(conn, body, HttpsURLConnection.HTTP_CREATED);
        return new SendEmailResponse(response);
    }

    public SendSmsResponse sendSms(String templateId, String phoneNumber, Map<String, ?> personalisation, String reference) throws NotificationClientException {
        return sendSms(templateId, phoneNumber, personalisation, reference, "");
    }

    public SendSmsResponse sendSms(String templateId,
                                   String phoneNumber,
                                   Map<String, ?> personalisation,
                                   String reference,
                                   String smsSenderId) throws NotificationClientException {

        JSONObject body = createBodyForPostRequest(templateId,
            phoneNumber,
            null,
            personalisation,
            reference,
            null,
            null);

        if (smsSenderId != null && !smsSenderId.isEmpty()) {
            body.put("sms_sender_id", smsSenderId);
        }
        HttpURLConnection conn = createConnectionAndSetHeaders(baseUrl + "/v2/notifications/sms", "POST");
        String response = performPostRequest(conn, body, HttpsURLConnection.HTTP_CREATED);
        return new SendSmsResponse(response);
    }

    public Notification getNotificationById(String notificationId) throws NotificationClientException {
        String url = baseUrl + "/v2/notifications/" + notificationId;
        HttpURLConnection conn = createConnectionAndSetHeaders(url, "GET");
        String response = performGetRequest(conn);
        return new Notification(response);

    }

    private String performPostRequest(HttpURLConnection conn, JSONObject body, int expectedStatusCode) throws NotificationClientException {
        try {
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), UTF_8);
            wr.write(body.toString());
            wr.flush();

            int httpResult = conn.getResponseCode();
            if (httpResult == expectedStatusCode) {
                return readStream(conn.getInputStream());
            } else {
                throw new NotificationClientException(httpResult, readStream(conn.getErrorStream()));
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String performGetRequest(HttpURLConnection conn) throws NotificationClientException {
        try {
            int httpResult = conn.getResponseCode();
            StringBuilder stringBuilder;
            if (httpResult == 200) {
                return readStream(conn.getInputStream());
            } else {
                throw new NotificationClientException(httpResult, readStream(conn.getErrorStream()));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private HttpURLConnection createConnectionAndSetHeaders(String urlString, String method) throws NotificationClientException {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = getConnection(url);
            conn.setRequestMethod(method);
            Authentication authentication = new Authentication();
            String token = authentication.create(serviceId, apiKey);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("User-agent", getUserAgent());
            if (method.equals("POST")) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

            }
            return conn;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        }
    }

    HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection conn;

        if (null != proxy) {
            conn = (HttpURLConnection) url.openConnection(proxy);
        } else {
            conn = (HttpURLConnection) url.openConnection();
        }
        conn.setReadTimeout(5000);
        conn.setConnectTimeout(5000);

        return conn;
    }

    private JSONObject createBodyForPostRequest(final String templateId,
                                                final String phoneNumber,
                                                final String emailAddress,
                                                final Map<String, ?> personalisation,
                                                final String reference,
                                                final String encodedFileData,
                                                final String postage) {
        JSONObject body = new JSONObject();

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            body.put("phone_number", phoneNumber);
        }

        if (emailAddress != null && !emailAddress.isEmpty()) {
            body.put("email_address", emailAddress);
        }

        if (templateId != null && !templateId.isEmpty()) {
            body.put("template_id", templateId);
        }

        if (personalisation != null && !personalisation.isEmpty()) {
            body.put("personalisation", personalisation);
        }

        if (reference != null && !reference.isEmpty()) {
            body.put("reference", reference);
        }

        if (encodedFileData != null && !encodedFileData.isEmpty()) {
            body.put("content", encodedFileData);
        }
        if (postage != null && !postage.isEmpty()) {
            body.put("postage", postage);
        }
        return body;
    }

    private String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        return IOUtils.toString(inputStream, UTF_8);
    }

    /**
     * Set default SSL context for HTTPS connections.
     * <p/>
     * This is necessary when client has to use keystore
     * (eg provide certification for client authentication).
     * <p/>
     * Use case: enterprise proxy requiring HTTPS client authentication
     */
    private static void setDefaultSSLContext() throws NoSuchAlgorithmException {
        HttpsURLConnection.setDefaultSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
    }

    private static void setCustomSSLContext(final SSLContext sslContext) {
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }


    private static String extractServiceId(String apiKey) {
        return apiKey.substring(Math.max(0, apiKey.length() - 73), Math.max(0, apiKey.length() - 37));
    }

    private static String extractApiKey(String apiKey) {
        return apiKey.substring(Math.max(0, apiKey.length() - 36));
    }


    private String getVersion() {
        InputStream input = null;
        Properties prop = new Properties();
        try {
            input = getClass().getClassLoader().getResourceAsStream("application.properties");

            prop.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return prop.getProperty("project.version");
    }

}
