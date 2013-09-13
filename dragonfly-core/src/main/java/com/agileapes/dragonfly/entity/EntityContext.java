package com.agileapes.dragonfly.entity;

import com.agileapes.dragonfly.metadata.TableMetadata;

/**
 * This interface abstracts the concept of an entity context, where entity instances come from.
 * The entity context is supposed to generate instances, manage them, and keep track of them.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/5, 15:11)
 */
public interface EntityContext {

    /**
     * Dispenses an instance of the entity
     * @param entityType    the entity type to be instantiated
     * @param <E>           the type of the entity
     * @return the generated entity instance
     */
    <E> E getInstance(Class<E> entityType);

    /**
     * Dispenses an instance of the entity associated with the given table metadata
     * @param tableMetadata    the table metadata
     * @param <E>              the type of the entity
     * @return the generated entity instance
     */
    <E> E getInstance(TableMetadata<E> tableMetadata);

    /**
     * Determines whether or not the given entity has come from this context.
     * @param entity    te entity to be checked
     * @param <E>       the type of the entity
     * @return {@code true} if {@link #getInstance(Class)} or {@link #getInstance(TableMetadata)}
     * was used to access the instance.
     */
    <E> boolean has(E entity);

}
