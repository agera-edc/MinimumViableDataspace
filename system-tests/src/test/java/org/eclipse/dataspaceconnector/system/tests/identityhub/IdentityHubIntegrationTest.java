package org.eclipse.dataspaceconnector.system.tests.identityhub;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.eclipse.dataspaceconnector.identityhub.client.IdentityHubClientImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.system.tests.utils.TestUtils.requiredPropOrEnv;

public class IdentityHubIntegrationTest {

    static final String PROVIDER_IDENTITY_HUB_URL = requiredPropOrEnv("PROVIDER_IDENTITY_HUB_URL", "http://localhost:8182/api/identity-hub");
    static final String CONSUMER_EU_IDENTITY_HUB_URL = requiredPropOrEnv("CONSUMER_EU_IDENTITY_HUB_URL", "http://localhost:8182/api/identity-hub");
    static final String CONSUMER_US_IDENTITY_HUB_URL = requiredPropOrEnv("CONSUMER_US_IDENTITY_HUB_URL", "http://localhost:8183/api/identity-hub");

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .build();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private IdentityHubClientImpl client;

    @BeforeEach
    void setUp() {
        client = new IdentityHubClientImpl(OK_HTTP_CLIENT, OBJECT_MAPPER);
    }

    @ParameterizedTest
    @MethodSource("provideHubUrls")
    void retrieveVerifiableCredentials_empty(String hubUrl) throws IOException {
        var vcs = client.getVerifiableCredentials(hubUrl);

        assertThat(vcs.succeeded()).isTrue();
        assertThat(vcs.getContent()).isEmpty();
    }

    private static Stream<Arguments> provideHubUrls() {
        return Stream.of(
                Arguments.of(PROVIDER_IDENTITY_HUB_URL),
                Arguments.of(CONSUMER_EU_IDENTITY_HUB_URL),
                Arguments.of(CONSUMER_US_IDENTITY_HUB_URL)
        );
    }
}
