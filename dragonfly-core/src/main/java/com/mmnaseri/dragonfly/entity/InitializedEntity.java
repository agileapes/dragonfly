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

package com.mmnaseri.dragonfly.entity;

import java.util.Map;

/**
 * This interface allows for the initialization of all data access objects with the given
 * initial state
 *
 * @author Milad Naseri (mmnaseri@programmer.net)
 * @since 1.0 (2013/9/5, 16:04)
 */
public interface InitializedEntity<E> {

    /**
     * Initializes the proxied entity
     * @param entityType    the type of the entity
     * @param entity        the actual entity
     * @param token           the context token for the object. This token is expected to be
     *                      the same throughout the entity's lifetime.
     */
    void initialize(Class<E> entityType, E entity, String token);

    /**
     * @return the token given to the entity by the initializing context
     */
    String getToken();

    /**
     * Sets the original copy of the entity
     * @param originalCopy    the original copy. This will be the reference for future updates.
     */
    void setOriginalCopy(E originalCopy);

    /**
     * @return the original copy of the entity, prior to being tampered with throughout the
     * application
     */
    E getOriginalCopy();

    /**
     * Freezes the entity's state. This will prevent auto-loading of lazy-load properties,
     * dirty checking, etc.
     */
    void freeze();

    /**
     * Unfreezes the entity's state, telling the data access object that it should now behave
     * as though the entity is being used for application
     */
    void unfreeze();

    /**
     * Sets the initialization context for the given entity
     * @param initializationContext    the initialization context for the entity
     */
    void setInitializationContext(EntityInitializationContext initializationContext);

    /**
     * Returns the initialization context for the entity
     * @return the context for the entity
     */
    EntityInitializationContext getInitializationContext();

    /**
     * The map of values for the entity as it was last seen in the database for the current instance
     * @param map    the map of values for the entity
     */
    void setMap(Map<String, Object> map);

}
