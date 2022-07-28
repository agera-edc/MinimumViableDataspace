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
import org.eclipse.dataspaceconnector.policy.model.Duty;
import org.eclipse.dataspaceconnector.policy.model.Operator;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.policy.AtomicConstraintFunction;
import org.eclipse.dataspaceconnector.spi.policy.PolicyContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RegionConstraintFunction implements AtomicConstraintFunction<Duty> {

    private final ObjectMapper objectMapper;
    private final Monitor monitor;

    public RegionConstraintFunction(ObjectMapper objectMapper, Monitor monitor) {
        this.objectMapper = objectMapper;
        this.monitor = monitor;
    }

    @Override
    public boolean evaluate(Operator operator, Object rightValue, Duty rule, PolicyContext context) {
        var regions = getRegions(context.getParticipantAgent().getClaims());
        switch (operator) {
            case EQ:
                return regions.contains(rightValue);
            default:
                return false;
        }
    }

    List<String> getRegions(Map<String, Object> claims) {
        return claims.values()
                .stream().flatMap(x -> getVerifiableCredential(x).stream())
                .flatMap(vc -> getRegion(vc).stream()).collect(Collectors.toList());
    }

    private Optional<VerifiableCredential> getVerifiableCredential(Object object) {
        try {
            var vcObject = (Map<String, Object>) object;
            var verifiableCredentialMap = vcObject.get("vc");
            return Optional.of(objectMapper.convertValue(verifiableCredentialMap, VerifiableCredential.class));
        } catch (Exception e) {
            monitor.warning("Error getting verifiable credentials", e);
            return Optional.empty();
        }
    }

    private Optional<String> getRegion(VerifiableCredential verifiableCredential) {
        try {
            var region = verifiableCredential.getCredentialSubject().get("region");
            return region == null ? Optional.empty() : Optional.of((String) region);
        } catch (Exception e) {
            monitor.warning("Error getting region", e);
            return Optional.empty();
        }

    }
}
