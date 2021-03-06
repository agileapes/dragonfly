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

package com.mmnaseri.dragonfly.data.impl;

import com.mmnaseri.dragonfly.metadata.impl.ImmutableNamedQueryMetadata;
import com.mmnaseri.dragonfly.metadata.impl.ResolvedColumnMetadata;
import com.mmnaseri.dragonfly.metadata.impl.ResolvedTableMetadata;
import com.mmnaseri.dragonfly.metadata.impl.UniqueConstraintMetadata;
import com.mmnaseri.dragonfly.metadata.*;

import java.sql.Types;
import java.util.*;

/**
 * This is an entity designated for generation of keys that are to be automatically maintained
 * through the {@link ValueGenerationType#TABLE} strategy.
 *
 * @author Milad Naseri (mmnaseri@programmer.net)
 * @since 1.0 (2013/9/26, 17:58)
 */
public class TableKeyGeneratorEntity {

    private String name;
    private Long value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public static TableMetadata<TableKeyGeneratorEntity> getTableMetadata(String schema) {
        final HashSet<ConstraintMetadata> constraints = new HashSet<ConstraintMetadata>();
        final HashSet<ColumnMetadata> columns = new HashSet<ColumnMetadata>();
        final ResolvedColumnMetadata nameColumn = new ResolvedColumnMetadata(null, TableKeyGeneratorEntity.class, "name", Types.VARCHAR, "name", String.class, false, 256, 0, 0, false, false);
        columns.add(nameColumn);
        columns.add(new ResolvedColumnMetadata(null, TableKeyGeneratorEntity.class, "value", Types.BIGINT, "value", Long.class, false, 0, 0, 0, false, false));
        final List<NamedQueryMetadata> namedQueries = new ArrayList<NamedQueryMetadata>();
        final ResolvedTableMetadata<TableKeyGeneratorEntity> tableMetadata = new ResolvedTableMetadata<TableKeyGeneratorEntity>(TableKeyGeneratorEntity.class, schema, "dragonfly_sequences", constraints, columns, namedQueries, Collections.<SequenceMetadata>emptyList(), Collections.<StoredProcedureMetadata>emptyList(), Collections.<RelationMetadata<TableKeyGeneratorEntity, ?>>emptyList(), null, null);
        namedQueries.add(new ImmutableNamedQueryMetadata("increment", "UPDATE ${qualify(table)} SET ${escape('value')} = ${escape('value')} + 1 WHERE ${escape('name')} = ${value.name}", tableMetadata, QueryType.NATIVE));
        constraints.add(new UniqueConstraintMetadata(tableMetadata, Arrays.<ColumnMetadata>asList(nameColumn)));
        return tableMetadata;
    }

}
