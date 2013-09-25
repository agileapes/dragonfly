package com.agileapes.dragonfly.data.impl;

import com.agileapes.couteau.basics.api.impl.MirrorFilter;
import com.agileapes.couteau.context.impl.OrderedBeanComparator;
import com.agileapes.dragonfly.data.*;
import com.agileapes.dragonfly.data.impl.op.*;
import com.agileapes.dragonfly.error.AmbiguousCallbackError;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.agileapes.couteau.basics.collections.CollectionWrapper.with;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/26, 2:24)
 */
@SuppressWarnings("unchecked")
public class DelegatingDataAccess implements DataAccess {

    private static class Executable<E extends DataOperation> {

        private final E dataOperation;
        private final DataCallback<E> callback;

        private Executable(E dataOperation, DataCallback<E> callback) {
            this.dataOperation = dataOperation;
            this.callback = callback;
        }

        private Object execute() {
            return callback.execute(dataOperation);
        }

    }

    private List<DataCallback<DataOperation>> callbacks = new CopyOnWriteArrayList<DataCallback<DataOperation>>();
    private final DataAccess dataAccess;

    public DelegatingDataAccess(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public synchronized void addCallback(DataCallback<?> callback) {
        callbacks = with(callbacks).add(new SmartDataCallback<DataOperation>((DataCallback<DataOperation>) callback)).sort(new OrderedBeanComparator()).concurrentList();
    }

    private <E extends DataOperation> Executable<E> given(E operation, DataCallback<E> defaultCallback) {
        final DataCallback<E> callback;
        final List<DataCallback<DataOperation>> candidates = with(callbacks).keep(new MirrorFilter<DataOperation>(operation)).sort(new OrderedBeanComparator()).list();
        if (candidates.size() == 1) {
            callback = (DataCallback<E>) candidates.get(0);
        } else if (!candidates.isEmpty()) {
            throw new AmbiguousCallbackError(operation);
        } else {
            callback = defaultCallback;
        }
        return new Executable<E>(operation, callback);
    }

    @Override
    public <E> E save(final E entity) {
        final SampledDataOperation operation = new SampledDataOperation(dataAccess, OperationType.SAVE, entity);
        return (E) given(operation, new AbstractDefaultDataCallback<SampledDataOperation>() {
            @Override
            public Object execute(SampledDataOperation operation) {
                return dataAccess.save(entity);
            }
        }).execute();
    }


    @Override
    public <E> void delete(E entity) {
        given(new SampledDataOperation(dataAccess, OperationType.DELETE, entity), new AbstractProceduralDataCallback<SampledDataOperation>() {
            @Override
            protected void executeWithoutResults(SampledDataOperation operation) {
                dataAccess.delete(operation.getSample());
            }
        }).execute();
    }

    @Override
    public <E, K extends Serializable> void delete(Class<E> entityType, K key) {
        given(new IdentifiableDataOperation(dataAccess, OperationType.DELETE, entityType, key), new AbstractProceduralDataCallback<IdentifiableDataOperation>() {
            @Override
            protected void executeWithoutResults(IdentifiableDataOperation operation) {
                dataAccess.delete(operation.getEntityType(), operation.getKey());
            }
        });
    }

    @Override
    public <E> void deleteAll(Class<E> entityType) {
        given(new TypedDataOperation(dataAccess, OperationType.DELETE, entityType), new AbstractProceduralDataCallback<TypedDataOperation>() {
            @Override
            protected void executeWithoutResults(TypedDataOperation operation) {
                dataAccess.deleteAll(operation.getEntityType());
            }
        }).execute();
    }

    @Override
    public <E> void truncate(Class<E> entityType) {
        given(new TypedDataOperation(dataAccess, OperationType.TRUNCATE, entityType), new AbstractProceduralDataCallback<TypedDataOperation>() {
            @Override
            protected void executeWithoutResults(TypedDataOperation operation) {
                dataAccess.truncate(operation.getEntityType());
            }
        }).execute();
    }

    @Override
    public <E> List<E> find(E sample) {
        return (List<E>) given(new SampledDataOperation(dataAccess, OperationType.FIND, sample), new AbstractDefaultDataCallback<SampledDataOperation>() {
            @Override
            public Object execute(SampledDataOperation operation) {
                return dataAccess.find(operation.getSample());
            }
        }).execute();
    }

    @Override
    public <E, K extends Serializable> E find(Class<E> entityType, K key) {
        return (E) given(new IdentifiableDataOperation(dataAccess, OperationType.FIND, entityType, key), new AbstractDefaultDataCallback<IdentifiableDataOperation>() {
            @Override
            public Object execute(IdentifiableDataOperation operation) {
                return dataAccess.find(operation.getEntityType(), operation.getKey());
            }
        }).execute();
    }

    @Override
    public <E> List<E> findAll(Class<E> entityType) {
        return (List<E>) given(new TypedDataOperation(dataAccess, OperationType.FIND, entityType), new AbstractDefaultDataCallback<TypedDataOperation>() {
            @Override
            public Object execute(TypedDataOperation operation) {
                return dataAccess.findAll(operation.getEntityType());
            }
        }).execute();
    }

    @Override
    public <E> int executeUpdate(Class<E> entityType, String queryName, Map<String, Object> values) {
        return (Integer) given(new TypedQueryDataOperation(dataAccess, OperationType.NAMED_UPDATE, entityType, queryName, values), new AbstractDefaultDataCallback<TypedQueryDataOperation>() {
            @Override
            public Object execute(TypedQueryDataOperation operation) {
                return dataAccess.executeUpdate(operation.getEntityType(), operation.getQueryName(), operation.getMap());
            }
        }).execute();
    }

    @Override
    public <E> int executeUpdate(E sample, String queryName) {
        return (Integer) given(new SampledQueryDataOperation(dataAccess, OperationType.NAMED_UPDATE, sample, queryName), new AbstractDefaultDataCallback<SampledQueryDataOperation>() {
            @Override
            public Object execute(SampledQueryDataOperation operation) {
                return dataAccess.executeUpdate(operation.getSample(), operation.getQueryName());
            }
        }).execute();
    }

    @Override
    public <E> List<E> executeQuery(Class<E> entityType, String queryName, Map<String, Object> values) {
        return (List<E>) given(new TypedQueryDataOperation(dataAccess, OperationType.NAMED_QUERY, entityType, queryName, values), new AbstractDefaultDataCallback<TypedQueryDataOperation>() {
            @Override
            public Object execute(TypedQueryDataOperation operation) {
                return dataAccess.executeQuery(operation.getEntityType(), operation.getQueryName(), operation.getMap());
            }
        }).execute();
    }

    @Override
    public <E> List<E> executeQuery(E sample, String queryName) {
        return (List<E>) given(new SampledQueryDataOperation(dataAccess, OperationType.NAMED_QUERY, sample, queryName), new AbstractDefaultDataCallback<SampledQueryDataOperation>() {
            @Override
            public Object execute(SampledQueryDataOperation operation) {
                return dataAccess.executeQuery(operation.getSample(), operation.getQueryName());
            }
        }).execute();
    }

    @Override
    public <E> List<?> call(Class<E> entityType, String procedureName, Object... parameters) {
        return (List<?>) given(new ProcedureCallDataOperation(dataAccess, OperationType.PROCEDURE, entityType, procedureName, parameters), new AbstractDefaultDataCallback<ProcedureCallDataOperation>() {
            @Override
            public Object execute(ProcedureCallDataOperation operation) {
                return dataAccess.call(operation.getEntityType(), operation.getProcedureName(), operation.getParameters());
            }
        }).execute();
    }

    @Override
    public <E> long countAll(Class<E> entityType) {
        return (Long) given(new TypedDataOperation(dataAccess, OperationType.COUNT, entityType), new AbstractDefaultDataCallback<TypedDataOperation>() {
            @Override
            public Object execute(TypedDataOperation operation) {
                return dataAccess.countAll(operation.getEntityType());
            }
        }).execute();
    }

    @Override
    public <E> long count(E sample) {
        return (Long) given(new SampledDataOperation(dataAccess, OperationType.COUNT, sample), new AbstractDefaultDataCallback<SampledDataOperation>() {
            @Override
            public Object execute(SampledDataOperation operation) {
                return dataAccess.count(operation.getSample());
            }
        }).execute();
    }

    @Override
    public <E> boolean exists(E sample) {
        return (Boolean) given(new SampledDataOperation(dataAccess, OperationType.EXISTS, sample), new AbstractDefaultDataCallback<SampledDataOperation>() {
            @Override
            public Object execute(SampledDataOperation operation) {
                return dataAccess.exists(operation.getSample());
            }
        }).execute();
    }

    @Override
    public <E, K extends Serializable> boolean exists(Class<E> entityType, K key) {
        return (Boolean) given(new IdentifiableDataOperation(dataAccess, OperationType.EXISTS, entityType, key), new AbstractDefaultDataCallback<IdentifiableDataOperation>() {
            @Override
            public Object execute(IdentifiableDataOperation operation) {
                return dataAccess.exists(operation.getEntityType(), operation.getKey());
            }
        }).execute();
    }

    @Override
    public List<Integer> run(BatchOperation batchOperation) {
        return dataAccess.run(batchOperation);
    }

}