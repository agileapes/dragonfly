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

package com.mmnaseri.dragonfly.sample.test.impl;

import com.mmnaseri.dragonfly.sample.test.ExpectationContext;
import com.mmnaseri.dragonfly.sample.test.Invocation;
import com.mmnaseri.dragonfly.sample.test.InvocationExpectation;

/**
 * @author Milad Naseri (mmnaseri@programmer.net)
 * @since 1.0 (14/8/30 AD, 15:20)
 */
public class DefaultInvocationExpectation<E extends Invocation> extends DefaultExpectation<E> implements InvocationExpectation<E> {

    private final ExpectationContext context;
    private final boolean negate;
    private final E value;

    public DefaultInvocationExpectation(ExpectationContext context, boolean negate, E value) {
        super(context, negate, value);
        this.context = context;
        this.negate = negate;
        this.value = value;
    }

    @Override
    public InvocationExpectation<E> not() {
        close();
        return new DefaultInvocationExpectation<E>(context, !negate, value);
    }

    @Override
    public void toThrow() {
        toThrow(Throwable.class);
    }

    @Override
    public void toThrow(Class<? extends Throwable> exceptionType) {
        try {
            value.invoke();
        } catch (Throwable e) {
            check(exceptionType.isAssignableFrom(e.getClass()), "%s.isAssignableFrom(%s)", exceptionType.getSimpleName(), e.getClass().getSimpleName());
        }
        check(!negate, "throwing");
    }

}
