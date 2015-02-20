/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AgileApes, Ltd.
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

package com.agileapes.dragonfly.fluent.generation.impl;

import com.agileapes.dragonfly.fluent.generation.BookKeeper;
import com.agileapes.dragonfly.fluent.generation.ComparisonType;
import com.agileapes.dragonfly.fluent.generation.JoinType;
import com.agileapes.dragonfly.fluent.generation.JoinedSelectionSource;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (14/8/10 AD, 12:11)
 */
public class ImmutableJoinedSelectionSource<E, F> implements JoinedSelectionSource<E, F> {

    private final BookKeeper<E> bookKeeper;
    private final JoinType joinType;
    private final F joinSource;
    private final F joinTarget;
    private final ComparisonType comparisonType;

    public ImmutableJoinedSelectionSource(BookKeeper<E> bookKeeper, JoinType joinType, F joinSource, F joinTarget, ComparisonType comparisonType) {
        this.comparisonType = comparisonType;
        this.bookKeeper = bookKeeper;
        this.joinType = joinType;
        this.joinSource = joinSource;
        this.joinTarget = joinTarget;
    }

    @Override
    public JoinType getJoinType() {
        return joinType;
    }

    @Override
    public F getJoinSource() {
        return joinSource;
    }

    @Override
    public F getJoinTarget() {
        return joinTarget;
    }

    @Override
    public ComparisonType getComparisonType() {
        return comparisonType;
    }

    @Override
    public BookKeeper<E> getBookKeeper() {
        return bookKeeper;
    }

}
