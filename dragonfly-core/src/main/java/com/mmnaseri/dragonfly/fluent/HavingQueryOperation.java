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
 * @since 1.0 (14/8/9 AD, 16:52)
 */
public interface HavingQueryOperation<E> {

    /**
     * Expands the HAVING phrase with a {@code =} operator
     * @param value    the property perform the comparison on
     * @param <G>      the type of the property
     * @return the addenda for expanding the query
     */
    <G> HavingQueryExpander<E> beEqualTo(G value);

    /**
     * Expands the HAVING phrase with a {@code !=} operator
     * @param value    the property perform the comparison on
     * @param <G>      the type of the property
     * @return the addenda for expanding the query
     */
    <G> HavingQueryExpander<E> notBeEqualTo(G value);

    /**
     * Expands the HAVING phrase with a {@code <} operator
     * @param value    the property perform the comparison on
     * @param <G>      the type of the property
     * @return the addenda for expanding the query
     */
    <G> HavingQueryExpander<E> beLessThan(G value);

    /**
     * Expands the HAVING phrase with a {@code >} operator
     * @param value    the property perform the comparison on
     * @param <G>      the type of the property
     * @return the addenda for expanding the query
     */
    <G> HavingQueryExpander<E> beGreaterThan(G value);

    /**
     * Expands the HAVING phrase with a {@code <=} operator
     * @param value    the property perform the comparison on
     * @param <G>      the type of the property
     * @return the addenda for expanding the query
     */
    <G> HavingQueryExpander<E> beLessThanOrEqualTo(G value);

    /**
     * Expands the HAVING phrase with a {@code >=} operator
     * @param value    the property perform the comparison on
     * @param <G>      the type of the property
     * @return the addenda for expanding the query
     */
    <G> HavingQueryExpander<E> beGreaterThanOrEqualTo(G value);

}
