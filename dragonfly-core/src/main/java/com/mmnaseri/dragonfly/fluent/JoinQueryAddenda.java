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

package com.mmnaseri.dragonfly.fluent;

/**
 * @author Milad Naseri (mmnaseri@programmer.net)
 * @since 1.0 (14/8/10 AD, 10:22)
 */
public interface JoinQueryAddenda<E> extends CriteriaQueryAddenda<E> {

    /**
     * Adds a cross-join target to the SELECT query
     * @param target    the alias for the target entity
     * @param <G>       the type of the entity
     * @return the addenda for expanding the query
     */
    <G> JoinQueryAddenda<E> crossJoin(G target);

    /**
     * Adds a inner join target to the SELECT query
     * @param target    the alias for the target entity
     * @param <G>       the type of the entity
     * @return the addenda for expanding the query
     */
    <G> JoinQueryCondition<E> innerJoin(G target);

    /**
     * Adds a left outer join target to the SELECT query
     * @param target    the alias for the target entity
     * @param <G>       the type of the entity
     * @return the addenda for expanding the query
     */
    <G> JoinQueryCondition<E> leftOuterJoin(G target);

    /**
     * Adds a right outer join target to the SELECT query
     * @param target    the alias for the target entity
     * @param <G>       the type of the entity
     * @return the addenda for expanding the query
     */
    <G> JoinQueryCondition<E> rightOuterJoin(G target);

    /**
     * Adds a full outer join target to the SELECT query
     * @param target    the alias for the target entity
     * @param <G>       the type of the entity
     * @return the addenda for expanding the query
     */
    <G> JoinQueryCondition<E> fullOuterJoin(G target);

}
