package com.agileapes.dragonfly.error;

import com.agileapes.dragonfly.data.DataOperation;
import com.agileapes.dragonfly.data.impl.op.SampledDataOperation;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/26, 2:57)
 */
public class AmbiguousCallbackError extends DatabaseError {

    public AmbiguousCallbackError(DataOperation operation) {
        super("There are more the one was to do this operation: " + operation);
    }

}