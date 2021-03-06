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

package com.mmnaseri.dragonfly.ext.impl.parser;

import com.mmnaseri.couteau.basics.api.Filter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import static com.mmnaseri.couteau.reflection.util.ReflectionUtils.withMethods;

/**
 * @author Milad Naseri (mmnaseri@programmer.net)
 * @since 1.0 (2013/10/8, 19:58)
 */
public class HavingMethodFilter implements Filter<Class<?>> {

    private final List<Filter<Class<?>>> annotations;
    private final Filter<Class<?>> returnType;
    private final String methodName;
    private final List<Filter<Class<?>>> parameters;

    public HavingMethodFilter(List<Filter<Class<?>>> annotations, Filter<Class<?>> returnType, String methodName, List<Filter<Class<?>>> parameters) {
        this.annotations = annotations;
        this.returnType = returnType;
        this.methodName = methodName;
        this.parameters = parameters;
    }

    @Override
    public boolean accepts(Class<?> item) {
        //noinspection unchecked
        return !withMethods(item)
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
                .keep(new Filter<Method>() {
                    @Override
                    public boolean accepts(Method item) {
                        return returnType.accepts(item.getReturnType());
                    }
                })
                .keep(new Filter<Method>() {
                    @Override
                    public boolean accepts(Method item) {
                        return item.getName().matches(methodName);
                    }
                })
                .keep(new Filter<Method>() {
                    @Override
                    public boolean accepts(Method item) {
                        if (parameters == null) {
                            return true;
                        }
                        if (parameters.size() != item.getParameterTypes().length) {
                            return false;
                        }
                        for (int i = 0; i < item.getParameterTypes().length; i++) {
                            final Class<?> parameterType = item.getParameterTypes()[i];
                            if (!parameters.get(i).accepts(parameterType)) {
                                return false;
                            }
                        }
                        return true;
                    }
                })
                .isEmpty();
    }

}
