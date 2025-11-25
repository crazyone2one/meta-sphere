export interface CreateOrUpdateSystemProjectParams {
    id?: string;
    // 项目名称
    name: string;
    num: string;
    // 项目描述
    description: string;
    // 启用或禁用
    enable: boolean;
    // 项目成员
    userIds: string[];
    // 模块配置
    moduleIds?: string[];
    // 所属组织
    organizationId?: string;
    // 资源池
    resourcePoolIds: string[];
    // 列表里的
    allResourcePool: boolean; // 默认全部资源池
}

export interface OrgProjectTableItem {
    id: string;
    name: string;
    description: string;
    enable: boolean;
    adminList: UserItem[];
    organizationId: string;
    organizationName: string;
    num: string;
    updateTime: number;
    createTime: number;
    memberCount: number;
    userIds: string[];
    resourcePoolIds: string[];
    orgAdmins: Record<string, any>;
    moduleIds: string[];
    resourcePoolList: string[];
    allResourcePool: boolean;
    createUser: string;
    projectCreateUserIsAdmin: boolean;
}

export interface SystemOrgOption {
    id: string;
    name: string;
}
export interface AddUserToOrgOrProjectParams {
    userIds?: string[];
    organizationId?: string;
    projectId?: string;
    // 等待接口改动 将要废弃，以后用userIds
    memberIds?: string[];
    userRoleIds?: string[];
}
export interface UserItem {
    id: string;
    name: string;
}