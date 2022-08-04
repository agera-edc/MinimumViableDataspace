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

import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNode;
import org.eclipse.dataspaceconnector.iam.did.spi.document.DidDocument;
import org.eclipse.dataspaceconnector.iam.did.spi.document.Service;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.dataspaceconnector.registration.client.models.Participant;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.result.Result;

import java.util.List;

/**
 * Resolves the FederatedCacheNode from the Participant's did document.
 */
public class FederatedCacheNodeResolver {

    public static final String IDS_URL = "IdsUrl";

    private final DidResolverRegistry resolver;
    private final Monitor monitor;

    public FederatedCacheNodeResolver(DidResolverRegistry resolver, Monitor monitor) {
        this.resolver = resolver;
        this.monitor = monitor;
    }

    public FederatedCacheNode toFederatedCacheNode(Participant participant) {
        Result<DidDocument> didDocument = resolver.resolve(participant.getDid());
        if (didDocument.failed()) {
            monitor.severe(String.join(" | ", didDocument.getFailure().getMessages()));
            throw new EdcException("Can't resolve did document for participant: " + participant.getDid());
        }
        String url = getUrl(didDocument);
        return new FederatedCacheNode(didDocument.getContent().getId(), url, List.of("ids-multipart"));
    }

    private String getUrl(Result<DidDocument> didDocument) {
        return didDocument.getContent()
                .getService().stream().filter(service -> service.getType().equals(IDS_URL)).map(Service::getServiceEndpoint).findFirst().orElseThrow(() -> new EdcException("Ids url not present in the participants did document."));
    }
}
