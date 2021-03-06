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

import com.mmnaseri.couteau.basics.api.Filter;
import com.mmnaseri.couteau.basics.api.impl.CachingDataDispenser;
import com.mmnaseri.dragonfly.data.DataAccess;
import com.mmnaseri.dragonfly.entity.EntityInitializationContext;
import com.mmnaseri.dragonfly.error.ContextLockFailureError;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This is an entity initialization context that provides the basics of an entity initialization
 * context with the ability to be locked and unlocked in a nested fashion. It also uses a cache
 * to hold entity instances, using type-key duos as the cache keys.
 *
 * @author Milad Naseri (mmnaseri@programmer.net)
 * @since 1.0 (2013/9/22, 13:04)
 */
public abstract class AbstractLockingEntityInitializationContext extends CachingDataDispenser<AbstractLockingEntityInitializationContext.EntityInstanceDescriptor, Object> implements EntityInitializationContext {

    /**
     * This class represents the keys to the cache
     */
    public static class EntityInstanceDescriptor {

        private final Class<?> entityType;
        private final Serializable key;

        public EntityInstanceDescriptor(Class<?> entityType, Serializable key) {
            this.entityType = entityType;
            this.key = key;
        }

        public Class<?> getEntityType() {
            return entityType;
        }

        public Serializable getKey() {
            return key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EntityInstanceDescriptor that = (EntityInstanceDescriptor) o;
            return entityType.equals(that.entityType) && key.equals(that.key);

        }

        @Override
        public int hashCode() {
            int result = entityType.hashCode();
            result = 31 * result + key.hashCode();
            return result;
        }
    }

    private final DataAccess dataAccess;
    private final EntityInitializationContext parent;
    private final Object lock = new Object();
    private final Map<EntityInstanceDescriptor, Set<EntityInstanceDescriptor>> associations = new ConcurrentHashMap<EntityInstanceDescriptor, Set<EntityInstanceDescriptor>>();
    private Integer lockIndex = 0;

    public AbstractLockingEntityInitializationContext(DataAccess dataAccess, EntityInitializationContext parent) {
        this.dataAccess = dataAccess;
        this.parent = parent;
    }

    @Override
    protected Object produce(EntityInstanceDescriptor key) {
        if (parent != null) {
            return parent.get(key.getEntityType(), key.getKey());
        }
        return dataAccess.find(key.getEntityType(), key.getKey());
    }

    @Override
    public <E> void delete(Class<E> entityType, Serializable key) {
        synchronized (lock) {
            //no one is allowed to steal from the context while it is locked ;-)
            //this is to protect the context from being invalidated by accident
            //while it is being populated through the data access interface
            if (lockIndex > 0) {
                return;
            }
        }
        final EntityInstanceDescriptor descriptor = new EntityInstanceDescriptor(entityType, key);
        if (parent != null && !contains(descriptor)) {
            parent.delete(entityType, key);
            return;
        }
        if (associations.containsKey(descriptor)) {
            final Set<EntityInstanceDescriptor> associatedItems = associations.get(descriptor);
            for (EntityInstanceDescriptor associatedItem : associatedItems) {
                disassociate(entityType, key, associatedItem.getEntityType(), associatedItem.getKey());
                delete(associatedItem.getEntityType(), associatedItem.getKey());
            }
        }
        remove(descriptor);
    }

    @Override
    public <E> void delete(final Class<E> entityType) {
        if (parent != null) {
            parent.delete(entityType);
            return;
        }
        remove(new Filter<EntityInstanceDescriptor>() {
            @Override
            public boolean accepts(EntityInstanceDescriptor entityInstanceDescriptor) {
                return entityInstanceDescriptor.getEntityType().equals(entityType);
            }
        });
    }

    @Override
    public <E> void register(Class<E> entityType, Serializable key, E entity) {
        if (parent != null) {
            parent.register(entityType, key, entity);
            return;
        }
        write(new EntityInstanceDescriptor(entityType, key), entity);
    }

