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

package com.agileapes.dragonfly.sample.service;

import com.agileapes.dragonfly.data.DataAccess;
import com.agileapes.dragonfly.data.impl.Reference;
import com.agileapes.dragonfly.sample.entities.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/22, 23:51)
 */
@Service
public class CountPeopleProcedureService {

    @Autowired
    private DataAccess dataAccess;

    public void execute() {
        final Reference<Long> count = new Reference<Long>();
        dataAccess.call(Person.class, "countPeople", count);
        System.out.println("count.getValue() = " + count.getValue());
    }

}
