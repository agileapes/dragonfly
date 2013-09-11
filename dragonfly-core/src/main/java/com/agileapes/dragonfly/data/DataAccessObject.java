package com.agileapes.dragonfly.data;

import com.agileapes.dragonfly.metadata.ReferenceMetadata;
import com.agileapes.dragonfly.metadata.TableMetadata;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * <p>This interface allows for a domain-driven view at entities. All entities in this
 * framework can be cast to the relevant type of {@link DataAccessObject} to enable
 * the user to perform the most basic tasks of working with a DAO without having to use
 * the more complex {@link DataAccess} interface's functionalities.</p>
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/5, 19:25)
 */
public interface DataAccessObject<E, K extends Serializable> {

    /**
     * Refreshes the entity from the database, changing the values of its properties to
     * reflect the most up-to-date values in the database.
     */
    void refresh();

    /**
     * Saves the entity to database. This is done either by inserting a new entry in the
     * database, or by updating an existing one.
     * @see DataAccess#save(Object)
     */
    void save();

    /**
     * Deletes the given entity from database. After calling this method, the DAO cannot
     * be used, as it is assumed to have been completely unlinked from the database, thus
     * rendering it useless from a practical point of view.
     * @see DataAccess#delete(Object)
     */
    void delete();

    /**
     * Finds all items like this one
     * @return a list of items in the database that resemble the entity wrapped by the data access object at hand
     */
    List<E> findLike();

    /**
     * Runs the query specified, while using this item as a sample for value injection
     * @param queryName    the name of the query
     * @return the list of matching items
     */
    List<E> query(String queryName);

    /**
     * Runs the update query specified, while using this item as a sample for value injection
     * @param queryName    the name of the query
     * @return the number of affected items
     */
    int update(String queryName);

    /**
     * Reads the value of the key identifying this data access object
     * @return the value of the key
     */
    K accessKey();

    /**
     * Changes the value of the key property of this data access object
     * @param key    the new value of the key
     */
    void changeKey(K key);

    /**
     * Determines whether or not this object has a key property
     * @return {@code true} in case a primary key is defined for this object
     */
    boolean hasKey();

    /**
     * Returns the qualified name of the entity this object is representing
     * @return qualified name
     */
    String getQualifiedName();

    /**
     * Returns the underlying metadata for the table that was used to describe this
     * data access object in the database.
     * @return the table metadata
     */
    TableMetadata<E> getTableMetadata();

    /**
     * Determines whether or not the keys for the underlying entity are automatically
     * generated by a third party, or whether they have to come from the user itself.
     * @return {@code false} means that the user is in charge of specifying unique values
     * for the keys.
     */
    boolean isKeyAutoGenerated();

    Collection<?> loadOneToMany(ReferenceMetadata<E, ?> referenceMetadata);
}
