/*
 * Copyright (c) 2013 AgileApes, Ltd.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall
 * be included in all copies or substantial portions of the
 * Software.
 */

package com.agileapes.dragonfly.metadata.impl;

import com.agileapes.dragonfly.metadata.ColumnMetadata;
import com.agileapes.dragonfly.metadata.TableMetadata;
import com.agileapes.dragonfly.tools.SynchronizedIdentifierDispenser;

import java.util.Collection;

/**
 * This class denotes a unique key constraint
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/8/29, 14:12)
 */
public class UniqueConstraintMetadata extends AbstractConstraintMetadata {

    private static SynchronizedIdentifierDispenser<TableMetadata<?>> dispenser = new SynchronizedIdentifierDispenser<TableMetadata<?>>();

    public UniqueConstraintMetadata(TableMetadata table, Collection<ColumnMetadata> columns) {
        super(table, columns);
    }

    @Override
    protected String getNameSuffix() {
        return "uk" + dispenser.getIdentifier(getTable());
    }
}
