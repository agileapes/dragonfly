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

package com.agileapes.dragonfly.error;

import com.agileapes.dragonfly.data.impl.Reference;

/**
 * This error indicates that the parameter passed to the procedure must have been an instance of
 * {@link Reference} while it was not
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/10, 2:14)
 */
public class ReferenceParameterExpectedError extends DatabaseError {

    public ReferenceParameterExpectedError(Class<?> entityType, String procedureName, int parameterIndex) {
        super("Parameter " + parameterIndex + " of " + entityType.getCanonicalName() + "." + procedureName + " must be of type " + Reference.class.getCanonicalName());
    }

}
