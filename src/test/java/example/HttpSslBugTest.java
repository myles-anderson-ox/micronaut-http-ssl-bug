
package example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.File;
import java.util.Map;
import javax.inject.Inject;
import javax.net.ssl.SSLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;

@MicronautTest(transactional = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpSslBugTest implements TestPropertyProvider {

    private static final String TRUSTED_URL = "https://www.google.com/";

    private static final String HTTPS_URL = "https://localhost:6443/";

    /*
     * myapi service trust store is specified in the property provider
     */
    @Inject
    @Client("myapi")
    HttpClient clientWithTrustStore;

    /*
     * no trust store is specified for this service
     */
    @Inject
    @Client("unknown")
    HttpClient clientWithoutTrustStore;

    /**
     * Since we don't trust the self signed cert of HTTPS_URL then
     * we should get a RuntimeException with a cause of SSLException
     */
    @Test
    void testWithTrustStoreFails() {
        final BlockingHttpClient blocking = clientWithTrustStore.toBlocking();
        Exception exception = assertThrows(RuntimeException.class, () -> {
            blocking.retrieve(HTTPS_URL);
        });

        assertEquals(SSLException.class, exception.getCause().getClass());
    }

    /**
     * Since we trust the certificate of TRUSTED_URL then we should get an OK response back
     */
    @Test
    void testWithTrustStoreGoodCAWorks() {
        assertEquals(HttpStatus.OK, clientWithTrustStore.toBlocking().retrieve(TRUSTED_URL, HttpStatus.class));
    }

    /**
     * Since we don't trust the self signed cert of HTTPS_URL then
     * we should get a RuntimeException with a cause of SSLException
     * 
     * This is not the case though and in fact we accept the untrusted cert.
     */
    @Test
    void testWithoutTrustStoreFails() {
        final BlockingHttpClient blocking = clientWithoutTrustStore.toBlocking();
        Exception exception = assertThrows(RuntimeException.class, () -> {
            blocking.retrieve(HTTPS_URL);
        }, "A self signed cert should be rejected by default, but it was not");

        assertEquals(SSLException.class, exception.getCause().getClass());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> getProperties() {
        return CollectionUtils.mapOf(
            "micronaut.ssl.enabled", "true",
            "micronaut.ssl.buildSelfSigned", "true",
            "micronaut.ssl.port", "6443",
            "micronaut.http.services.myapi.ssl.enabled", "true",
            "micronaut.http.services.myapi.ssl.trust-store.path", "file:" + System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar),
            "micronaut.http.services.myapi.ssl.trust-store.type", "JKS",
            "micronaut.http.services.myapi.ssl.trust-store.password", "changeit");
    }

}
