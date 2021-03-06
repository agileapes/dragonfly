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

import com.mmnaseri.couteau.context.error.RegistryException;
import com.mmnaseri.dragonfly.entity.*;
import com.mmnaseri.dragonfly.metadata.ColumnMetadata;
import com.mmnaseri.dragonfly.metadata.TableMetadata;
import com.mmnaseri.dragonfly.metadata.TableMetadataRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the entity handler context used throughout the application
 *
 * @author Milad Naseri (mmnaseri@programmer.net)
 * @since 1.0 (2013/9/14, 5:38)
 */
public class DefaultEntityHandlerContext implements EntityHandlerContext {

    private static final Log log = LogFactory.getLog(EntityHandlerContext.class);
    private final Map<Class<?>, EntityMapCreator> mapCreators;
    private final Map<Class<?>, MapEntityCreator> entityCreators;
    private final EntityMapCreator defaultMapCreator;
    private final MapEntityCreator defaultEntityCreator;
    private final EntityContext entityContext;
    private final Map<Class<?>, EntityHandler<?>> entityHandlers;
    private final TableMetadataRegistry tableMetadataRegistry;

    public DefaultEntityHandlerContext(EntityContext entityContext, TableMetadataRegistry tableMetadataRegistry) {
        this.entityContext = entityContext;
        this.tableMetadataRegistry = tableMetadataRegistry;
        if (entityContext instanceof DefaultEntityContext) {
            final DefaultEntityContext context = (DefaultEntityContext) entityContext;
            context.setHandlerContext(this);
        }
        try {
            defaultEntityCreator = new DefaultMapEntityCreator();
        } catch (RegistryException e) {
            throw new RuntimeException(e);
        }
        defaultMapCreator = new DefaultEntityMapCreator();
        entityCreators = new ConcurrentHashMap<Class<?>, MapEntityCreator>();
        mapCreators = new ConcurrentHashMap<Class<?>, EntityMapCreator>();
        entityHandlers = new ConcurrentHashMap<Class<?>, EntityHandler<?>>();
        log.info("Started entity handler context ...");
    }

    @Override
    public void addHandler(EntityHandler<?> entityHandler) {
        //noinspection unchecked
        entityCreators.put(entityHandler.getEntityType(), new DelegatingEntityCreator((EntityHandler<Object>) entityHandler));
        //noinspection unchecked
        mapCreators.put(entityHandler.getEntityType(), new DelegatingMapCreator((EntityHandler<Object>) entityHandler));
        entityHandlers.put(entityHandler.getEntityType(), entityHandler);
    }

    public EntityMapCreator getMapCreator(Class<?> entityType) {
        if (mapCreators.containsKey(entityType)) {
            return mapCreators.get(entityType);
        }
        return defaultMapCreator;
    }

    public MapEntityCreator getEntityCreator(Class<?> entityType) {
        if (entityCreators.containsKey(entityType)) {
            return entityCreators.get(entityType);
        }
        return defaultEntityCreator;
    }

    @Override
    public <E> Map<String, Object> toMap(TableMetadata<E> tableMetadata, E entity) {
        return getMapCreator(tableMetadata.getEntityType()).toMap(tableMetadata, entity);
    }

    @Override
    public <E> Map<String, Object> toMap(Collection<ColumnMetadata> columns, E entity) {
        return getMapCreator(entity.getClass()).toMap(columns, entity);
    }

    @Override
    public <E> E fromMap(E entity, Collection<ColumnMetadata> columns, Map<String, Object> values) {
        return getEntityCreator(entity.getClass()).fromMap(entity, columns, values);
    }
    
    @Override
    public <E> EntityHandler<E> getHandler(Class<E> entityType) {
        if (entityHandlers.containsKey(entityType)) {
            //noinspection unchecked
            return (EntityHandler<E>) entityHandlers.get(entityType);
        }
        for (Class<?> registeredType : entityHandlers.keySet()) {
            if (registeredType.isAssignableFrom(entityType)) {
                //noinspection unchecked
                return (EntityHandler<E>) entityHandlers.get(registeredType);
            }
        }
        final GenericEntityHandler<E> entityHandler = new GenericEntityHandler<E>(entityType, entityContext, tableMetadataRegistry.getTableMetadata(entityType));
        entityHandlers.put(entityType, entityHandler);
        return entityHandler;
    }

    @Override
    public <E> EntityHandler<E> getHandler(E entity) {
        //noinspection unchecked
        return (EntityHandler<E>) getHandler(entity.getClass());
    }

}
