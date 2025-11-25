package com.master.meta.handle.result;

/**
 * @author Created by 11's papa on 2025/10/24
 */
public enum SystemResultCode implements IResultCode {
    /**
     * 调用获取全局用户组接口，如果操作的是非全局的用户组，会返回该响应码
     */
    GLOBAL_USER_ROLE_PERMISSION(101001, "global_user_role_permission_error"),
    GLOBAL_USER_ROLE_EXIST(101002, "global_user_role_exist_error"),
    GLOBAL_USER_ROLE_RELATION_SYSTEM_PERMISSION(101003, "global_user_role_relation_system_permission_error"),
    GLOBAL_USER_ROLE_LIMIT(101004, "global_user_role_limit_error"),
    INVITE_EMAIL_EXIST(101513, "user_email_already_exists"),
    /**
     * 开启组织模板，操作项目模板时，会返回
     */
    PROJECT_TEMPLATE_PERMISSION(102002, "project_template_permission_error"),
    CUSTOM_FIELD_EXIST(100012, "custom_field.exist"),
    TEMPLATE_SCENE_ILLEGAL(100010, "template_scene_illegal_error"),
    TEMPLATE_EXIST(100013, "template.exist"),
    ;
    private final int code;
    private final String message;

    SystemResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return getTranslationMessage(this.message);
    }
}
