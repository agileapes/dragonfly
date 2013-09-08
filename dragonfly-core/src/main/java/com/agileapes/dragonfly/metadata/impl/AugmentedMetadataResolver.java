package com.agileapes.dragonfly.metadata.impl;

import com.agileapes.dragonfly.metadata.MetadataResolver;
import com.agileapes.dragonfly.metadata.TableMetadata;
import com.agileapes.dragonfly.metadata.TableMetadataInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/8, 19:37)
 */
public class AugmentedMetadataResolver implements MetadataResolver {

    private final MetadataResolver metadataResolver;
    private final List<TableMetadataInterceptor> interceptors = new ArrayList<TableMetadataInterceptor>();

    public AugmentedMetadataResolver(MetadataResolver metadataResolver, List<TableMetadataInterceptor> interceptors) {
        this.metadataResolver = metadataResolver;
        this.interceptors.addAll(interceptors);
    }

    @Override
    public <E> TableMetadata<E> resolve(Class<E> entityType) {
        TableMetadata<E> tableMetadata = metadataResolver.resolve(entityType);
        for (TableMetadataInterceptor interceptor : interceptors) {
            tableMetadata = interceptor.intercept(tableMetadata);
        }
        return tableMetadata;
    }

    @Override
    public boolean accepts(Class<?> entityType) {
        return metadataResolver.accepts(entityType);
    }

}