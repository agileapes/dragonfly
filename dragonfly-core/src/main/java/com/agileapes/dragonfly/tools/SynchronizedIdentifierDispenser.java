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

package com.agileapes.dragonfly.tools;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates sequential identifiers synchronously
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/8/29, 14:23)
 */
public class SynchronizedIdentifierDispenser<E> {

    private final Map<E, Long> data = new HashMap<E, Long>();

    public synchronized long getIdentifier(E target) {
        final Long value;
        if (data.containsKey(target)) {
            value = data.get(target);
        } else {
            value = 0L;
        }
        data.put(target, value + 1);
        return value;
    }

}
