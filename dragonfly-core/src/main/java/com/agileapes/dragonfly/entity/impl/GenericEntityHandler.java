package com.agileapes.dragonfly.entity.impl;

import com.agileapes.couteau.basics.api.Filter;
import com.agileapes.couteau.basics.api.Processor;
import com.agileapes.couteau.basics.api.Transformer;
import com.agileapes.couteau.basics.api.impl.NegatingFilter;
import com.agileapes.couteau.reflection.beans.BeanAccessor;
import com.agileapes.couteau.reflection.beans.BeanWrapper;
import com.agileapes.couteau.reflection.beans.impl.MethodBeanAccessor;
import com.agileapes.couteau.reflection.beans.impl.MethodBeanWrapper;
import com.agileapes.couteau.reflection.error.NoSuchPropertyException;
import com.agileapes.couteau.reflection.property.WritePropertyAccessor;
import com.agileapes.couteau.reflection.util.ReflectionUtils;
import com.agileapes.dragonfly.data.DataAccess;
import com.agileapes.dragonfly.data.DataAccessSession;
import com.agileapes.dragonfly.data.impl.ManyToManyActionHelper;
import com.agileapes.dragonfly.data.impl.ManyToManyMiddleEntity;
import com.agileapes.dragonfly.entity.EntityContext;
import com.agileapes.dragonfly.entity.EntityHandler;
import com.agileapes.dragonfly.entity.EntityInitializationContext;
import com.agileapes.dragonfly.entity.InitializedEntity;
import com.agileapes.dragonfly.error.EntityDefinitionError;
import com.agileapes.dragonfly.error.EntityPreparationError;
import com.agileapes.dragonfly.error.NoPrimaryKeyDefinedError;
import com.agileapes.dragonfly.error.UnsuccessfulOperationError;
import com.agileapes.dragonfly.metadata.*;
import com.agileapes.dragonfly.metadata.impl.PrimaryKeyConstraintMetadata;
import com.agileapes.dragonfly.tools.ColumnNameFilter;
import com.agileapes.dragonfly.tools.ColumnPropertyFilter;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static com.agileapes.couteau.basics.collections.CollectionWrapper.with;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/18, 15:28)
 */
public class GenericEntityHandler<E> implements EntityHandler<E> {

    private final Class<E> entityType;
    private final DefaultEntityMapCreator mapCreator;
    private final DefaultMapEntityCreator entityCreator;
    private final EntityContext entityContext;
    private final TableMetadata<E> tableMetadata;
    private boolean keyAutoGenerated;
    private boolean hasPrimaryKey;
    private String keyProperty;

    public GenericEntityHandler(Class<E> entityType, EntityContext entityContext, TableMetadata<E> tableMetadata) {
        this.entityType = entityType;
        this.entityContext = entityContext;
        this.tableMetadata = tableMetadata;
        mapCreator = new DefaultEntityMapCreator();
        entityCreator = new DefaultMapEntityCreator();
        hasPrimaryKey = this.tableMetadata.hasPrimaryKey();
        if (hasPrimaryKey) {
            final PrimaryKeyConstraintMetadata primaryKey = this.tableMetadata.getPrimaryKey();
            if (primaryKey.getColumns().size() != 1) {
                hasPrimaryKey = false;
            } else {
                final ColumnMetadata columnMetadata = primaryKey.getColumns().iterator().next();
                keyProperty = columnMetadata.getPropertyName();
                keyAutoGenerated = ValueGenerationType.AUTO.equals(columnMetadata.getGenerationType());
            }
        }
    }

    @Override
    public Class<E> getEntityType() {
        return entityType;
    }

    @Override
    public Map<String, Object> toMap(E entity) {
        return mapCreator.toMap(tableMetadata, entity);
    }

    @Override
    public E fromMap(E entity, final Map<String, Object> map) {
        return entityCreator.fromMap(entity, tableMetadata.getColumns(), map);
    }

