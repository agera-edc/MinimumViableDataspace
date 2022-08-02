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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.identityhub.credentials.model.VerifiableCredential;
import org.eclipse.dataspaceconnector.policy.model.Operator;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.policy.AtomicConstraintFunction;
import org.eclipse.dataspaceconnector.spi.policy.PolicyContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.eclipse.dataspaceconnector.identityhub.credentials.VerifiableCredentialsJwtService.VERIFIABLE_CREDENTIALS_KEY;

public class RegionConstraintFunction implements AtomicConstraintFunction<Permission> {
    private static final String REGION_KEY = "region";
    private final ObjectMapper objectMapper;
    private final Monitor monitor;

    public RegionConstraintFunction(ObjectMapper objectMapper, Monitor monitor) {
        this.objectMapper = objectMapper;
        this.monitor = monitor;
    }

    @Override
    public boolean evaluate(Operator operator, Object rightValue, Permission rule, PolicyContext context) {
        var regions = getRegions(context.getParticipantAgent().getClaims());
        switch (operator) {
            case EQ:
                return regions.contains(rightValue);
            case NEQ:
                return !regions.contains(rightValue);
            case IN:
                return !Collections.disjoint((Collection<?>) rightValue, regions);
            default:
                return false;
        }
    }

    private List<String> getRegions(Map<String, Object> claims) {
        List<String> regions = new ArrayList<>();
        var objects = claims.values();
        for (Object o : objects) {
            try {
                var verifiableCredential = getVerifiableCredential(o);
                var region = getRegion(verifiableCredential);
                regions.add(region);
            } catch (ClassCastException | IllegalArgumentException e) {
                monitor.warning("Failed getting region from verifiableCredential", e);
            }
        }
        return regions;
    }

    private VerifiableCredential getVerifiableCredential(Object object) {
        var vcObject = (Map<String, Object>) object;
        var verifiableCredentialMap = vcObject.get(VERIFIABLE_CREDENTIALS_KEY);
        return objectMapper.convertValue(verifiableCredentialMap, VerifiableCredential.class);
    }

    private String getRegion(VerifiableCredential verifiableCredential) {
        var region = verifiableCredential.getCredentialSubject().get(REGION_KEY);
        return (String) region;
    }
}
