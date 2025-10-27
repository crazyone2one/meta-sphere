package com.master.meta.handle.result;

/**
 * @author Created by 11's papa on 2025/10/24
 */
public enum CommonResultCode implements IResultCode {
    USER_ROLE_RELATION_REMOVE_ADMIN_USER_PERMISSION(100004, "user_role_relation_remove_admin_user_permission_error"),
    INTERNAL_USER_ROLE_PERMISSION(100003, "internal_user_role_permission_error"),
    ADMIN_USER_ROLE_PERMISSION(100019, "internal_admin_user_role_permission_error");
    ;

    private final int code;
    private final String message;

    CommonResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
