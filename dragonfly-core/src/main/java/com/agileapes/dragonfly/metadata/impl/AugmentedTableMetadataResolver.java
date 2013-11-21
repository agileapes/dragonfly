/*
 * Copyright (c) 2013 AgileApes, Ltd.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall
 * be included in all copies or substantial portions of the
 * Software.
 */

package com.agileapes.dragonfly.metadata.impl;

import com.agileapes.dragonfly.metadata.TableMetadata;
import com.agileapes.dragonfly.metadata.TableMetadataInterceptor;
import com.agileapes.dragonfly.metadata.TableMetadataResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a class that will take table a metadata resolver, and a list of metadata
 * interceptors, and then return the resolved and augmented table metadata for the entity
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/8, 19:37)
 */
public class AugmentedTableMetadataResolver implements TableMetadataResolver {

    private final TableMetadataResolver tableMetadataResolver;
    private final List<TableMetadataInterceptor> interceptors = new ArrayList<TableMetadataInterceptor>();

    public AugmentedTableMetadataResolver(TableMetadataResolver tableMetadataResolver, List<TableMetadataInterceptor> interceptors) {
        this.tableMetadataResolver = tableMetadataResolver;
        this.interceptors.addAll(interceptors);
    }

    @Override
    public <E> TableMetadata<E> resolve(Class<E> entityType) {
        TableMetadata<E> tableMetadata = tableMetadataResolver.resolve(entityType);
        for (TableMetadataInterceptor interceptor : interceptors) {
            tableMetadata = interceptor.intercept(tableMetadata);
        }
        return tableMetadata;
    }

    @Override
    public boolean accepts(Class<?> entityType) {
        return tableMetadataResolver.accepts(entityType);
    }

}