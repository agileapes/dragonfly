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

package com.mmnaseri.dragonfly.runtime.repo.impl.strategies;

import com.mmnaseri.couteau.reflection.util.ReflectionUtils;
import com.mmnaseri.couteau.reflection.util.assets.MemberNameFilter;
import com.mmnaseri.couteau.reflection.util.assets.MethodArgumentsFilter;
import com.mmnaseri.couteau.reflection.util.assets.MethodReturnTypeFilter;
import com.mmnaseri.dragonfly.runtime.repo.MethodInterceptionStrategy;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author Milad Naseri (mmnaseri@programmer.net)
 * @since 1.0 (14/8/21 AD, 11:47)
 */
public class InterfaceMethodInterceptionStrategy implements MethodInterceptionStrategy {

    private final Class superClass;
    private final Object instance;

    public InterfaceMethodInterceptionStrategy(Class superClass, Object instance) {
        this.superClass = superClass;
        this.instance = instance;
    }

    @Override
    public boolean accepts(Method method) {
        return !ReflectionUtils.withMethods(superClass)
                .keep(new MemberNameFilter(method.getName()))
                .keep(new MethodReturnTypeFilter(method.getReturnType()))
                .keep(new MethodArgumentsFilter(method.getParameterTypes())).isEmpty();
    }

    @Override
    public Object intercept(Object target, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {
        final Method declaration = ReflectionUtils.withMethods(superClass)
                .keep(new MemberNameFilter(method.getName()))
                .keep(new MethodReturnTypeFilter(method.getReturnType()))
                .keep(new MethodArgumentsFilter(method.getParameterTypes()))
                .first();
        return declaration.invoke(instance, arguments);
    }
}
