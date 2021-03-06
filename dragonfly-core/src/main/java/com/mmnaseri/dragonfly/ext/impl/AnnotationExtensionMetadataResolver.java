/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Milad Naseri.
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

package com.mmnaseri.dragonfly.ext.impl;

import com.mmnaseri.couteau.basics.api.Filter;
import com.mmnaseri.couteau.basics.api.Transformer;
import com.mmnaseri.dragonfly.annotations.Extension;
import com.mmnaseri.dragonfly.entity.EntityDefinition;
import com.mmnaseri.dragonfly.entity.EntityDefinitionInterceptor;
import com.mmnaseri.dragonfly.entity.impl.ImmutableEntityDefinition;
import com.mmnaseri.dragonfly.ext.ExtensionExpressionParser;
import com.mmnaseri.dragonfly.ext.ExtensionMetadata;
import com.mmnaseri.dragonfly.ext.ExtensionMetadataResolver;
import com.mmnaseri.dragonfly.metadata.RelationMetadata;
import com.mmnaseri.dragonfly.metadata.TableMetadata;
import com.mmnaseri.dragonfly.metadata.TableMetadataInterceptor;
import com.mmnaseri.dragonfly.metadata.TableMetadataResolver;
import com.mmnaseri.dragonfly.metadata.impl.DefaultRelationMetadata;
import com.mmnaseri.dragonfly.metadata.impl.ResolvedTableMetadata;
import com.mmnaseri.dragonfly.metadata.impl.TableMetadataCopier;

import java.util.HashMap;

import static com.mmnaseri.couteau.basics.collections.CollectionWrapper.with;

/**
 * This resolver will accept classes marked with {@link Extension}. After that, all interfaces
 * implemented by the class will be added to the definition of the entities targeted by the
 * extension, and all table-related metadata will be resolved through the class the same as the
 * way it was deduced from normal entities, and will afterwards be used to augment the table
 * metadata for all matching entity tables.
 *
 * @author Milad Naseri (mmnaseri@programmer.net)
 * @since 1.0 (2013/11/11, 19:32)
 */
public class AnnotationExtensionMetadataResolver implements ExtensionMetadataResolver<Class<?>> {

    private final ExtensionExpressionParser parser;
    private final TableMetadataResolver tableMetadataResolver;

    public AnnotationExtensionMetadataResolver(TableMetadataResolver tableMetadataResolver) {
        this.parser = new DefaultExtensionExpressionParser();
        this.tableMetadataResolver = tableMetadataResolver;
    }

    @Override
    public ExtensionMetadata resolve(final Class<?> extension) {
        final Extension annotation = extension.getAnnotation(Extension.class);
        final Filter<Class<?>> filter = parser.map(annotation.filter());
        final TableMetadata<?> extendedTableMetadata = tableMetadataResolver.resolve(extension);
        //noinspection unchecked
        final TableMetadataCopier<Object> tableMetadataCopier = new TableMetadataCopier<Object>((TableMetadata<Object>) extendedTableMetadata);
        return new ImmutableExtensionMetadata(extension, new TableMetadataInterceptor() {
            @Override
            public <E> TableMetadata<E> intercept(final TableMetadata<E> tableMetadata) {
                if (extendedTableMetadata.getColumns().isEmpty()) {
                    return tableMetadata;
                }
                final TableMetadata<Object> metadata = tableMetadataCopier.copy();
                return new ResolvedTableMetadata<E>(tableMetadata.getEntityType(), tableMetadata.getSchema(), tableMetadata.getName(),
                        with(tableMetadata.getConstraints()).add(metadata.getConstraints()).list(),
                        with(tableMetadata.getColumns()).add(metadata.getColumns()).list(),
                        with(tableMetadata.getNamedQueries()).add(metadata.getNamedQueries()).list(),
                        with(tableMetadata.getSequences()).add(metadata.getSequences()).list(),
                        with(tableMetadata.getProcedures()).add(metadata.getProcedures()).list(),
                        with(tableMetadata.getForeignReferences()).add(with(metadata.getForeignReferences()).transform(new Transformer<RelationMetadata<Object, ?>, RelationMetadata<E, ?>>() {
                            @Override
                            public RelationMetadata<E, ?> map(RelationMetadata<Object, ?> input) {
                                //noinspection unchecked
                                return new DefaultRelationMetadata<E, Object>(input.getDeclaringClass(), input.getPropertyName(), input.isOwner(), tableMetadata, (TableMetadata<Object>) input.getForeignTable(), input.getForeignColumn(), input.getType(), input.getCascadeMetadata(), input.isLazy(), input.getOrdering());
                            }
                        }).list()).list(), tableMetadata.getVersionColumn(),
                        with(tableMetadata.getOrdering()).add(metadata.getOrdering()).list());
            }
        }, new EntityDefinitionInterceptor() {
            @Override
            public <E> EntityDefinition<E> intercept(EntityDefinition<E> definition) {
                final HashMap<Class<?>, Class<?>> interfaces = new HashMap<Class<?>, Class<?>>();
                interfaces.putAll(definition.getInterfaces());
                for (Class<?> superType : extension.getInterfaces()) {
                    interfaces.put(superType, extension);
                }
                return new ImmutableEntityDefinition<E>(definition.getEntityType(), interfaces);
            }
        }, filter);
    }

    @Override
    public boolean accepts(Class<?> item) {
        return item.isAnnotationPresent(Extension.class);
    }

}
