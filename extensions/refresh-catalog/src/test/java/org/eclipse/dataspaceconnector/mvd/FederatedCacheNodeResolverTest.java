/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FederatedCacheNodeResolverTest {

    FederatedCacheNodeResolver resolver;
    DidResolverRegistry didResolver = mock(DidResolverRegistry.class);
    static final Faker FAKER =  Faker.instance();
    String did = "did:web:" + FAKER.internet().domainName();
    static String idsUrl = FAKER.internet().url();

    @ParameterizedTest
    @MethodSource("propertiesSource")
    void getNode_success(List<Service> services) {
        when(didResolver.resolve(did)).thenReturn(Result.success(getDidDocument(services)));

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

    private static Stream<Arguments> propertiesSource() {
        return Stream.of(
                Arguments.of(List.of(new Service("#idsUrl", FederatedCacheNodeResolver.IDS_URL, idsUrl))),
                Arguments.of(List.of(new Service("#idsUrl", FederatedCacheNodeResolver.IDS_URL, idsUrl), new Service(FAKER.lorem().word(), FAKER.lorem().word(), FAKER.internet().url())))
        );
    }

}