package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.security.AccessTokenProvider;

@ExtendWith(MockitoExtension.class)
public class DocumentDownloadClientTest {

    private final String someAccessToken = "some-access-token";
    private final String someServiceAuthToken = "some-service-auth-token";

    @Mock
    private DocumentDownloadClientApi documentDownloadClientApi;
    @Mock private AccessTokenProvider accessTokenProvider;
    @Mock private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock private UserDetailsProvider userDetailsProvider;
    @Mock private UserDetails userDetails;
    @Mock private ResponseEntity<Resource> responseEntity;
    @Mock private Resource downloadedResource;

    private DocumentDownloadClient documentDownloadClient;
    private final String someWellFormattedDocumentBinaryDownloadUrl = "http://host:8080/a/b/c";
    private final String someUserRolesString = "some-role,some-other-role";
    private final String someUserId = "some-user-id";

    @BeforeEach
    public void setUp() {
        documentDownloadClient = new DocumentDownloadClient(
            documentDownloadClientApi,
            serviceAuthTokenGenerator,
            accessTokenProvider,
            userDetailsProvider);
    }

    @Test
    public void downloads_resource() {

        when(documentDownloadClientApi.downloadBinary(
                someAccessToken,
                someServiceAuthToken,
                someUserRolesString,
                someUserId,
                "a/b/c")).thenReturn(responseEntity);

        when(responseEntity.getBody())
                .thenReturn(downloadedResource);

        when(accessTokenProvider.getAccessToken())
                .thenReturn(someAccessToken);

        when(serviceAuthTokenGenerator.generate())
                .thenReturn(someServiceAuthToken);

        when(userDetailsProvider.getUserDetails())
                .thenReturn(userDetails);

        when(userDetails.getRoles())
                .thenReturn(List.of(someUserRolesString));

        when(userDetails.getId())
                .thenReturn(someUserId);

        Resource resource = documentDownloadClient.download(someWellFormattedDocumentBinaryDownloadUrl);

        verify(documentDownloadClientApi, times(1))
            .downloadBinary(
                eq(someAccessToken),
                eq(someServiceAuthToken),
                eq(someUserRolesString),
                eq(someUserId),
                eq("a/b/c")
            );

        assertEquals(resource, downloadedResource);
    }

    @Test
    public void throws_if_document_binary_url_bad() {

        IllegalArgumentException exception =
assertThrows(IllegalArgumentException.class, () -> documentDownloadClient.download("bad-url"))
            ;
assertEquals("Invalid url for DocumentDownloadClientApi", exception.getMessage());

        verifyNoInteractions(documentDownloadClientApi);
        verifyNoInteractions(serviceAuthTokenGenerator);
        verifyNoInteractions(accessTokenProvider);
    }

    @Test
    public void throws_if_document_api_returns_empty_body() {

        when(documentDownloadClientApi.downloadBinary(
                someAccessToken,
                someServiceAuthToken,
                someUserRolesString,
                someUserId,
                "a/b/c")).thenReturn(responseEntity);

        when(responseEntity.getBody())
                .thenReturn(downloadedResource);

        when(accessTokenProvider.getAccessToken())
                .thenReturn(someAccessToken);

        when(serviceAuthTokenGenerator.generate())
                .thenReturn(someServiceAuthToken);

        when(userDetailsProvider.getUserDetails())
                .thenReturn(userDetails);

        when(userDetails.getRoles())
                .thenReturn(List.of(someUserRolesString));

        when(userDetails.getId())
                .thenReturn(someUserId);

        when(responseEntity.getBody())
            .thenReturn(null);

        IllegalStateException exception =
assertThrows(IllegalStateException.class, () -> documentDownloadClient.download(someWellFormattedDocumentBinaryDownloadUrl))
            ;
assertEquals("Document could not be downloaded", exception.getMessage());
    }

}
