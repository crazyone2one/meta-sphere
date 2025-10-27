package com.master.meta.constants;

import lombok.Getter;

/**
 * @author Created by 11's papa on 2025/10/24
 */
@Getter
public enum InternalUserRole {
    ADMIN("admin"),
    MEMBER("member"),
    ORG_ADMIN("org_admin"),
    ORG_MEMBER("org_member"),
    PROJECT_ADMIN("project_admin"),
    PROJECT_MEMBER("project_member");

    private final String value;

    InternalUserRole(String value) {
        this.value = value;
    }

}
