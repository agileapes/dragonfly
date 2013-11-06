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

package com.agileapes.dragonfly.metadata;

import com.agileapes.couteau.basics.api.Processor;

import java.util.Collection;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/8/30, 14:35)
 */
public interface MetadataRegistry {

    Collection<Class<?>> getEntityTypes();

    <E> TableMetadata<E> getTableMetadata(Class<E> entityType);

    <E> void addTableMetadata(TableMetadata<E> tableMetadata);

    boolean contains(Class<?> entityType);

    void setChangeCallback(Processor<MetadataRegistry> registryProcessor);

}
