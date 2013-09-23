package com.agileapes.dragonfly.entity;

import com.agileapes.dragonfly.data.DataAccess;
import com.agileapes.dragonfly.metadata.ReferenceMetadata;

import java.io.Serializable;
import java.util.Map;

/**
 * This interface encapsulates all the information about an entity that can be known
 * beforehand.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/14, 4:49)
 */
public interface EntityHandler<E> {

    /**
     * @return the type of the supported entity
     */
    Class<E> getEntityType();

    /**
     * Populates a map from the given entity's properties such that all properties set
     * on the entity are reflected back in the map. Here, the map's keys will be property
     * names.
     * @param entity    the entity to create the map from
     * @return the populated map
     */
    Map<String, Object> toMap(E entity);

    /**
     * Populates the properties of a given entity based on the provided map. Keep in mind
     * that here, the map's keys are column names.
     * @param entity    the entity to be populated
     * @param map       the map to read from
     * @return populated entity (same instance as the input)
     */
    E fromMap(E entity, Map<String, Object> map);

    /**
     * Returns the current value set for the key property on the given entity, or throws
     * an exception if no key has been defined.
     * @param entity    the entity to read the key from
     * @return the value of the key
     * @throws com.agileapes.dragonfly.error.NoPrimaryKeyDefinedError
     */
    Serializable getKey(E entity);

    /**
     * Changes the value of the key property for the entity, or throws an exception if no
     * key has been defined.
     * @param entity    the entity to write the value to
     * @param key       the new value for the key
     * @throws com.agileapes.dragonfly.error.NoPrimaryKeyDefinedError
     */
    void setKey(E entity, Serializable key);

    /**
     * Determines whether or not a key property has been defined for the entity
     * @return {@code true} means there is a key property
     */
    boolean hasKey();

    /**
     * Determines whether or not the key for the entity is auto-generated or not or throws
     * an exception in case there are no key columns
     * @return {@code true} means that the database system will generate values for the
     * key property
     * @throws com.agileapes.dragonfly.error.NoPrimaryKeyDefinedError
     */
    boolean isKeyAutoGenerated();

    /**
     * Copies the properties of the first entity into the second entity
     * @param original    the entity to read from
     * @param copy        the entity to write to
     */
    void copy(E original, E copy);

    /**
     * @return the name of the key property for the entity
     * @throws com.agileapes.dragonfly.error.NoPrimaryKeyDefinedError
     */
    String getKeyProperty();

    /**
     * Loads all eager relations into the entity
     * @param entity                   the entity to be loaded
     * @param values                   the values of the actual data retrieval
     * @param initializationContext    the initialization context for the entity
     */
    void loadEagerRelations(E entity, Map<String, Object> values, EntityInitializationContext initializationContext);

    void loadLazyRelations(E entity, ReferenceMetadata<E, ?> referenceMetadata, DataAccess dataAccess);

    /**
     * Deletes all objects on the deletion of which the correct deletion of
     * the entity relies
     * @param entity        the entity that is to be deleted
     * @param dataAccess    the data access for the deletion
     */
    void deleteDependencyRelations(E entity, DataAccess dataAccess);

    /**
     * Deletes all objects that are references by the given entity
     * @param entity        the entity that is to be deleted
     * @param dataAccess    the data access for the deletion
     */
    void deleteDependentRelations(E entity, DataAccess dataAccess);

    /**
     * Saves all relations to which references exist in the given entity
     * @param entity        the entity that is to be saved
     * @param dataAccess    the data access for the save
     */
    void saveDependencyRelations(E entity, DataAccess dataAccess);

    /**
     * Saves all relations which refer to the entity
     * @param entity        the entity that is to be saved
     * @param dataAccess    the data access for the save
     */
    void saveDependentRelations(E entity, DataAccess dataAccess);

}
