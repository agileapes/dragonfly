package com.agileapes.dragonfly.metadata.impl;

import com.agileapes.dragonfly.metadata.*;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/12, 0:17)
 */
public class ImmutableReferenceMetadata<S, D> implements ReferenceMetadata<S, D> {

    private final String propertyName;
    private ColumnMetadata foreignColumn;
    private TableMetadata<S> localTable;
    private TableMetadata<D> foreignTable;
    private final RelationType relationType;
    private final boolean lazy;
    private final CascadeMetadata cascadeMetadata;

    public ImmutableReferenceMetadata(TableMetadata<S> localTable, String propertyName, TableMetadata<D> foreignTable, ColumnMetadata foreignColumn, RelationType relationType, CascadeMetadata cascadeMetadata, boolean lazy) {
        this.localTable = localTable;
        this.foreignTable = foreignTable;
        this.relationType = relationType;
        this.lazy = lazy;
        this.cascadeMetadata = cascadeMetadata;
        this.foreignColumn = foreignColumn;
        this.propertyName = propertyName;
    }

    @Override
    public TableMetadata<S> getLocalTable() {
        return localTable;
    }

    @Override
    public TableMetadata<D> getForeignTable() {
        return foreignTable;
    }

    @Override
    public RelationType getRelationType() {
        return relationType;
    }

    @Override
    public boolean isLazy() {
        return lazy;
    }

    @Override
    public CascadeMetadata getCascadeMetadata() {
        return cascadeMetadata;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public ColumnMetadata getForeignColumn() {
        return foreignColumn;
    }

    public void setLocalTable(TableMetadata<S> localTable) {
        this.localTable = localTable;
    }

    public void setForeignColumn(ColumnMetadata foreignColumn) {
        this.foreignColumn = foreignColumn;
        //noinspection unchecked
        this.foreignTable = (TableMetadata<D>) foreignColumn.getTable();
    }
}