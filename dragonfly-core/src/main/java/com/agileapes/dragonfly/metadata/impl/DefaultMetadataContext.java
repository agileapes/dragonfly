package com.agileapes.dragonfly.metadata.impl;

import com.agileapes.couteau.basics.api.Cache;
import com.agileapes.couteau.basics.api.Filter;
import com.agileapes.couteau.basics.api.Processor;
import com.agileapes.couteau.basics.api.impl.ConcurrentCache;
import com.agileapes.couteau.basics.api.impl.EqualityFilter;
import com.agileapes.dragonfly.data.impl.ManyToManyMiddleEntity;
import com.agileapes.dragonfly.data.impl.TableKeyGeneratorEntity;
import com.agileapes.dragonfly.error.*;
import com.agileapes.dragonfly.metadata.*;
import com.agileapes.dragonfly.tools.ColumnNameFilter;
import com.agileapes.dragonfly.tools.DatabaseUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.agileapes.couteau.basics.collections.CollectionWrapper.with;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/8/30, 15:46)
 */
public class DefaultMetadataContext extends DefaultMetadataRegistry implements MetadataContext {

    private final Cache<Class<?>, TableMetadata<?>> metadataCache = new ConcurrentCache<Class<?>, TableMetadata<?>>();
    private final Set<TableMetadata<?>> manyToManyTables = new CopyOnWriteArraySet<TableMetadata<?>>();
    private Set<MetadataRegistry> registries = new CopyOnWriteArraySet<MetadataRegistry>();
    private final Set<Class<?>> entityTypes = new HashSet<Class<?>>();
    private boolean ready = true;

    public DefaultMetadataContext() {
        addMetadataRegistry(this);
    }

    @Override
    public Collection<Class<?>> getEntityTypes() {
        if (!ready) {
            rebuildCache();
        }
        return new HashSet<Class<?>>(entityTypes);
    }

    public void setRegistries(Set<MetadataRegistry> registries) {
        this.registries = new CopyOnWriteArraySet<MetadataRegistry>(registries);
        this.registries.add(this);
        ready = false;
    }

    @Override
    public synchronized void addMetadataRegistry(MetadataRegistry registry) {
        registries.add(registry);
        ready = false;
        registry.setChangeCallback(new Processor<MetadataRegistry>() {
            @Override
            public void process(MetadataRegistry registry) {
                entityTypes.addAll((registry == DefaultMetadataContext.this) ? DefaultMetadataContext.super.getEntityTypes() : registry.getEntityTypes());
                ready = false;
            }
        });
    }

    @Override
    public <E> TableMetadata<E> getTableMetadata(final Class<E> entityType) {
        if (!ready) {
            rebuildCache();
        }
        if (!metadataCache.contains(entityType)) {
            throw new UnresolvedTableMetadataError(entityType);
        }
        //noinspection unchecked
        return (TableMetadata<E>) metadataCache.read(entityType);
    }

    private synchronized void rebuildCache() {
        //we first clear the cache
        metadataCache.invalidate();
        String schema = null;
        //then we fill a map from entity-type to table metadata from each registry
        final Map<Class<?>, TableMetadata<?>> map = new HashMap<Class<?>, TableMetadata<?>>();
        for (MetadataRegistry registry : registries) {
            final Collection<Class<?>> entityTypes = registry == this ? super.getEntityTypes() : registry.getEntityTypes();
            for (Class<?> entityType : entityTypes) {
                map.put(entityType, registry == this ? super.getTableMetadata(entityType) : registry.getTableMetadata(entityType));
                if (schema == null && map.get(entityType).getSchema() != null && !map.get(entityType).getSchema().isEmpty()) {
                    schema = map.get(entityType).getSchema();
                }
            }
        }
        //adding table metadata for the key generator table so that we can later
        //rely on the table being there before calculating generated keys
        if (schema != null) {
            final TableMetadata<TableKeyGeneratorEntity> tableMetadata = TableKeyGeneratorEntity.getTableMetadata(schema);
            map.put(tableMetadata.getEntityType(), tableMetadata);
            addInternalMetadata(tableMetadata);
        }
        Map<ManyToManyDescriptor, TableMetadata<?>> manyToManyMiddleTables = new HashMap<ManyToManyDescriptor, TableMetadata<?>>();
        //we then resolve all unresolved foreign references
        for (Map.Entry<Class<?>, TableMetadata<?>> entry : map.entrySet()) {
            final TableMetadata<?> metadata = entry.getValue();
            collectManyToManyRelations(map, manyToManyMiddleTables, metadata, schema);
            for (ColumnMetadata columnMetadata : metadata.getColumns()) {
                if (columnMetadata instanceof UnresolvedColumnMetadata) {
                    throw new MetadataCollectionError("Metadata cannot be resolved", new UnresolvedColumnMetadataError());
                }
                final ColumnMetadata foreignColumn = columnMetadata.getForeignReference();
                if (foreignColumn == null) {
                    continue;
                }
                if (!(foreignColumn instanceof UnresolvedColumnMetadata)) {
                    continue;
                }
                final ColumnMetadata resolvedColumn = resolveColumn(map, metadata, foreignColumn);
                ((ResolvedColumnMetadata) columnMetadata).setForeignReference(resolvedColumn);
            }
            for (ReferenceMetadata<?, ?> referenceMetadata : metadata.getForeignReferences()) {
                if (RelationType.MANY_TO_MANY.equals(referenceMetadata.getRelationType())) {
                    final ManyToManyDescriptor key = new ManyToManyDescriptor(metadata.getEntityType(), referenceMetadata.getPropertyName(), referenceMetadata.getForeignColumn().getTable().getEntityType(), referenceMetadata.getForeignColumn().getName());
                    final TableMetadata<?> foreignTable = manyToManyMiddleTables.get(with(manyToManyMiddleTables.keySet()).find(new EqualityFilter<ManyToManyDescriptor>(key)));
                    ((ImmutableReferenceMetadata) referenceMetadata).setForeignColumn(with(foreignTable.getColumns()).find(new ColumnNameFilter(metadata.getName())));
                    continue;
                }
                ((ImmutableReferenceMetadata) referenceMetadata).setForeignColumn(resolveColumn(map, metadata, referenceMetadata.getForeignColumn()));
            }
        }
        entityTypes.clear();
        entityTypes.addAll(map.keySet());
        for (Map.Entry<Class<?>, TableMetadata<?>> entry : map.entrySet()) {
            metadataCache.write(entry.getKey(), entry.getValue());
        }
        manyToManyTables.clear();
        manyToManyTables.addAll(manyToManyMiddleTables.values());
        ready = true;
    }

