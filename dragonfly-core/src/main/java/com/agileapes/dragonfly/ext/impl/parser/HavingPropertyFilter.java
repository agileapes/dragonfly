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

package com.agileapes.dragonfly.ext.impl.parser;

import com.agileapes.couteau.basics.api.Filter;
import com.agileapes.couteau.reflection.util.ReflectionUtils;
import com.agileapes.couteau.reflection.util.assets.GetterMethodFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import static com.agileapes.couteau.reflection.util.ReflectionUtils.withMethods;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/10/8, 16:43)
 */
public class HavingPropertyFilter implements Filter<Class<?>> {

    private final List<Filter<Class<?>>> annotations;
    private final Filter<Class<?>> typeFilter;
    private final String propertyName;

    public HavingPropertyFilter(List<Filter<Class<?>>> annotations, Filter<Class<?>> typeFilter, String propertyName) {
        this.annotations = annotations;
        this.typeFilter = typeFilter;
        this.propertyName = propertyName;
    }

    @Override
    public boolean accepts(Class<?> item) {
        //noinspection unchecked
        return !withMethods(item)
                .keep(new GetterMethodFilter())
                .keep(new Filter<Method>() {
                    @Override
                    public boolean accepts(Method item) {
                        final String accessorProperty = ReflectionUtils.getPropertyName(item.getName());
                        return accessorProperty.matches(propertyName);
                    }
                })
                .keep(new Filter<Method>() {
                    @Override
                    public boolean accepts(Method item) {
                        return typeFilter.accepts(item.getReturnType());
                    }
                })
                .keep(new Filter<Method>() {
                    @Override
                    public boolean accepts(Method item) {
                        for (Filter<Class<?>> annotationFilter : annotations) {
                            boolean found = false;
                            for (Annotation annotation : item.getAnnotations()) {
                                if (annotationFilter.accepts(annotation.annotationType())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                return false;
                            }
                        }
                        return true;
                    }
                })
                .isEmpty();
    }

}
