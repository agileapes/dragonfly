package com.agileapes.dragonfly.entity.impl;

import com.agileapes.couteau.context.impl.OrderedBeanComparator;
import com.agileapes.dragonfly.entity.EntityDefinition;
import com.agileapes.dragonfly.entity.EntityDefinitionContext;
import com.agileapes.dragonfly.entity.EntityDefinitionInterceptor;
import com.agileapes.dragonfly.error.NoSuchEntityError;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.agileapes.couteau.basics.collections.CollectionWrapper.with;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/17, 14:22)
 */
@Service("definitionContext")
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