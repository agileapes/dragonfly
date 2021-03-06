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

package com.mmnaseri.dragonfly.runtime.analysis.analyzers;

import com.mmnaseri.couteau.basics.api.Filter;
import com.mmnaseri.couteau.basics.api.Transformer;
import com.mmnaseri.couteau.basics.api.impl.MirrorFilter;
import com.mmnaseri.couteau.basics.api.impl.NullFilter;
import com.mmnaseri.couteau.reflection.util.assets.AnnotatedElementFilter;
import com.mmnaseri.couteau.reflection.util.assets.GetterMethodFilter;
import com.mmnaseri.dragonfly.entity.EntityDefinition;
import com.mmnaseri.dragonfly.entity.EntityDefinitionContext;
import com.mmnaseri.dragonfly.ext.ExtensionManager;
import com.mmnaseri.dragonfly.ext.ExtensionMetadata;
import com.mmnaseri.dragonfly.runtime.analysis.ApplicationDesignAnalyzer;
import com.mmnaseri.dragonfly.runtime.analysis.DesignIssue;
import com.mmnaseri.dragonfly.runtime.analysis.IssueTarget;
import com.mmnaseri.dragonfly.runtime.analysis.impl.ComplexDesignIssueTarget;
import com.mmnaseri.dragonfly.runtime.analysis.impl.EntityIssueTarget;
import com.mmnaseri.dragonfly.runtime.analysis.impl.ExtensionIssueTarget;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

import static com.mmnaseri.couteau.basics.collections.CollectionWrapper.with;
import static com.mmnaseri.couteau.reflection.util.ReflectionUtils.withMethods;

/**
 * @author Milad Naseri (mmnaseri@programmer.net)
 * @since 1.0 (14/3/17 AD, 2:08)
 */
public class IdentityCollisionAnalyzer implements ApplicationDesignAnalyzer {

    private final ExtensionManager extensionManager;
    private final EntityDefinitionContext definitionContext;

    public IdentityCollisionAnalyzer(ExtensionManager extensionManager, EntityDefinitionContext definitionContext) {
        this.extensionManager = extensionManager;
        this.definitionContext = definitionContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DesignIssue> analyze() {
        return with(definitionContext.getDefinitions())
                .transform(new Transformer<EntityDefinition<?>, Class<?>>() {
                    @Override
                    public Class<?> map(EntityDefinition<?> entityDefinition) {
                        return entityDefinition.getEntityType();
                    }
                })
        .keep(new Filter<Class<?>>() {
            @Override
            public boolean accepts(Class<?> entityType) {
                return !withMethods(entityType).keep(new GetterMethodFilter()).keep(new AnnotatedElementFilter(Id.class)).isEmpty();
            }
        })
        .transform(new Transformer<Class<?>, ComplexDesignIssueTarget>() {
            @Override
            public ComplexDesignIssueTarget map(Class<?> entityType) {
                final List<IssueTarget<?>> extensionsTryingToIntroduceId = with(extensionManager.getRegisteredExtensions())
                        .keep(new MirrorFilter<Class<?>>(entityType))
                        .keep(new Filter<ExtensionMetadata>() {
                            @Override
                            public boolean accepts(ExtensionMetadata extensionMetadata) {
                                return !withMethods(extensionMetadata.getExtension()).keep(new GetterMethodFilter()).keep(new AnnotatedElementFilter(Id.class)).isEmpty();
                            }
                        })
                        .transform(new Transformer<ExtensionMetadata, IssueTarget<?>>() {
                            @Override
                            public IssueTarget<?> map(ExtensionMetadata extensionMetadata) {
                                return new ExtensionIssueTarget(extensionMetadata);
                            }
                        }).list();
                if (extensionsTryingToIntroduceId.isEmpty()) {
                    return null;
                }
                final ArrayList<IssueTarget<?>> involvedParties = new ArrayList<IssueTarget<?>>(extensionsTryingToIntroduceId);
                involvedParties.add(0, new EntityIssueTarget(entityType));
                return new ComplexDesignIssueTarget(involvedParties);
            }
        })
        .drop(new NullFilter<ComplexDesignIssueTarget>())
        .transform(new Transformer<ComplexDesignIssueTarget, DesignIssue>() {
            @Override
            public DesignIssue map(ComplexDesignIssueTarget complexDesignIssueTarget) {
                return new DesignIssue(DesignIssue.Severity.SEVERE, complexDesignIssueTarget, "Multiple sources exist " +
                        "for entity ID for the given target", "Try to externalize the ID column for the entity if it " +
                        "defines its own ID column, and also take a closer look at your extensions to make sure they " +
                        "do not apply the same sort of semantic modification to your entity, rendering them useless.");
            }
        })
        .list();
    }

}
