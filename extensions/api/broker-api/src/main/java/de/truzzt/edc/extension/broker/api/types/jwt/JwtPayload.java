/*
 *  Copyright (c) 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - Initial implementation
 *       truzzt GmbH - PostgreSQL implementation
 *
 */

package de.truzzt.edc.extension.broker.api.types.jwt;

import com.fasterxml.jackson.annotation.JsonAlias;

import javax.validation.constraints.NotNull;

public class JwtPayload {

    @NotNull
    @JsonAlias({"https://w3id.org/idsa/core/sub", "ids:sub", "sub"})
    private String sub;

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }
}