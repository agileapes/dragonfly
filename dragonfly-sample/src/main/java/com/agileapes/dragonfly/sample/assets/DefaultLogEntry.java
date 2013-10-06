package com.agileapes.dragonfly.sample.assets;

import com.agileapes.dragonfly.data.DataOperation;
import org.springframework.util.StopWatch;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/10/5, 10:50)
 */
public class DefaultLogEntry implements LogEntry {

    private final DataOperation operation;
    private final StopWatch stopWatch;
    private long time;

    public DefaultLogEntry(DataOperation operation) {
        this.operation = operation;
        stopWatch = new StopWatch();
        stopWatch.start();
    }

    public void stop() {
        stopWatch.stop();
        time = stopWatch.getTotalTimeMillis();
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public DataOperation getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return String.format("[%5dms] %s", getTime(), getOperation());
    }

}
