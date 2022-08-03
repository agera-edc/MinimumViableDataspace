package org.eclipse.dataspaceconnector.mvd;

import com.github.javafaker.Faker;
import org.eclipse.dataspaceconnector.iam.did.spi.document.DidDocument;
import org.eclipse.dataspaceconnector.iam.did.spi.document.Service;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.dataspaceconnector.registration.client.models.Participant;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FederatedCacheNodeResolverTest {

    FederatedCacheNodeResolver resolver;
    DidResolverRegistry didResolver = mock(DidResolverRegistry.class);
    static final Faker FAKER =  Faker.instance();
    String did = "did:web:" + FAKER.internet().domainName();

    @Test
    void getNode_success() {
        var idsUrl = FAKER.internet().url();
        when(didResolver.resolve(did)).thenReturn(Result.success(getDidDocument(List.of(new Service("#idsUrl", FederatedCacheNodeResolver.IDS_URL, idsUrl)))));

        resolver =  new FederatedCacheNodeResolver(didResolver, new ConsoleMonitor());

        var node = resolver.toFederatedCacheNode(new Participant().did(did));

        assertThat(node.getName()).isEqualTo(did);
        assertThat(node.getTargetUrl()).isEqualTo(idsUrl);
        assertThat(node.getSupportedProtocols()).containsExactly("ids-multipart");
    }

    @Test
    void getNode_failure() {
        when(didResolver.resolve(did)).thenReturn(Result.failure("failure"));

        resolver =  new FederatedCacheNodeResolver(didResolver, new ConsoleMonitor());

        assertThatThrownBy(() -> resolver.toFederatedCacheNode(new Participant().did(did))).isInstanceOf(EdcException.class);
    }

    @NotNull
    private DidDocument getDidDocument(List<Service> services) {
        return DidDocument.Builder.newInstance().id(did).service(services).build();
    }

    @Test
    void getNode_noIdsUrl() {
        when(didResolver.resolve(did)).thenReturn(Result.success(getDidDocument(List.of())));

        resolver =  new FederatedCacheNodeResolver(didResolver, new ConsoleMonitor());

        assertThatThrownBy(() -> resolver.toFederatedCacheNode(new Participant().did(did))).isInstanceOf(EdcException.class);
    }

}