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

package com.mmnaseri.dragonfly.data.impl;

import com.mmnaseri.dragonfly.data.DataCallback;
import com.mmnaseri.dragonfly.data.DataOperation;

/**
 * This is the data callback that will respond to all data operations.
 *
 * @author Milad Naseri (mmnaseri@programmer.net)
 * @since 1.0 (2013/9/26, 3:04)
 */
public abstract class AbstractDefaultDataCallback<E extends DataOperation> implements DataCallback<E> {

    @Override
    public boolean accepts(E e) {
        return true;
    }

}
