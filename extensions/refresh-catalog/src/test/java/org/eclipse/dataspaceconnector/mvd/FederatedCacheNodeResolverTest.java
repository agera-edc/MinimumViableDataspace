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
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FederatedCacheNodeResolverTest {

    public static final String IDS_MESSAGING = "IDSMessaging";

    FederatedCacheNodeResolver resolver;
    DidResolverRegistry didResolver = mock(DidResolverRegistry.class);
    static final Faker FAKER =  Faker.instance();
    static String did = "did:web:" + FAKER.internet().domainName();
    static String idsUrl = FAKER.internet().url();
    private Monitor monitor = mock(Monitor.class);

    @ParameterizedTest
    @MethodSource("argumentsStreamSuccess")
    void getNode_success(List<Service> services) {
        when(didResolver.resolve(did)).thenReturn(Result.success(getDidDocument(services)));

        resolver =  new FederatedCacheNodeResolver(didResolver, monitor);

        var result = resolver.toFederatedCacheNode(new Participant().did(did));

        assertThat(result.succeeded()).isTrue();
        var node = result.getContent();
        assertThat(node.getName()).isEqualTo(did);
        assertThat(node.getTargetUrl()).isEqualTo(idsUrl);
        assertThat(node.getSupportedProtocols()).containsExactly("ids-multipart");
    }

    @ParameterizedTest
    @MethodSource("argumentsStreamFailure")
    void getNode_failure(Result result) {
        when(didResolver.resolve(did)).thenReturn(result);

        resolver =  new FederatedCacheNodeResolver(didResolver, monitor);

        var nodeResult = resolver.toFederatedCacheNode(new Participant().did(did));

        assertThat(nodeResult.failed()).isTrue();
    }

    @NotNull
    private static DidDocument getDidDocument(List<Service> services) {
        return DidDocument.Builder.newInstance().id(did).service(services).build();
    }

    private static Stream<Arguments> argumentsStreamSuccess() {
        return Stream.of(
                arguments(List.of(new Service(FAKER.lorem().word(), IDS_MESSAGING, idsUrl))),
                arguments(List.of(new Service(FAKER.lorem().word(), IDS_MESSAGING, idsUrl), new Service(FAKER.lorem().word(), FAKER.lorem().word(), FAKER.internet().url())))
        );
    }

    private static Stream<Arguments> argumentsStreamFailure() {
        return Stream.of(
                arguments(Result.failure("failure")),
                arguments(Result.success(getDidDocument(List.of())))
        );
    }

}