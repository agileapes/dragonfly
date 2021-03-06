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

package com.mmnaseri.dragonfly.metadata;

import com.mmnaseri.couteau.basics.api.Processor;

import java.util.Collection;

/**
 * This interface exposes functionality that will allow for easy registration and from
 * of table metadata, using entity types as keys. This interface works on the general
 * assumption that each entity type will correspond to exactly one table metadata.
 *
 * @author Milad Naseri (mmnaseri@programmer.net)
 * @since 1.0 (2013/8/30, 14:35)
 */
public interface TableMetadataRegistry {

    /**
     * @return the types of entities for which some metadata is available through this
     * interface
     */
    Collection<Class<?>> getEntityTypes();

    /**
     * @return all table metadata registered with this registry
     */
    Collection<TableMetadata<?>> getTables();

    /**
     * Looks up table metadata for a given entity type. Failure to locate proper table
     * metadata might result in an error being thrown
     * @param entityType    the type of the entity
     * @param <E>           the type parameter for the entity
     * @return the table metadata
     */
    <E> TableMetadata<E> getTableMetadata(Class<E> entityType);

    /**
     * Adds new metadata to the registry
     * @param tableMetadata    the table metadata being added
     * @param <E>              the type parameter for the entity which is being registered
     */
    <E> void addTableMetadata(TableMetadata<E> tableMetadata);

    /**
     * Determines whether or not metadata is available for the given entity type
     * @param entityType    the type of the entity
     * @return {@code true} means the entity is known within the registry
     */
    boolean contains(Class<?> entityType);

    /**
     * Adds a callback which will be called whenever metadata within the registry
     * is changed
     * @param registryProcessor    the processor that will modify or monitor the registry
     *                             and the changes made
     */
    void setChangeCallback(Processor<TableMetadataRegistry> registryProcessor);

}