    @Override
    public Serializable getKey(E entity) {
        if (!hasKey()) {
            throw new NoPrimaryKeyDefinedError(entityType);
        }
        try {
            return new MethodBeanAccessor<E>(entity).getPropertyValue(keyProperty, Serializable.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setKey(E entity, Serializable key) {
        if (!hasKey()) {
            throw new NoPrimaryKeyDefinedError(entityType);
        }
        try {
            new MethodBeanWrapper<E>(entity).setPropertyValue(keyProperty, key);
        } catch (Exception e) {
            throw new EntityDefinitionError("Failed to set key on entity " + entityType, e);
        }
    }

    @Override
    public boolean hasKey() {
        return hasPrimaryKey;
    }

    @Override
    public boolean isKeyAutoGenerated() {
        if (!hasKey()) {
            throw new NoPrimaryKeyDefinedError(entityType);
        }
        return keyAutoGenerated;
    }

    @Override
    public void copy(E original, E copy) {
        if (original instanceof InitializedEntity) {
            ((InitializedEntity) original).freeze();
        }
        final BeanAccessor<E> reader = new MethodBeanAccessor<E>(original);
        final BeanWrapper<E> writer = new MethodBeanWrapper<E>(copy);
        //noinspection unchecked
        with(reader.getPropertyNames())
                .keep(new Filter<String>() {
                    @Override
                    public boolean accepts(String item) {
                        try {
                            return writer.isWritable(item);
                        } catch (NoSuchPropertyException e) {
                            return false;
                        }
                    }
                })
                .each(new Processor<String>() {
                    @Override
                    public void process(String input) {
                        try {
                            writer.setPropertyValue(input, reader.getPropertyValue(input));
                        } catch (Exception ignored) {
                        }
                    }
                });
        if (original instanceof InitializedEntity) {
            ((InitializedEntity) original).unfreeze();
        }
    }

    @Override
    public String getKeyProperty() {
        if (!hasKey()) {
            throw new NoPrimaryKeyDefinedError(entityType);
        }
        return keyProperty;
    }

    @Override
    public void loadEagerRelations(final E entity, final Map<String, Object> values, final EntityInitializationContext initializationContext) {
        final MethodBeanWrapper<E> wrapper = new MethodBeanWrapper<E>(entity);
        //noinspection unchecked
        with(tableMetadata.getForeignReferences())
                .drop(new Filter<ReferenceMetadata<E, ?>>() {
                    @Override
                    public boolean accepts(ReferenceMetadata<E, ?> item) {
                        return item.isLazy();
                    }
                })
                .forThose(new Filter<ReferenceMetadata<E, ?>>() {
                              @Override
                              public boolean accepts(ReferenceMetadata<E, ?> reference) {
                                  return reference.isRelationOwner() && reference.getRelationType().getForeignCardinality() == 1;
                              }
                          },
                        new Processor<ReferenceMetadata<E, ?>>() {
                            @Override
                            public void process(final ReferenceMetadata<E, ?> reference) {
                                final ColumnMetadata columnMetadata = with(reference.getLocalTable().getColumns()).find(new ColumnPropertyFilter(reference.getPropertyName()));
                                final String columnName = with(values.keySet()).find(new Filter<String>() {
                                    @Override
                                    public boolean accepts(String item) {
                                        return item.equalsIgnoreCase(columnMetadata.getName());
                                    }
                                });
                                final Object foreignKey = values.get(columnName);
                                if (foreignKey == null) {
                                    return;
                                }
                                final Object foreignEntity;
                                if (hasKey() && getKey(entity) != null) {
                                    foreignEntity = initializationContext.get(reference.getForeignTable().getEntityType(), (Serializable) foreignKey, getEntityType(), getKey(entity));
                                } else {
                                    foreignEntity = initializationContext.get(reference.getForeignTable().getEntityType(), (Serializable) foreignKey);
                                }
                                try {
                                    wrapper.setPropertyValue(reference.getPropertyName(), foreignEntity);
                                } catch (Exception e) {
                                    throw new EntityPreparationError("Could not set relation property " + reference.getLocalTable().getEntityType().getCanonicalName() + "." + reference.getPropertyName(), e);
                                }
                            }
                        }
                )
                .forThose(new Filter<ReferenceMetadata<E, ?>>() {
                              @Override
                              public boolean accepts(ReferenceMetadata<E, ?> reference) {
                                  return !reference.isRelationOwner() && reference.getRelationType().getLocalCardinality() == 1;
                              }
                          }, new Processor<ReferenceMetadata<E, ?>>() {
                              @Override
                              public void process(ReferenceMetadata<E, ?> reference) {
                                  final Object foreignEntity = entityContext.getInstance(reference.getForeignTable().getEntityType());
                                  final MethodBeanWrapper<Object> foreignEntityWrapper = new MethodBeanWrapper<Object>(foreignEntity);
                                  try {
                                      foreignEntityWrapper.setPropertyValue(reference.getForeignColumn().getPropertyName(), entity);
                                  } catch (Exception e) {
                                      throw new EntityPreparationError("Could not set relation property " + reference.getForeignTable().getEntityType().getCanonicalName() + "." + reference.getForeignColumn().getPropertyName(), e);
                                  }
                                  final List<Object> objects = initializationContext.getDataAccess().find(foreignEntity);
                                  final Object propertyValue;
                                  if (reference.getRelationType().getForeignCardinality() == 1) {
                                      if (objects.isEmpty()) {
                                          return;
                                      } else if (objects.size() > 1) {
                                          throw new EntityDefinitionError("More than one item corresponds to one-to-one relationship");
                                      }
                                      propertyValue = objects.get(0);
                                  } else {
                                      try {
                                          propertyValue = ReflectionUtils.getCollection(wrapper.getPropertyType(reference.getPropertyName()));
                                      } catch (NoSuchPropertyException e) {
                                          throw new EntityDefinitionError("Failed to get property type " + reference.getLocalTable().getEntityType().getCanonicalName() + "." + reference.getPropertyName());
                                      }
                                      //noinspection unchecked
                                      ((Collection<Object>) propertyValue).addAll(objects);
                                  }
                                  try {
                                      wrapper.setPropertyValue(reference.getPropertyName(), propertyValue);
                                  } catch (Exception e) {
                                      throw new EntityPreparationError("Could not set relation property " + reference.getLocalTable().getEntityType().getCanonicalName() + "." + reference.getPropertyName(), e);
                                  }
                              }
                          }
                );
    }

    @Override
    public void loadLazyRelation(E entity, final ReferenceMetadata<E, ?> referenceMetadata, final DataAccess dataAccess, EntityContext entityContext, Map<String, Object> map, DataAccessSession session) {
        final BeanWrapper<E> wrapper = new MethodBeanWrapper<E>(entity);
        if (referenceMetadata.getRelationType().equals(RelationType.ONE_TO_ONE)) {
            if (referenceMetadata.isRelationOwner()) {
                final ColumnMetadata columnMetadata = with(tableMetadata.getColumns()).find(new ColumnPropertyFilter(referenceMetadata.getPropertyName()));
                final String key = with(map.keySet()).find(new Filter<String>() {
                    @Override
                    public boolean accepts(String item) {
                        return item.equalsIgnoreCase(columnMetadata.getName());
                    }
                });
                if (!map.containsKey(key) || map.get(key) == null) {
                    return;
                }
                final Object foreignEntity = entityContext.getInstance(referenceMetadata.getForeignTable().getEntityType());
                if (!columnMetadata.getForeignReference().getDeclaringClass().isInstance(foreignEntity)) {
                    return;
                }
                final BeanWrapper<Object> foreignEntityWrapper = new MethodBeanWrapper<Object>(foreignEntity);
                try {
                    foreignEntityWrapper.setPropertyValue(columnMetadata.getForeignReference().getPropertyName(), map.get(key));
                } catch (Exception e) {
                    throw new EntityPreparationError("Failed to prepare entity properties", e);
                }
                final List<Object> objects = dataAccess.find(foreignEntity);
                if (objects.isEmpty()) {
                    return;
                }
                if (objects.size() > 1) {
                    throw new EntityPreparationError("More than one entity correspond to one-to-one relationship");
                }
                try {
                    wrapper.setPropertyValue(referenceMetadata.getPropertyName(), objects.get(0));
                } catch (Exception e) {
                    throw new EntityPreparationError("Failed to prepare entity", e);
                }
            } else {
                final Object foreignEntity = entityContext.getInstance(referenceMetadata.getForeignTable().getEntityType());
                final BeanWrapper<Object> foreignEntityWrapper = new MethodBeanWrapper<Object>(foreignEntity);
                try {
                    foreignEntityWrapper.setPropertyValue(referenceMetadata.getForeignColumn().getPropertyName(), entity);
                } catch (Exception e) {
                    throw new EntityPreparationError("Failed to prepare entity properties", e);
                }
                final List<Object> objects = dataAccess.find(foreignEntity);
                if (objects.isEmpty()) {
                    return;
                }
                if (objects.size() > 1) {
                    throw new EntityPreparationError("More than one entity correspond to one-to-one relationship");
                }
                try {
                    wrapper.setPropertyValue(referenceMetadata.getPropertyName(), objects.get(0));
                } catch (Exception e) {
                    throw new EntityPreparationError("Failed to prepare entity", e);
                }
            }
        } else if (referenceMetadata.getRelationType().equals(RelationType.ONE_TO_MANY)) {
            final Object foreignEntity = entityContext.getInstance(referenceMetadata.getForeignTable().getEntityType());
            final BeanWrapper<Object> foreignEntityWrapper = new MethodBeanWrapper<Object>(foreignEntity);
            try {
                foreignEntityWrapper.setPropertyValue(referenceMetadata.getForeignColumn().getPropertyName(), entity);
            } catch (Exception e) {
                throw new EntityPreparationError("Failed to prepare entity properties", e);
            }
            try {
                final Collection<Object> newCollection = ReflectionUtils.getCollection(wrapper.getPropertyType(referenceMetadata.getPropertyName()));
                newCollection.addAll(dataAccess.find(foreignEntity));
                wrapper.setPropertyValue(referenceMetadata.getPropertyName(), newCollection);
            } catch (Exception e) {
                throw new EntityPreparationError("Failed to prepare entity", e);
            }
        } else if (referenceMetadata.getRelationType().equals(RelationType.MANY_TO_ONE)) {
            final ColumnMetadata columnMetadata = with(tableMetadata.getColumns()).find(new ColumnPropertyFilter(referenceMetadata.getPropertyName()));
            final String key = with(map.keySet()).find(new Filter<String>() {
                @Override
                public boolean accepts(String item) {
                    return item.equalsIgnoreCase(columnMetadata.getName());
                }
            });
            if (!map.containsKey(key) || map.get(key) == null) {
                return;
            }
            final Object foreignEntity = entityContext.getInstance(referenceMetadata.getForeignTable().getEntityType());
            if (!columnMetadata.getForeignReference().getDeclaringClass().isInstance(foreignEntity)) {
                return;
            }
            final BeanWrapper<Object> foreignEntityWrapper = new MethodBeanWrapper<Object>(foreignEntity);
            try {
                foreignEntityWrapper.setPropertyValue(columnMetadata.getForeignReference().getPropertyName(), map.get(key));
            } catch (Exception e) {
                throw new EntityPreparationError("Failed to prepare entity properties", e);
            }
            final List<Object> objects = dataAccess.find(foreignEntity);
            if (objects.isEmpty()) {
                return;
            }
            if (objects.size() > 1) {
                throw new EntityPreparationError("More than one entity correspond to one-to-one relationship");
            }
            try {
                wrapper.setPropertyValue(referenceMetadata.getPropertyName(), objects.get(0));
            } catch (Exception e) {
                throw new EntityPreparationError("Failed to prepare entity", e);
            }
        } else if (referenceMetadata.getRelationType().equals(RelationType.MANY_TO_MANY)) {
            final Connection connection = session.getConnection();
            final ManyToManyActionHelper helper = new ManyToManyActionHelper(new DefaultStatementPreparator(false), connection, session.getDatabaseDialect().getStatementBuilderContext(), referenceMetadata.getForeignColumn().getTable(), tableMetadata);
            final ManyToManyMiddleEntity middleEntity = new ManyToManyMiddleEntity();
            final BeanWrapper<ManyToManyMiddleEntity> middleEntityWrapper = new MethodBeanWrapper<ManyToManyMiddleEntity>(middleEntity);
            try {
                middleEntityWrapper.setPropertyValue(referenceMetadata.getForeignColumn().getPropertyName(), entity);
            } catch (Exception e) {
                throw new EntityPreparationError("Failed to prepare entity", e);
            }
            final List<Serializable> list = helper.find(middleEntity);
            helper.close();
            try {
                connection.close();
            } catch (SQLException e) {
                throw new EntityPreparationError("Failed to commit changes to the database", e);
            }
            try {
                wrapper.setPropertyValue(referenceMetadata.getPropertyName(), with(list).transform(new Transformer<Serializable, Object>() {
                    @Override
                    public Object map(Serializable key) {
                        return dataAccess.find(with(referenceMetadata.getForeignTable().getColumns()).find(new NegatingFilter<ColumnMetadata>(new ColumnNameFilter(tableMetadata.getName()))).getForeignReference().getTable().getEntityType(), key);
                    }
                }).list());
            } catch (Exception e) {
                throw new EntityPreparationError("Failed to prepare entity", e);
            }
        }
    }

    @Override
    public void deleteDependentRelations(final E entity, final DataAccess dataAccess) {
        final MethodBeanWrapper<E> wrapper = new MethodBeanWrapper<E>(entity);
        //noinspection unchecked
        with(tableMetadata.getForeignReferences())
                .keep(new Filter<ReferenceMetadata<E, ?>>() {
                    @Override
                    public boolean accepts(ReferenceMetadata<E, ?> item) {
                        return item.getCascadeMetadata().cascadeRemove();
                    }
                })
                .forThose(new Filter<ReferenceMetadata<E, ?>>() {
                              @Override
                              public boolean accepts(ReferenceMetadata<E, ?> item) {
                                  return item.isRelationOwner();
                              }
                          }, new Processor<ReferenceMetadata<E, ?>>() {
                              @Override
                              public void process(ReferenceMetadata<E, ?> reference) {
                                  //many-to-one, owner=here and one-to-one, owner=here
                                  try {
                                      final Object foreignEntity = wrapper.getPropertyValue(reference.getPropertyName());
                                      if (foreignEntity == null) {
                                          return;
                                      }
                                      dataAccess.delete(foreignEntity);
                                  } catch (Exception e) {
                                      throw new EntityDefinitionError("Could not access foreign property", e);
                                  }
                              }
                          }
                );
    }

    @Override
    public void deleteDependencyRelations(final E entity, final DataAccess dataAccess) {
        //noinspection unchecked
        with(tableMetadata.getForeignReferences())
                .keep(new Filter<ReferenceMetadata<E, ?>>() {
                    @Override
                    public boolean accepts(ReferenceMetadata<E, ?> item) {
                        return item.getCascadeMetadata().cascadeRemove();
                    }
                })
                .forThose(new Filter<ReferenceMetadata<E, ?>>() {
                              @Override
                              public boolean accepts(ReferenceMetadata<E, ?> item) {
                                  return !item.isRelationOwner() && item.getRelationType().getLocalCardinality() == 1;
                              }
                          },
                        new Processor<ReferenceMetadata<E, ?>>() {
                            @Override
                            public void process(ReferenceMetadata<E, ?> reference) {
                                //one-to-many, owner=there and one-to-one, owner=there
                                final Object foreignEntity = entityContext.getInstance(reference.getForeignTable().getEntityType());
                                final MethodBeanWrapper<Object> foreignEntityWrapper = new MethodBeanWrapper<Object>(foreignEntity);
                                try {
                                    //noinspection unchecked
                                    final WritePropertyAccessor<Object> writer = (WritePropertyAccessor<Object>) foreignEntityWrapper.getPropertyWriter(reference.getForeignColumn().getPropertyName());
                                    writer.setPropertyValue(entity);
                                } catch (Exception e) {
                                    throw new EntityDefinitionError("Failed to access property", e);
                                }
                                dataAccess.delete(foreignEntity);
                            }
                        }
                );
    }

    @Override
    public void saveDependencyRelations(E entity, final DataAccess dataAccess) {
        final MethodBeanWrapper<E> wrapper = new MethodBeanWrapper<E>(entity);
        //noinspection unchecked
        with(tableMetadata.getForeignReferences())
                .keep(new Filter<ReferenceMetadata<E, ?>>() {
                    @Override
                    public boolean accepts(ReferenceMetadata<E, ?> item) {
                        return item.getCascadeMetadata().cascadePersist() && item.isRelationOwner();
                    }
                })
                .forThose(new Filter<ReferenceMetadata<E, ?>>() {
                              //many-to-one and one-to-one relations where owner=here
                              @Override
                              public boolean accepts(ReferenceMetadata<E, ?> item) {
                                  return item.getRelationType().getForeignCardinality() == 1;
                              }
                          },
                        new Processor<ReferenceMetadata<E, ?>>() {
                            @Override
                            public void process(ReferenceMetadata<E, ?> input) {
                                try {
                                    final Object foreignEntity = wrapper.getPropertyValue(input.getPropertyName());
                                    if (foreignEntity == null) {
                                        return;
                                    }
                                    wrapper.setPropertyValue(input.getPropertyName(), dataAccess.save(foreignEntity));
                                } catch (Exception e) {
                                    throw new EntityDefinitionError("Failed to access property " + input.getLocalTable().getEntityType().getCanonicalName() + "." + input.getPropertyName(), e);
                                }
                            }
                        }
                );
    }

    @Override
    public void saveDependentRelations(final E entity, final DataAccess dataAccess, final EntityContext entityContext) {
        final MethodBeanWrapper<E> wrapper = new MethodBeanWrapper<E>(entity);
        //noinspection unchecked
        with(tableMetadata.getForeignReferences())
                .keep(new Filter<ReferenceMetadata<E, ?>>() {
                    @Override
                    public boolean accepts(ReferenceMetadata<E, ?> item) {
                        return item.getCascadeMetadata().cascadePersist() && !item.isRelationOwner();
                    }
                })
                .forThose(new Filter<ReferenceMetadata<E, ?>>() {
                              //one-to-one relations
                              @Override
                              public boolean accepts(ReferenceMetadata<E, ?> item) {
                                  //one-to-one
                                  return item.getRelationType().getLocalCardinality() == 1 && item.getRelationType().getForeignCardinality() == 1;
                              }
                          },
                        new Processor<ReferenceMetadata<E, ?>>() {
                            @Override
                            public void process(ReferenceMetadata<E, ?> reference) {
                                try {
                                    final Object foreignEntitySample = entityContext.getInstance(reference.getForeignColumn().getTable().getEntityType());
                                    final MethodBeanWrapper<Object> foreignEntitySampleWrapper = new MethodBeanWrapper<Object>(foreignEntitySample);
                                    foreignEntitySampleWrapper.setPropertyValue(reference.getForeignColumn().getPropertyName(), entity);
                                    dataAccess.delete(foreignEntitySample);
                                    final Object foreignEntity = wrapper.getPropertyValue(reference.getPropertyName());
                                    if (foreignEntity == null) {
                                        return;
                                    }
                                    final MethodBeanWrapper<Object> foreignEntityWrapper = new MethodBeanWrapper<Object>(foreignEntity);
                                    foreignEntityWrapper.setPropertyValue(reference.getForeignColumn().getPropertyName(), entity);
                                    wrapper.setPropertyValue(reference.getPropertyName(), dataAccess.save(foreignEntity));
                                } catch (Exception e) {
                                    throw new EntityDefinitionError("Failed to access property " + reference.getLocalTable().getEntityType().getCanonicalName() + "." + reference.getPropertyName(), e);
                                }
                            }
                        }
                )
                .forThose(new Filter<ReferenceMetadata<E, ?>>() {
                              @Override
                              public boolean accepts(ReferenceMetadata<E, ?> item) {
                                  //one-to-many
                                  return item.getRelationType().getLocalCardinality() == 1 && item.getRelationType().getForeignCardinality() > 1;
                              }
                          },
                        new Processor<ReferenceMetadata<E, ?>>() {
                            @Override
                            public void process(ReferenceMetadata<E, ?> reference) {
                                try {
                                    final Object foreignEntitySample = entityContext.getInstance(reference.getForeignColumn().getTable().getEntityType());
                                    final MethodBeanWrapper<Object> foreignEntitySampleWrapper = new MethodBeanWrapper<Object>(foreignEntitySample);
                                    foreignEntitySampleWrapper.setPropertyValue(reference.getForeignColumn().getPropertyName(), entity);
                                    dataAccess.delete(foreignEntitySample);
                                    final Object propertyValue = wrapper.getPropertyValue(reference.getPropertyName());
                                    if (propertyValue == null) {
                                        return;
                                    }
                                    //noinspection unchecked
                                    final Collection<Object> originalCollection = (Collection<Object>) propertyValue;
                                    final Collection<Object> newCollection = ReflectionUtils.getCollection(originalCollection.getClass());
                                    for (Object foreignEntity : originalCollection) {
                                        if (foreignEntity == null) {
                                            newCollection.add(null);
                                            continue;
                                        }
                                        final MethodBeanWrapper<Object> foreignEntityWrapper = new MethodBeanWrapper<Object>(foreignEntity);
                                        foreignEntityWrapper.setPropertyValue(reference.getForeignColumn().getPropertyName(), entity);
                                        newCollection.add(dataAccess.save(foreignEntity));
                                    }
                                    wrapper.setPropertyValue(reference.getPropertyName(), newCollection);
                                } catch (Exception e) {
                                    throw new EntityDefinitionError("Failed to access property " + reference.getLocalTable().getEntityType().getCanonicalName() + "." + reference.getPropertyName(), e);
                                }
                            }
                        }
                )
                .forThose(
                        new Filter<ReferenceMetadata<E, ?>>() {
                            @Override
                            public boolean accepts(ReferenceMetadata<E, ?> item) {
                                return item.getRelationType().getLocalCardinality() > 1 && item.getRelationType().getForeignCardinality() > 1;
                            }
                        },
                        new Processor<ReferenceMetadata<E, ?>>() {
                            @Override
                            public void process(ReferenceMetadata<E, ?> reference) {
                                try {
                                    final Object propertyValue = wrapper.getPropertyValue(reference.getPropertyName());
                                    if (propertyValue == null) {
                                        return;
                                    }
                                    //noinspection unchecked
                                    final Collection<Object> originalCollection = (Collection<Object>) propertyValue;
                                    if (originalCollection.isEmpty()) {
                                        return;
                                    }
                                    final Class<?> collectionType = wrapper.getPropertyType(reference.getPropertyName());
                                    final Collection<Object> resultCollection = ReflectionUtils.getCollection(collectionType);
                                    for (Object foreignEntity : originalCollection) {
                                        if (foreignEntity == null) {
                                            resultCollection.add(null);
                                            continue;
                                        }
                                        resultCollection.add(dataAccess.save(foreignEntity));
                                    }
                                    wrapper.setPropertyValue(reference.getPropertyName(), resultCollection);
                                } catch (Exception e) {
                                    throw new EntityDefinitionError("Failed to cascade save property " + reference.getPropertyName(), e);
                                }
                            }
                        }
                );
    }

    @Override
    public Map<TableMetadata<?>, Set<ManyToManyMiddleEntity>> getManyToManyRelatedObjects(final E entity) {
        final HashMap<TableMetadata<?>, Set<ManyToManyMiddleEntity>> map = new HashMap<TableMetadata<?>, Set<ManyToManyMiddleEntity>>();
        final BeanAccessor<E> accessor = new MethodBeanAccessor<E>(entity);
        //noinspection unchecked
        with(tableMetadata.getForeignReferences())
                .keep(new Filter<ReferenceMetadata<E, ?>>() {
                    @Override
                    public boolean accepts(ReferenceMetadata<E, ?> item) {
                        return item.getCascadeMetadata().cascadePersist() && RelationType.MANY_TO_MANY.equals(item.getRelationType());
                    }
                })
                .each(new Processor<ReferenceMetadata<E, ?>>() {
                    @Override
                    public void process(ReferenceMetadata<E, ?> input) {
                        final TableMetadata<?> foreignTable = input.getForeignTable();
                        try {
                            Object propertyValue = accessor.getPropertyValue(input.getPropertyName());
                            if (propertyValue == null) {
                                propertyValue = Collections.emptyList();
                            }
                            //noinspection unchecked
                            final Collection<Object> collection = (Collection<Object>) propertyValue;
                            final ColumnNameFilter columnNameFilter = new ColumnNameFilter(tableMetadata.getName());
                            if (collection.isEmpty()) {
                                final ManyToManyMiddleEntity middleEntity = new ManyToManyMiddleEntity();
                                final BeanWrapper<ManyToManyMiddleEntity> wrapper = new MethodBeanWrapper<ManyToManyMiddleEntity>(middleEntity);
                                wrapper.setPropertyValue(with(foreignTable.getColumns()).find(columnNameFilter).getPropertyName(), entity);
                                map.put(foreignTable, new HashSet<ManyToManyMiddleEntity>(Arrays.asList(middleEntity)));
                                return;
                            }
                            final Set<ManyToManyMiddleEntity> entities = new HashSet<ManyToManyMiddleEntity>();
                            for (Object item : collection) {
                                final ManyToManyMiddleEntity middleEntity = new ManyToManyMiddleEntity();
                                final BeanWrapper<ManyToManyMiddleEntity> wrapper = new MethodBeanWrapper<ManyToManyMiddleEntity>(middleEntity);
                                wrapper.setPropertyValue(with(foreignTable.getColumns()).find(columnNameFilter).getPropertyName(), entity);
                                wrapper.setPropertyValue(with(foreignTable.getColumns()).find(new NegatingFilter<ColumnMetadata>(columnNameFilter)).getPropertyName(), item);
                                entities.add(middleEntity);
                            }
                            map.put(foreignTable, entities);
                        } catch (Exception e) {
                            throw new UnsuccessfulOperationError("Failed to determine intermediary relation", e);
                        }
                    }
                });
        return map;
    }

}
