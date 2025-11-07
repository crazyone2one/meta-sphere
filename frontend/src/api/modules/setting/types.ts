import type {AuthScopeEnumType} from "/@/enums/common-enum.ts";

export interface UserGroupItem {
    // 组ID
    id: string;
    // 组名称
    name: string;
    code: string;
    // 组描述
    description: string;
    // 是否是内置用户组
    internal: true;
    // 所属类型
    type: AuthScopeEnumType;
    createTime: number;
    updateTime: number;
    // 创建人
    createUser: string;
    // 应用范围
    scopeId: string;
    // 自定义排序
    pos: number;
}

export interface CurrentUserGroupItem {
    // 组ID
    id: string;
    // 组名称
    name: string;
    code: string;
    // 所属类型
    type: AuthScopeEnumType;
    // 是否是内置用户组
    internal: boolean;
    // 组ID
    scopeId?: string;
}

export interface UserGroupParams {
    id?: string; // 组ID
    name?: string; // 名称
    code?: string; // 名称
    scopeId?: string; // 组织ID
    type?: string; // 组类型：SYSTEM | PROJECT | ORGANIZATION
}

export interface UserGroupPermissionItem {
    id: string;
    name: string;
    enable: boolean;
    license: boolean;
}

export interface AuthTableItem {
    id: string;
    name?: string;
    enable: boolean;
    license: boolean;
    ability?: string | undefined;
    permissions?: UserGroupPermissionItem[];
    // 对应表格权限的复选框组的绑定值
    perChecked?: string[];
    operationObject?: string;
    rowSpan?: number;
    indeterminate?: boolean;
}

export type AuthScopeType =
    | 'SYSTEM'
    | 'PROJECT'
    | 'ORGANIZATION'
    | 'WORKSTATION'
    | 'TEST_PLAN'
    | 'BUG_MANAGEMENT'
    | 'CASE_MANAGEMENT'
    | 'API_TEST'
    | 'UI_TEST'
    | 'LOAD_TEST'
    | 'PERSONAL';

export interface UserGroupAuthSetting {
    // 菜单项ID
    id: AuthScopeType;
    // 菜单所属类型
    type?: string;
    // 菜单项名称
    name: string;
    // 是否企业版
    license: boolean;
    // 是否全选
    enable: boolean;
    // 菜单下的权限列表
    permissions?: UserGroupPermissionItem[];
    // 子菜单
    children?: UserGroupAuthSetting[];
    perChecked?: string[];
    indeterminate?: boolean;
}
export interface SavePermissions {
    id: string;
    enable: boolean;
}
export interface SaveGlobalUSettingData {
    userRoleId: string;
    permissions: SavePermissions[];
}