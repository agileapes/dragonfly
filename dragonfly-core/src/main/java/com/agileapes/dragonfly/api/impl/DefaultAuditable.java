package com.agileapes.dragonfly.api.impl;

import com.agileapes.dragonfly.api.Auditable;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/9/8, 21:09)
 */
public class DefaultAuditable implements Auditable {

    private String updateUser;

    @Override
    public String getUpdateUser() {
        return updateUser;
    }

    @Override
    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

}