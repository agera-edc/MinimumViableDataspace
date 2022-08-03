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
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNode;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.eclipse.dataspaceconnector.registration.client.models.Participant;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class RegistrationServiceNodeDirectoryTest {

    static Faker faker = new Faker();

    private final RegistryApi registryApi = Mockito.mock(RegistryApi.class);
    private final FederatedCacheNodeResolver resolver = Mockito.mock(FederatedCacheNodeResolver.class);

    @Test
    void getAll_emptyList() {
        var service = new RegistrationServiceNodeDirectory(registryApi, resolver);

        when(registryApi.listParticipants()).thenReturn(List.of());

        List<FederatedCacheNode> cacheNodes = service.getAll();
        assertThat(cacheNodes).isEmpty();
    }

    @Test
    void getAll() {
        var service = new RegistrationServiceNodeDirectory(registryApi, resolver);

        var company1 = getParticipant();
        var company2 = getParticipant();
        FederatedCacheNode node1 = node();
        FederatedCacheNode node2 = node();
        when(registryApi.listParticipants()).thenReturn(List.of(company1, company2));
        when(resolver.toFederatedCacheNode(company1)).thenReturn(node1);
        when(resolver.toFederatedCacheNode(company2)).thenReturn(node2);

        List<FederatedCacheNode> cacheNodes = service.getAll();
        assertThat(cacheNodes)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(node1, node2);
    }

    private FederatedCacheNode node() {
        return new FederatedCacheNode(faker.lorem().word(), faker.internet().url(), List.of("ids-multipart"));
    }

    @NotNull
    private Participant getParticipant() {
        var participant = new Participant();
        participant.setDid(faker.internet().domainName());
        return participant;
    }
}