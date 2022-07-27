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
 *       Microsoft Corporation - initial implementation
 *
 */

package org.eclipse.dataspaceconnector.mvd;

import org.eclipse.dataspaceconnector.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.dataspaceconnector.iam.did.spi.document.DidDocument;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.result.Result;

import java.util.Map;

/**
 * Mock credentials verifier that simply returns claims parsed from the URL configured for the identity hub.
 */
public class MockCredentialsVerifier implements CredentialsVerifier {
    private final Monitor monitor;

    public MockCredentialsVerifier(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public Result<Map<String, Object>> getVerifiedCredentials(DidDocument didDocument) {
        monitor.debug("Starting (mock) credential verification against didDocument " + didDocument);

        return Result.success(Map.of());
    }
}
