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

package com.mmnaseri.dragonfly.tools;

import com.mmnaseri.couteau.context.contract.OrderedBean;
import com.mmnaseri.dragonfly.data.impl.TableKeyGeneratorEntity;
import com.mmnaseri.dragonfly.error.EntityDefinitionError;
import com.mmnaseri.dragonfly.metadata.ColumnMetadata;
import com.mmnaseri.dragonfly.metadata.TableMetadata;
import com.mmnaseri.dragonfly.metadata.TableMetadataContext;
import com.mmnaseri.dragonfly.metadata.TableMetadataContextPostProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;

/**
 * @deprecated we should not rely on core level inspections as the specific mode of entity handling is not yet
 * decided
 * @author Milad Naseri (mmnaseri@programmer.net)
 * @since 1.0 (2013/10/28, 14:01)
 */
@Deprecated
public class PostStartupInspector implements TableMetadataContextPostProcessor, OrderedBean {

    private static final Log log = LogFactory.getLog(PostStartupInspector.class);

    @Override
    public void postProcessMetadataContext(TableMetadataContext tableMetadataContext) {
        final Collection<Class<?>> entityTypes = tableMetadataContext.getEntityTypes();
        for (Class<?> entityType : entityTypes) {
            checkEntity(tableMetadataContext.getTableMetadata(entityType));
        }
    }

    private void checkEntity(TableMetadata<?> tableMetadata) {
        if (TableKeyGeneratorEntity.class.equals(tableMetadata.getEntityType())) {
            return;
        }
        findPrimitiveTypeColumns(tableMetadata);
        findEntityWithoutIdentifier(tableMetadata);
        findEntityWithVersionWithoutPrimaryKey(tableMetadata);
    }

    private void findEntityWithVersionWithoutPrimaryKey(TableMetadata<?> tableMetadata) {
        if (tableMetadata.getVersionColumn() != null && !tableMetadata.hasPrimaryKey()) {
            log.error("Optimistic locking will not work without primary key identifier", new EntityDefinitionError("Entities without a primary key cannot have a version column"));
        }
    }

    private void findEntityWithoutIdentifier(TableMetadata<?> tableMetadata) {
        if (!tableMetadata.hasPrimaryKey()) {
            log.warn("No primary key has been defined for entity: " + tableMetadata.getEntityType().getCanonicalName());
        }
    }

    private void findPrimitiveTypeColumns(TableMetadata<?> tableMetadata) {
        for (ColumnMetadata columnMetadata : tableMetadata.getColumns()) {
            if (columnMetadata.getPropertyType().isPrimitive()) {
                log.warn("Column has primitive type: " + columnMetadata.getPropertyType().getCanonicalName() + " in " + tableMetadata.getEntityType().getCanonicalName() + "." + columnMetadata.getName());
            }
        }
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

}
