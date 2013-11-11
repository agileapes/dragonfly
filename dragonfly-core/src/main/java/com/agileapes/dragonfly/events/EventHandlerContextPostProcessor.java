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

package com.agileapes.dragonfly.events;

/**
 * The event handler context post-processor, as the name suggests, lets you take a fully populated
 * event handler context, and post-process it. As the operations exposed through the context are
 * fairly limited, this generally means the introduction of new handlers.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/10/11, 12:14)
 */
public interface EventHandlerContextPostProcessor {

    /**
     * This method is called by a more general context to let post-processing of the handler
     * context happen
     * @param eventHandlerContext    the handler context being processed
     */
    void postProcessEventHandlerContext(EventHandlerContext eventHandlerContext);

}
