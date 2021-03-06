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

package com.mmnaseri.dragonfly.runtime.config;

import com.mmnaseri.dragonfly.data.DataAccess;
import com.mmnaseri.dragonfly.data.DataAccessSession;
import com.mmnaseri.dragonfly.data.impl.DefaultDataAccessSession;
import com.mmnaseri.dragonfly.data.impl.DelegatingDataAccess;
import com.mmnaseri.dragonfly.data.impl.SecuredDataAccess;
import com.mmnaseri.dragonfly.dialect.DatabaseDialect;
import com.mmnaseri.dragonfly.entity.EntityContext;
import com.mmnaseri.dragonfly.entity.EntityHandlerContext;
import com.mmnaseri.dragonfly.entity.impl.DefaultEntityContext;
import com.mmnaseri.dragonfly.entity.impl.DefaultEntityHandlerContext;
import com.mmnaseri.dragonfly.metadata.TableMetadataContext;
import com.mmnaseri.dragonfly.metadata.TableMetadataRegistry;
import com.mmnaseri.dragonfly.metadata.impl.DefaultTableMetadataContext;
import com.mmnaseri.dragonfly.runtime.assets.BeanDisposer;
import com.mmnaseri.dragonfly.runtime.assets.DataAccessPreparator;
import com.mmnaseri.dragonfly.runtime.ext.monitoring.MonitoredEntityContext;
import com.mmnaseri.dragonfly.runtime.ext.monitoring.impl.DefaultMonitoredEntityContext;
import com.mmnaseri.dragonfly.runtime.ext.monitoring.impl.MonitoredEntityInterceptor;
import com.mmnaseri.dragonfly.runtime.repo.impl.CrudRepositoryContext;
import com.mmnaseri.dragonfly.runtime.repo.impl.NamedQueryOrganizer;
import com.mmnaseri.dragonfly.runtime.session.impl.SessionPostProcessorHandler;
import com.mmnaseri.dragonfly.security.AccessDeniedHandler;
import com.mmnaseri.dragonfly.security.DataSecurityManager;
import com.mmnaseri.dragonfly.security.impl.DefaultDataSecurityManager;
import com.mmnaseri.dragonfly.security.impl.FailFirstAccessDeniedHandler;
import com.mmnaseri.dragonfly.statement.StatementRegistry;
import com.mmnaseri.dragonfly.statement.impl.DefaultStatementRegistry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * @author Milad Naseri (mmnaseri@programmer.net)
 * @since 1.0 (14/8/13 AD, 15:35)
 */
@Configuration
public class RuntimeDataConfiguration implements ApplicationContextAware {

    @Bean
    public BeanDisposer beanDisposer() {
        return new BeanDisposer();
    }

    @Bean
    public DataAccessPreparator dataAccessPreparator() {
        return new DataAccessPreparator();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new FailFirstAccessDeniedHandler();
    }

    @Bean
    public TableMetadataContext tableMetadataContext() {
        return new DefaultTableMetadataContext();
    }

    @Bean
    public StatementRegistry statementRegistry() {
        return new DefaultStatementRegistry();
    }

    @Bean
    public DataSecurityManager dataSecurityManager(AccessDeniedHandler accessDeniedHandler) {
        return new DefaultDataSecurityManager(accessDeniedHandler);
    }

    @Bean
    public DataAccessSession dataAccessSession(DatabaseDialect dialect, StatementRegistry statementRegistry, TableMetadataRegistry tableMetadataRegistry, DataSource dataSource) {
        return new DefaultDataAccessSession(dialect, statementRegistry, tableMetadataRegistry, dataSource);
    }

    @Bean
    public DefaultEntityContext entityContext(DataSecurityManager securityManager, TableMetadataRegistry tableMetadataRegistry, DataAccessSession session) {
        return new DefaultEntityContext(securityManager, tableMetadataRegistry, session);
    }

    @Bean
    public EntityHandlerContext entityHandlerContext(EntityContext entityContext, TableMetadataRegistry tableMetadataRegistry) {
        return new DefaultEntityHandlerContext(entityContext, tableMetadataRegistry);
    }

    private SecuredDataAccess securedDataAccess(DataAccessSession session, DataSecurityManager securityManager, EntityContext entityContext, EntityHandlerContext handlerContext) {
        return new SecuredDataAccess(session, securityManager, entityContext, handlerContext, false);
    }

    @Bean
    public DataAccess dataAccess(DataAccessSession session, DataSecurityManager securityManager, EntityContext entityContext, EntityHandlerContext handlerContext) {
        return new DelegatingDataAccess(securedDataAccess(session, securityManager, entityContext, handlerContext));
    }

    @Bean
    public SessionPostProcessorHandler sessionPostProcessorHandler() {
        return new SessionPostProcessorHandler();
    }

    @Bean
    public CrudRepositoryContext crudRepositoryContext(ApplicationContext context, Environment environment) {
        return new CrudRepositoryContext(context.getClassLoader(), environment.getProperty("crud.basePackages", "com.mmnaseri.dragonfly").trim().split("\\s*,\\s*"));
    }

    @Bean
    public NamedQueryOrganizer namedQueryOrganizer() {
        return new NamedQueryOrganizer();
    }

    @Bean
    public MonitoredEntityContext monitoredEntityContext() {
        return new DefaultMonitoredEntityContext();
    }

    @Bean
    public MonitoredEntityInterceptor monitoredEntityInterceptor() throws InterruptedException {
        return new MonitoredEntityInterceptor();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ((DefaultMonitoredEntityContext) applicationContext.getBean(MonitoredEntityContext.class)).setApplicationContext(applicationContext);
    }
}