    @Override
    public <E> E get(Class<E> entityType, Serializable key) {
        final Object value = read(new EntityInstanceDescriptor(entityType, key));
        return value == null ? null : entityType.cast(value);
    }

    @Override
    public <E> E get(Class<E> entityType, Serializable key, Class<?> requestingEntityType, Serializable requesterKey) {
        if (requestingEntityType != null && requesterKey != null && contains(requestingEntityType, requesterKey)) {
            associate(entityType, key, requestingEntityType, requesterKey);
        }
        final Object value = read(new EntityInstanceDescriptor(entityType, key));
        return value == null ? null : entityType.cast(value);
    }

    protected synchronized void associate(Class<?> firstEntity, Serializable firstKey, Class<?> secondEntity, Serializable secondKey) {
        if (parent != null && parent instanceof AbstractLockingEntityInitializationContext) {
            ((AbstractLockingEntityInitializationContext) parent).associate(firstEntity, firstKey, secondEntity, secondKey);
            return;
        }
        final EntityInstanceDescriptor firstDescriptor = new EntityInstanceDescriptor(firstEntity, firstKey);
        final EntityInstanceDescriptor secondDescriptor = new EntityInstanceDescriptor(secondEntity, secondKey);
        if (firstDescriptor.equals(secondDescriptor)) {
            return;
        }
        final Set<EntityInstanceDescriptor> firstAssociations = associations.containsKey(firstDescriptor) ? associations.get(firstDescriptor) : new CopyOnWriteArraySet<EntityInstanceDescriptor>();
        final Set<EntityInstanceDescriptor> secondAssociations = associations.containsKey(secondDescriptor) ? associations.get(secondDescriptor) : new CopyOnWriteArraySet<EntityInstanceDescriptor>();
        firstAssociations.add(secondDescriptor);
        secondAssociations.add(firstDescriptor);
        associations.put(firstDescriptor, firstAssociations);
        associations.put(secondDescriptor, secondAssociations);
    }

    protected synchronized void disassociate(Class<?> firstEntity, Serializable firstKey, Class<?> secondEntity, Serializable secondKey) {
        if (parent != null && parent instanceof AbstractLockingEntityInitializationContext) {
            ((AbstractLockingEntityInitializationContext) parent).disassociate(firstEntity, firstKey, secondEntity, secondKey);
            return;
        }
        final EntityInstanceDescriptor firstDescriptor = new EntityInstanceDescriptor(firstEntity, firstKey);
        final EntityInstanceDescriptor secondDescriptor = new EntityInstanceDescriptor(secondEntity, secondKey);
        if (firstDescriptor.equals(secondDescriptor)) {
            return;
        }
        final Set<EntityInstanceDescriptor> firstAssociations = associations.containsKey(firstDescriptor) ? associations.get(firstDescriptor) : new CopyOnWriteArraySet<EntityInstanceDescriptor>();
        final Set<EntityInstanceDescriptor> secondAssociations = associations.containsKey(secondDescriptor) ? associations.get(secondDescriptor) : new CopyOnWriteArraySet<EntityInstanceDescriptor>();
        firstAssociations.remove(secondDescriptor);
        secondAssociations.remove(firstDescriptor);
        associations.put(firstDescriptor, firstAssociations);
        associations.put(secondDescriptor, secondAssociations);
        if (firstAssociations.isEmpty()) {
            associations.remove(firstDescriptor);
        }
        if (secondAssociations.isEmpty()) {
            associations.remove(secondDescriptor);
        }
    }

    @Override
    public void lock() {
        synchronized (lock) {
            lockIndex++;
        }
    }

    @Override
    public void unlock() {
        synchronized (lock) {
            lockIndex--;
            if (lockIndex < 0) {
                throw new ContextLockFailureError();
            }
        }
    }

    @Override
    public <E> boolean contains(Class<E> entityType, Serializable key) {
        return contains(new EntityInstanceDescriptor(entityType, key)) || (parent != null && parent.contains(entityType, key));
    }

    @Override
    public DataAccess getDataAccess() {
        return dataAccess;
    }

}
