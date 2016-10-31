/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AgileApes, Ltd.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.agileapes.dragonfly.ext.impl;

import com.mmnaseri.couteau.basics.api.Filter;
import com.agileapes.dragonfly.entity.EntityDefinitionInterceptor;
import com.agileapes.dragonfly.ext.ExtensionMetadata;
import com.agileapes.dragonfly.metadata.TableMetadataInterceptor;

/**
 * This is an immutable extension metadata
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/18, 16:46)
 */
public class ImmutableExtensionMetadata implements ExtensionMetadata {

    private final Class<?> extension;
    private final TableMetadataInterceptor tableMetadataInterceptor;
    private final EntityDefinitionInterceptor entityDefinitionInterceptor;
    private final Filter<Class<?>> filter;

    public ImmutableExtensionMetadata(Class<?> extension, TableMetadataInterceptor tableMetadataInterceptor, EntityDefinitionInterceptor entityDefinitionInterceptor, Filter<Class<?>> filter) {
        this.extension = extension;
        this.tableMetadataInterceptor = tableMetadataInterceptor;
        this.entityDefinitionInterceptor = entityDefinitionInterceptor;
        this.filter = filter;
    }

    @Override
    public Class<?> getExtension() {
        return extension;
    }

    @Override
    public TableMetadataInterceptor getTableMetadataInterceptor() {
        return tableMetadataInterceptor;
    }

    @Override
    public EntityDefinitionInterceptor getEntityDefinitionInterceptor() {
        return entityDefinitionInterceptor;
    }

    @Override
    public boolean accepts(Class<?> item) {
        return filter.accepts(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableExtensionMetadata that = (ImmutableExtensionMetadata) o;
        return extension.equals(that.extension);

    }

    @Override
    public int hashCode() {
        return extension.hashCode();
    }
}
