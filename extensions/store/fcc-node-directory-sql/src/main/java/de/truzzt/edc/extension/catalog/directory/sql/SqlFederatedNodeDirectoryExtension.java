/*
 *  Copyright (c) 2023 sovity GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       sovity GmbH - initial implementation
 *
 */

package de.truzzt.edc.extension.catalog.directory.sql;

import de.truzzt.edc.extension.catalog.directory.sql.schema.postgres.PostgresDialectStatements;
import org.eclipse.edc.catalog.spi.directory.FederatedCacheNodeDirectory;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;

public class SqlFederatedNodeDirectoryExtension implements ServiceExtension {

    public static final String NAME = "SQL Federated Node Directory Extension";

    @Inject
    private TypeManager typeManager;

    @Inject
    private DataSourceRegistry dataSourceRegistry;
    @Inject
    private TransactionContext transactionContext;

    @Override
    public String name() {
        return NAME;
    }

    @Provider
    public FederatedCacheNodeDirectory sqlNodeDirectory() {
        return new SqlFederatedNodeDirectory(
                dataSourceRegistry,
                DataSourceRegistry.DEFAULT_DATASOURCE,
                transactionContext,
                typeManager.getMapper(),
                new PostgresDialectStatements()
        );
    }
}