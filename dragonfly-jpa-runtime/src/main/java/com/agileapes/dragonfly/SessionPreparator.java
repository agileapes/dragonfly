package com.agileapes.dragonfly;

import com.agileapes.couteau.basics.api.Transformer;
import com.agileapes.couteau.basics.api.impl.CastingTransformer;
import com.agileapes.couteau.reflection.util.ClassUtils;
import com.agileapes.dragonfly.annotations.Extension;
import com.agileapes.dragonfly.dialect.DatabaseDialect;
import com.agileapes.dragonfly.entity.EntityDefinition;
import com.agileapes.dragonfly.entity.EntityDefinitionContext;
import com.agileapes.dragonfly.entity.ModifiableEntityContext;
import com.agileapes.dragonfly.entity.impl.DefaultEntityDefinitionContext;
import com.agileapes.dragonfly.entity.impl.ImmutableEntityDefinition;
import com.agileapes.dragonfly.ext.ExtensionManager;
import com.agileapes.dragonfly.ext.impl.DefaultExtensionExpressionParser;
import com.agileapes.dragonfly.ext.impl.DefaultExtensionManager;
import com.agileapes.dragonfly.metadata.MetadataRegistry;
import com.agileapes.dragonfly.metadata.MetadataResolveStrategy;
import com.agileapes.dragonfly.metadata.MetadataResolverContext;
import com.agileapes.dragonfly.metadata.TableMetadataInterceptor;
import com.agileapes.dragonfly.metadata.impl.AnnotationMetadataResolver;
import com.agileapes.dragonfly.metadata.impl.DefaultMetadataResolverContext;
import com.agileapes.dragonfly.statement.impl.StatementRegistry;
import com.agileapes.dragonfly.statement.impl.StatementRegistryPreparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import javax.persistence.Entity;
import java.util.*;

import static com.agileapes.couteau.basics.collections.CollectionWrapper.with;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/18, 17:56)
 */
public class SessionPreparator implements BeanFactoryPostProcessor, Disposable {

    private final static Log log = LogFactory.getLog(SessionPreparator.class);
    
    private final ExtensionManager extensionManager;
    private final MetadataResolverContext resolverContext;
    private final EntityDefinitionContext definitionContext;
    private final String[] basePackages;

    public SessionPreparator(String basePackages) {
        this(basePackages.split("\\."));
    }

    public SessionPreparator(Properties basePackages) {
        this(with(basePackages.keySet()).transform(new CastingTransformer<Object, String>(String.class)).list());
    }

    public SessionPreparator(Collection<String> basePackages) {
        this(basePackages.toArray(new String[basePackages.size()]));
    }

    public SessionPreparator(String[] basePackages) {
        this.basePackages = basePackages;
        extensionManager = new DefaultExtensionManager(new DefaultExtensionExpressionParser(), new AnnotationMetadataResolver());
        resolverContext = new DefaultMetadataResolverContext(MetadataResolveStrategy.UNAMBIGUOUS, Arrays.<TableMetadataInterceptor>asList(extensionManager));
        definitionContext = new DefaultEntityDefinitionContext();
        definitionContext.addInterceptor(extensionManager);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory context) throws BeansException {
        long time = System.nanoTime();
        log.warn("Preparing the session at runtime can be harmful to your performance. You should consider switching to " +
                "the Maven plugin.");
        log.debug("Looking up the necessary components ...");
        final DatabaseDialect databaseDialect = context.getBean(DatabaseDialect.class);
        final MetadataRegistry metadataRegistry = context.getBean(MetadataRegistry.class);
        final StatementRegistry statementRegistry = context.getBean(StatementRegistry.class);
        final ModifiableEntityContext entityContext = context.getBean(ModifiableEntityContext.class);
        final ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
        log.info("Finding entity classes ...");
        log.debug("Looking for classes with @Entity");
        componentProvider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        for (String basePackage : basePackages) {
            final Set<BeanDefinition> beanDefinitions = componentProvider.findCandidateComponents(basePackage);
            for (BeanDefinition beanDefinition : beanDefinitions) {
                try {
                    log.debug("Registering entity " + beanDefinition.getBeanClassName());
                    //noinspection unchecked
                    definitionContext.addDefinition(new ImmutableEntityDefinition<Object>(ClassUtils.forName(beanDefinition.getBeanClassName(), ClassLoader.getSystemClassLoader()), Collections.<Class<?>, Class<?>>emptyMap()));
                } catch (ClassNotFoundException e) {
                    throw new FatalBeanException("Failed to retrieve class: " + beanDefinition.getBeanClassName(), e);
                }
            }
        }
        componentProvider.resetFilters(false);
        log.info("Finding extensions to the data access ...");
        log.debug("Looking for classes with @Extension");
        componentProvider.addIncludeFilter(new AnnotationTypeFilter(Extension.class));
        for (String basePackage : basePackages) {
            final Set<BeanDefinition> beanDefinitions = componentProvider.findCandidateComponents(basePackage);
            for (BeanDefinition beanDefinition : beanDefinitions) {
                try {
                    log.debug("Registering extension " + beanDefinition.getBeanClassName());
                    extensionManager.addExtension(ClassUtils.forName(beanDefinition.getBeanClassName(), ClassLoader.getSystemClassLoader()));
                } catch (ClassNotFoundException e) {
                    throw new FatalBeanException("Failed to retrieve class: " + beanDefinition.getBeanClassName(), e);
                }
            }
        }
        resolverContext.addMetadataResolver(new AnnotationMetadataResolver());
        log.info("Preparing entity statements for later use");
        final StatementRegistryPreparator preparator = new StatementRegistryPreparator(databaseDialect, resolverContext, metadataRegistry);
        for (Class<?> entity : definitionContext.getEntities()) {
            preparator.addEntity(entity);
        }
        preparator.prepare(statementRegistry);
        log.info("Registering interfaces with the context");
        entityContext.setInterfaces(with(definitionContext.getEntities()).map(new Transformer<Class<?>, Map<Class<?>, Class<?>>>() {
            @Override
            public Map<Class<?>, Class<?>> map(Class<?> input) {
                //noinspection unchecked
                final EntityDefinition<Object> definition = extensionManager.intercept(new ImmutableEntityDefinition<Object>((Class<Object>) input, Collections.<Class<?>, Class<?>>emptyMap()));
                return definition.getInterfaces();
            }
        }));
        time = System.nanoTime() - time;
        log.info("Session preparation took " + Math.round((double) time / 1000000d) / 1000d + " second(s)");
    }
}
