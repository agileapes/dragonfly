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

package com.mmnaseri.dragonfly.entity.impl;

import com.mmnaseri.couteau.context.impl.OrderedBeanComparator;
import com.mmnaseri.dragonfly.entity.EntityDefinition;
import com.mmnaseri.dragonfly.entity.EntityDefinitionContext;
import com.mmnaseri.dragonfly.entity.EntityDefinitionInterceptor;
import com.mmnaseri.dragonfly.error.DuplicateEntityDefinitionError;
import com.mmnaseri.dragonfly.error.NoSuchEntityError;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.mmnaseri.couteau.basics.collections.CollectionWrapper.with;

/**
 * This is the default entity definition context used by the application. You have the
 * ability to automatically update the context by recalling all post-processors in the
 * event of new interceptors being added.
 *
 * @author Milad Naseri (mmnaseri@programmer.net)
 * @since 1.0 (2013/9/17, 14:22)
 */
public class DefaultEntityDefinitionContext implements EntityDefinitionContext {

    private final boolean updateOnChange;
    private final Map<Class<?>, EntityDefinition<?>> definitions = new ConcurrentHashMap<Class<?>, EntityDefinition<?>>();
    private final Map<Class<?>, EntityDefinition<?>> originalDefinitions = new ConcurrentHashMap<Class<?>, EntityDefinition<?>>();
    private List<EntityDefinitionInterceptor> interceptors = new CopyOnWriteArrayList<EntityDefinitionInterceptor>();

    public DefaultEntityDefinitionContext() {
        this(true);
    }

    public DefaultEntityDefinitionContext(boolean updateOnChange) {
        this.updateOnChange = updateOnChange;
    }

    @Override
    public synchronized void addInterceptor(EntityDefinitionInterceptor interceptor) {
        interceptors = with(interceptors).add(interceptor).sort(new OrderedBeanComparator()).concurrentList();
        if (updateOnChange) {
            definitions.clear();
            for (EntityDefinition<?> definition : originalDefinitions.values()) {
                internalAddEntityDefinition(definition);
            }
        }
    }

    @Override
    public void addDefinition(EntityDefinition<?> entityDefinition) {
        internalAddEntityDefinition(entityDefinition);
        if (updateOnChange) {
            originalDefinitions.put(entityDefinition.getEntityType(), entityDefinition);
        }
    }

    private void internalAddEntityDefinition(EntityDefinition<?> entityDefinition) {
        if (definitions.containsKey(entityDefinition.getEntityType())) {
            throw new DuplicateEntityDefinitionError(entityDefinition.getEntityType());
        }
        EntityDefinition<?> definition = entityDefinition;
        for (EntityDefinitionInterceptor interceptor : interceptors) {
            definition = interceptor.intercept(definition);
        }
        definitions.put(definition.getEntityType(), definition);
    }

    @Override
    public <E> EntityDefinition<E> getDefinition(Class<E> entityType) {
        if (!definitions.containsKey(entityType)) {
            throw new NoSuchEntityError(entityType);
        }
        //noinspection unchecked
        return (EntityDefinition<E>) definitions.get(entityType);
    }

    @Override
    public Collection<EntityDefinition<?>> getDefinitions() {
        return definitions.values();
    }

    @Override
    public Collection<Class<?>> getEntities() {
        return definitions.keySet();
    }

}
