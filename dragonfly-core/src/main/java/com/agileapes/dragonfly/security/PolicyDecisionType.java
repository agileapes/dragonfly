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

package com.agileapes.dragonfly.security;

/**
 * Allows for definition of various types of policies for any given action
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/9, 16:54)
 */
public enum PolicyDecisionType {

    /**
     * Access should be allowed
     */
    ALLOW,
    /**
     * Access must be denied
     */
    DENY,
    /**
     * The policy does not state a clear action to be taken
     */
    UNDECIDED

}