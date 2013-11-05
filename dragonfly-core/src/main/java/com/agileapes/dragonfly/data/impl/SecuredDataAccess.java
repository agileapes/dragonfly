package com.agileapes.dragonfly.data.impl;

import com.agileapes.couteau.enhancer.api.MethodDescriptor;
import com.agileapes.couteau.enhancer.impl.ImmutableMethodDescriptor;
import com.agileapes.dragonfly.data.DataAccess;
import com.agileapes.dragonfly.data.DataAccessSession;
import com.agileapes.dragonfly.data.PartialDataAccess;
import com.agileapes.dragonfly.entity.EntityContext;
import com.agileapes.dragonfly.entity.EntityHandlerContext;
import com.agileapes.dragonfly.events.DataAccessEventHandler;
import com.agileapes.dragonfly.events.EventHandlerContext;
import com.agileapes.dragonfly.security.DataSecurityManager;
import com.agileapes.dragonfly.security.impl.MethodSubject;
import com.agileapes.dragonfly.security.impl.StoredProcedureSubject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class adds security to the method calls and procedure calls of the default data access implementation
 * available through {@link DefaultDataAccess}.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/20, 23:29)
 */
public class SecuredDataAccess extends DefaultDataAccess implements PartialDataAccess, EventHandlerContext {

    private static final Map<Integer, MethodDescriptor> methodDescriptors = new HashMap<Integer, MethodDescriptor>();
    private static final Annotation[] NO_ANNOTATIONS = new Annotation[0];

    static {
        methodDescriptors.put(3, new ImmutableMethodDescriptor(DefaultDataAccess.class, Object.class, "find", new Class[]{Class.class, Serializable.class}, NO_ANNOTATIONS));
        methodDescriptors.put(4, new ImmutableMethodDescriptor(DefaultDataAccess.class, List.class, "find", new Class[]{Object.class}, NO_ANNOTATIONS));
        methodDescriptors.put(5, new ImmutableMethodDescriptor(DefaultDataAccess.class, void.class, "save", new Class[]{Object.class}, NO_ANNOTATIONS));
        methodDescriptors.put(6, new ImmutableMethodDescriptor(DefaultDataAccess.class, void.class, "delete", new Class[]{Class.class, Serializable.class}, NO_ANNOTATIONS));
        methodDescriptors.put(7, new ImmutableMethodDescriptor(DefaultDataAccess.class, void.class, "delete", new Class[]{Object.class}, NO_ANNOTATIONS));
        methodDescriptors.put(8, new ImmutableMethodDescriptor(DefaultDataAccess.class, void.class, "truncate", new Class[]{Class.class}, NO_ANNOTATIONS));
        methodDescriptors.put(9, new ImmutableMethodDescriptor(DefaultDataAccess.class, boolean.class, "has", new Class[]{Object.class}, NO_ANNOTATIONS));
        methodDescriptors.put(10, new ImmutableMethodDescriptor(DefaultDataAccess.class, void.class, "deleteAll", new Class[]{Class.class}, NO_ANNOTATIONS));
        methodDescriptors.put(11, new ImmutableMethodDescriptor(DefaultDataAccess.class, List.class, "findAll", new Class[]{Class.class}, NO_ANNOTATIONS));
        methodDescriptors.put(12, new ImmutableMethodDescriptor(DefaultDataAccess.class, List.class, "call", new Class[]{Class.class, String.class, Object[].class}, NO_ANNOTATIONS));
        methodDescriptors.put(13, new ImmutableMethodDescriptor(DefaultDataAccess.class, int.class, "executeUpdate", new Class[]{Object.class, String.class}, NO_ANNOTATIONS));
        methodDescriptors.put(14, new ImmutableMethodDescriptor(DefaultDataAccess.class, int.class, "executeUpdate", new Class[]{Class.class, String.class, Map.class}, NO_ANNOTATIONS));
        methodDescriptors.put(15, new ImmutableMethodDescriptor(DefaultDataAccess.class, int.class, "executeUpdate", new Class[]{Object.class}, NO_ANNOTATIONS));
        methodDescriptors.put(16, new ImmutableMethodDescriptor(DefaultDataAccess.class, List.class, "executeQuery", new Class[]{Class.class, String.class, Map.class}, NO_ANNOTATIONS));
        methodDescriptors.put(17, new ImmutableMethodDescriptor(DefaultDataAccess.class, List.class, "executeQuery", new Class[]{Object.class}, NO_ANNOTATIONS));
        methodDescriptors.put(18, new ImmutableMethodDescriptor(DefaultDataAccess.class, List.class, "executeQuery", new Class[]{Class.class}, NO_ANNOTATIONS));
        methodDescriptors.put(19, new ImmutableMethodDescriptor(DefaultDataAccess.class, List.class, "executeQuery", new Class[]{Class.class, Map.class}, NO_ANNOTATIONS));
        methodDescriptors.put(20, new ImmutableMethodDescriptor(DefaultDataAccess.class, List.class, "executeQuery", new Class[]{Object.class, String.class}, NO_ANNOTATIONS));
        methodDescriptors.put(21, new ImmutableMethodDescriptor(DefaultDataAccess.class, void.class, "addInterface", new Class[]{Class.class, Class.class}, NO_ANNOTATIONS));
        methodDescriptors.put(22, new ImmutableMethodDescriptor(DefaultDataAccess.class, void.class, "addHandler", new Class[]{DataAccessEventHandler.class}, NO_ANNOTATIONS));
        methodDescriptors.put(23, new ImmutableMethodDescriptor(DefaultDataAccess.class, List.class, "executeUntypedQuery", new Class[]{Class.class, String.class, Map.class}, NO_ANNOTATIONS));
        methodDescriptors.put(24, new ImmutableMethodDescriptor(DefaultDataAccess.class, long.class, "countAll", new Class[]{Class.class}, NO_ANNOTATIONS));
        methodDescriptors.put(25, new ImmutableMethodDescriptor(DefaultDataAccess.class, long.class, "count", new Class[]{Object.class}, NO_ANNOTATIONS));
        methodDescriptors.put(26, new ImmutableMethodDescriptor(DefaultDataAccess.class, boolean.class, "exists", new Class[]{Object.class}, NO_ANNOTATIONS));
        methodDescriptors.put(27, new ImmutableMethodDescriptor(DefaultDataAccess.class, boolean.class, "exists", new Class[]{Class.class, Serializable.class}, NO_ANNOTATIONS));
        methodDescriptors.put(28, new ImmutableMethodDescriptor(DefaultDataAccess.class, List.class, "find", new Class[]{Object.class, String.class}, NO_ANNOTATIONS));
        methodDescriptors.put(29, new ImmutableMethodDescriptor(DefaultDataAccess.class, List.class, "findAll", new Class[]{Class.class, String.class}, NO_ANNOTATIONS));
    }

