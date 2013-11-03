package com.agileapes.dragonfly.sample.ext;

import com.agileapes.dragonfly.events.EventHandlerContext;
import com.agileapes.dragonfly.events.EventHandlerContextPostProcessor;
import com.agileapes.dragonfly.sample.user.UserContext;
import com.agileapes.dragonfly.sample.user.UserContextAware;

import java.util.concurrent.Semaphore;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/13, 12:59)
 */
public class AuditInterceptor implements EventHandlerContextPostProcessor, UserContextAware {

    private final Semaphore mutex = new Semaphore(1);
    private UserContext userContext;

    public AuditInterceptor() {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            throw new Error("Failed to acquire the lock", e);
        }
    }

    @Override
    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
        mutex.release();
    }

    @Override
    public void postProcessEventHandlerContext(EventHandlerContext eventHandlerContext) {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            throw new Error("Failed to acquire the lock", e);
        }
        eventHandlerContext.addHandler(new AuditEventHandler(userContext));
        mutex.release();
    }

}