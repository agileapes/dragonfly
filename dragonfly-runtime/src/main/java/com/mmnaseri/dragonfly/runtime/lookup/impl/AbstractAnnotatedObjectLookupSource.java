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

package com.mmnaseri.dragonfly.runtime.lookup.impl;

import com.mmnaseri.dragonfly.runtime.lookup.LookupSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

/**
 * @author Milad Naseri (mmnaseri@programmer.net)
 * @since 1.0 (14/8/14 AD, 9:41)
 */
public abstract class AbstractAnnotatedObjectLookupSource extends AbstractClassPathScanningLookupSource {

    private static final Log log = LogFactory.getLog(LookupSource.class);

    protected AbstractAnnotatedObjectLookupSource(ClassLoader classLoader) {
        super(classLoader);
    }

    protected AbstractAnnotatedObjectLookupSource(ClassPathScanningCandidateComponentProvider provider, ClassLoader classLoader) {
        super(provider, classLoader);
    }

    @Override
    protected AbstractTypeHierarchyTraversingFilter getFilter() {
        return new AnnotationTypeFilter(getAnnotation());
    }

    protected abstract Class<? extends Annotation> getAnnotation();

    @Override
    public Class[] getClasses(String... basePackages) {
        final Class<? extends Annotation> annotation = getAnnotation();
        log.info("Scanning for classes annotated with @" + annotation.getSimpleName());
        return super.getClasses(basePackages);
    }
}