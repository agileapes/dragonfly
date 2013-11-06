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

import com.agileapes.dragonfly.data.BatchOperation;
import com.agileapes.dragonfly.data.DataAccess;
import com.agileapes.dragonfly.sample.entities.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/25, 0:21)
 */
@Service
public class BatchService {

    public static final int BENCHMARK_SIZE = 10000;

    @Autowired
    private DataAccess dataAccess;

    public void execute() {
        final StopWatch stopWatch = new StopWatch("Batch Benchmark, size: " + BENCHMARK_SIZE);
        stopWatch.start("batch");
        //0.00025
        dataAccess.run(new BatchOperation() {
            @Override
            public void execute(DataAccess dataAccess) {
                final Group group = getGroup();
                for (int i = 0; i < BENCHMARK_SIZE; i++) {
                    dataAccess.save(group);
                }
            }
        });
        stopWatch.stop();
        dataAccess.delete(getGroup());
        stopWatch.start("normal");
        //0.0084862 (x33)
        for (int i = 0; i < BENCHMARK_SIZE; i ++) {
            dataAccess.save(getGroup());
        }
        stopWatch.stop();
        dataAccess.delete(getGroup());
        System.out.println(stopWatch.prettyPrint());
    }

    private Group getGroup() {
        final Group group = new Group();
        group.setName("This Group");
        return group;
    }

}
