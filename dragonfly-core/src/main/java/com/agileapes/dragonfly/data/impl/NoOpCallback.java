/*
 * Copyright (c) 2013 AgileApes, Ltd.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall
 * be included in all copies or substantial portions of the
 * Software.
 */

package com.agileapes.dragonfly.data.impl;

import com.agileapes.dragonfly.data.DataCallback;
import com.agileapes.dragonfly.data.DataOperation;

/**
 * This is a callback that will simply let the operation proceed uninterrupted.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/26, 3:32)
 */
public class NoOpCallback implements DataCallback {

    @Override
    public Object execute(DataOperation operation) {
        return operation.proceed();
    }

    @Override
    public boolean accepts(Object o) {
        return true;
    }

}
