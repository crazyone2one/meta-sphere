import {userGroupApi} from "/@/api/modules/setting/user-group.ts";
import {orgAndProjectApi} from "/@/api/modules/setting/org-project";

export const UserRequestTypeEnum = {
    SYSTEM_USER_GROUP: 'SYSTEM_USER_GROUP',
    SYSTEM_ORGANIZATION: 'SYSTEM_ORGANIZATION',
    SYSTEM_ORGANIZATION_ADMIN: 'SYSTEM_ORGANIZATION_ADMIN',
    SYSTEM_PROJECT: 'SYSTEM_PROJECT',
    SYSTEM_PROJECT_ADMIN: 'SYSTEM_PROJECT_ADMIN',
    ORGANIZATION_USER_GROUP: 'ORGANIZATION_USER_GROUP',
    ORGANIZATION_USER_GROUP_ADMIN: 'ORGANIZATION_USER_GROUP_ADMIN',
    ORGANIZATION_PROJECT: 'ORGANIZATION_PROJECT',
    ORGANIZATION_PROJECT_ADMIN: 'ORGANIZATION_PROJECT_ADMIN',
    SYSTEM_ORGANIZATION_PROJECT: 'SYSTEM_ORGANIZATION_PROJECT',
    SYSTEM_ORGANIZATION_MEMBER: 'SYSTEM_ORGANIZATION_MEMBER',
    PROJECT_PERMISSION_MEMBER: 'PROJECT_PERMISSION_MEMBER',
    PROJECT_USER_GROUP: 'PROJECT_USER_GROUP',
    SYSTEM_ORGANIZATION_LIST: 'SYSTEM_ORGANIZATION_LIST',
    SYSTEM_PROJECT_LIST: 'SYSTEM_PROJECT_LIST',
    EXECUTE_USER: 'EXECUTE_USER',
} as const;

export type UserRequestTypeEnum = typeof UserRequestTypeEnum[keyof typeof UserRequestTypeEnum];
export default function initOptionsFunc(type: string, params: Record<string, any>) {
    if (type === UserRequestTypeEnum.SYSTEM_USER_GROUP) {
        return userGroupApi.getSystemUserGroupOption(params.roleId, params.keyword);
    }
    if (type === UserRequestTypeEnum.SYSTEM_ORGANIZATION_ADMIN || type === UserRequestTypeEnum.SYSTEM_PROJECT_ADMIN) {
        // 系统 - 【组织 或 项目】-添加管理员-下拉选项
        return orgAndProjectApi.getAdminByOrganizationOrProject(params.keyword);
    }
}