    private void collectManyToManyRelations(final Map<Class<?>, TableMetadata<?>> tables, final Map<ManyToManyDescriptor, TableMetadata<?>> map, final TableMetadata<?> tableMetadata, final String schema) {
        //noinspection unchecked
        with(tableMetadata.getForeignReferences())
                .keep(new Filter<ReferenceMetadata<?, ?>>() {
                    @Override
                    public boolean accepts(ReferenceMetadata<?, ?> item) {
                        return RelationType.MANY_TO_MANY.equals(item.getRelationType());
                    }
                })
                .each(new Processor<ReferenceMetadata<?, ?>>() {
                    @Override
                    public void process(ReferenceMetadata<?, ?> reference) {
                        final ManyToManyDescriptor key = new ManyToManyDescriptor(tableMetadata.getEntityType(), reference.getPropertyName(), reference.getForeignColumn().getTable().getEntityType(), reference.getForeignColumn().getName());
                        if (with(map.keySet()).exists(new EqualityFilter<ManyToManyDescriptor>(key))) {
                            return;
                        }
                        final TableMetadata<?> firstTable = tables.get(key.getHere());
                        final TableMetadata<?> secondTable = tables.get(key.getThere());
                        final String middleTableName = DatabaseUtils.getMiddleTableName(firstTable, secondTable, key.getLocalProperty(), key.getTargetProperty());
                        final HashSet<ColumnMetadata> columns = new HashSet<ColumnMetadata>();
                        final ColumnMetadata firstKey = firstTable.getPrimaryKey().getColumns().iterator().next();
                        final ColumnMetadata secondKey = secondTable.getPrimaryKey().getColumns().iterator().next();
                        final ResolvedColumnMetadata firstColumn = new ResolvedColumnMetadata(null, ManyToManyMiddleEntity.class, firstTable.getName(), firstKey.getType(), "first", Object.class, false, firstKey.getLength(), firstKey.getPrecision(), firstKey.getScale(), null, null, firstKey);
                        columns.add(firstColumn);
                        final ResolvedColumnMetadata secondColumn = new ResolvedColumnMetadata(null, ManyToManyMiddleEntity.class, secondTable.getName(), secondKey.getType(), "second", Object.class, false, secondKey.getLength(), secondKey.getPrecision(), secondKey.getScale(), null, null, secondKey);
                        columns.add(secondColumn);
                        final List<ConstraintMetadata> constraints = new ArrayList<ConstraintMetadata>();
                        //noinspection unchecked
                        final ResolvedTableMetadata metadata = new ResolvedTableMetadata(ManyToManyMiddleEntity.class, schema, middleTableName, constraints, columns, Collections.<NamedQueryMetadata>emptyList(), Collections.<SequenceMetadata>emptyList(), Collections.<StoredProcedureMetadata>emptyList(), new HashSet<ReferenceMetadata<Object, ?>>());
                        constraints.add(new UniqueConstraintMetadata(metadata, columns));
                        constraints.add(new ForeignKeyConstraintMetadata(metadata, firstColumn));
                        constraints.add(new ForeignKeyConstraintMetadata(metadata, secondColumn));
                        map.put(key, metadata);
                    }
                });
    }

    private static ColumnMetadata resolveColumn(Map<Class<?>, TableMetadata<?>> map, TableMetadata<?> localTable, ColumnMetadata foreignColumn) {
        TableMetadata<?> foreignTable = foreignColumn.getTable();
        final ColumnMetadata resolvedColumn;
        if (foreignTable instanceof UnresolvedTableMetadata<?>) {
            foreignTable = map.get(foreignTable.getEntityType());
        }
        if (foreignTable == null || !(foreignTable instanceof ResolvedTableMetadata<?>)) {
            throw new NoSuchEntityError(foreignColumn.getTable().getEntityType());
        }
        if (foreignColumn.getName() == null || foreignColumn.getName().isEmpty()) {
            final PrimaryKeyConstraintMetadata primaryKey = foreignTable.getPrimaryKey();
            if (primaryKey.getColumns().size() != 1) {
                throw new EntityDefinitionError("Entity " + localTable.getEntityType().getCanonicalName() + " references composite primary key at " + foreignTable.getEntityType().getCanonicalName());
            }
            resolvedColumn = primaryKey.getColumns().iterator().next();
        } else {
            resolvedColumn = with(foreignTable.getColumns()).keep(new ColumnNameFilter(foreignColumn.getName())).first();
            if (resolvedColumn == null) {
                throw new NoSuchColumnError(foreignTable.getEntityType(), foreignColumn.getName());
            }
        }
        return resolvedColumn;
    }

    public Set<TableMetadata<?>> getManyToManyTables() {
        if (!ready) {
            rebuildCache();
        }
        return manyToManyTables;
    }

}
