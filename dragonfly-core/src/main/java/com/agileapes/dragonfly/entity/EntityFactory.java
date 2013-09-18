package com.agileapes.dragonfly.entity;

import com.agileapes.dragonfly.entity.impl.EntityProxy;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/14, 14:21)
 */
public interface EntityFactory<E> {

    E getInstance(EntityProxy<E> proxy);

}