    private final DataSecurityManager securityManager;
    private static final Log log = LogFactory.getLog(DataAccess.class);

    public SecuredDataAccess(DataAccessSession session, DataSecurityManager securityManager, EntityContext entityContext, EntityHandlerContext handlerContext) {
        this(session, securityManager, entityContext, handlerContext, true);
    }

    public SecuredDataAccess(DataAccessSession session, DataSecurityManager securityManager, EntityContext entityContext, EntityHandlerContext handlerContext, boolean autoInitialize) {
        super(session, entityContext, handlerContext, autoInitialize);
        this.securityManager = securityManager;
        log.info("Initializing secured data access interface");
    }

    @Override
    public <E, K extends Serializable> E find(final Class<E> entityType, final K key) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(3)));
        return super.find(entityType, key);
    }

    @Override
    public <E> List<E> find(final E sample) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(4)));
        return super.find(sample);
    }

    @Override
    public <E> E save(final E entity) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(5)));
        return super.save(entity);
    }

    @Override
    public void delete(final Class entityType, final Serializable key) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(6)));
        super.delete(entityType, key);
    }

    @Override
    public void delete(final Object sample) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(7)));
        super.delete(sample);
    }

    @Override
    public void truncate(final Class entityType) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(8)));
        super.truncate(entityType);
    }

    @Override
    public void deleteAll(final Class entityType) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(10)));
        super.deleteAll(entityType);
    }

    @Override
    public <E> List<E> findAll(final Class<E> entityType) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(11)));
        return super.findAll(entityType);
    }

    @Override
    public List call(final Class entityType, final String procedureName, final Object[] parameters) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(12)));
        securityManager.checkAccess(new StoredProcedureSubject(entityType, procedureName, parameters));
        return super.call(entityType, procedureName, parameters);
    }

    @Override
    public int executeUpdate(final Object sample, final String queryName) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(13)));
        return super.executeUpdate(sample, queryName);
    }

    @Override
    public <E> int executeUpdate(final Class<E> entityType, final String queryName, final Map<String, Object> values) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(14)));
        return super.executeUpdate(entityType, queryName, values);
    }

    @Override
    public int executeUpdate(final Object sample) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(15)));
        return super.executeUpdate(sample);
    }

    @Override
    public <E> List<E> executeQuery(final Class<E> entityType, final String queryName, final Map<String, Object> values) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(16)));
        return super.executeQuery(entityType, queryName, values);
    }

    @Override
    public <E> List<E> executeQuery(final E sample) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(17)));
        return super.executeQuery(sample);
    }

    @Override
    public <E> List<E> executeQuery(final Class<E> resultType) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(18)));
        return super.executeQuery(resultType);
    }

    @Override
    public <E> List<E> executeQuery(final Class<E> resultType, final Map<String, Object> values) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(19)));
        return super.executeQuery(resultType, values);
    }

    @Override
    public <E> List<E> executeQuery(final E sample, final String queryName) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(20)));
        return super.executeQuery(sample, queryName);
    }

    @Override
    public void addHandler(final DataAccessEventHandler eventHandler) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(22)));
        super.addHandler(eventHandler);
    }

    @Override
    public <E> List<Map<String, Object>> executeUntypedQuery(final Class<E> entityType, final String queryName, final Map<String, Object> values) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(23)));
        return super.executeUntypedQuery(entityType, queryName, values);
    }

    @Override
    public <E> long countAll(Class<E> entityType) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(24)));
        return super.countAll(entityType);
    }

    @Override
    public <E> long count(E sample) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(25)));
        return super.count(sample);
    }

    @Override
    public <E> boolean exists(E sample) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(26)));
        return super.exists(sample);
    }

    @Override
    public <E, K extends Serializable> boolean exists(Class<E> entityType, K key) {
        securityManager.checkAccess(new MethodSubject(methodDescriptors.get(27)));
        return super.exists(entityType, key);
    }

    @Override
    public boolean equals(final Object that) {
        return super.equals(that);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public <E> List<E> find(E sample, String order) {
        return super.find(sample, order);
    }

    @Override
    public <E> List<E> findAll(Class<E> entityType, String order) {
        return super.findAll(entityType, order);
    }

}