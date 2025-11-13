export type SystemScopeType = 'PROJECT' | 'ORGANIZATION' | 'SYSTEM';

export interface AuthenticationResponse {
    accessToken: string;
    refreshToken: string;
}

export interface UserRole {
    createTime: number;
    updateTime: number;
    createUser: string;
    description?: string;
    id: string;
    name: string;
    code: string;
    scopeId: string; // 项目/组织/系统 id
    type: SystemScopeType;
}

export interface permissionsItem {
    id: string;
    permissionId: string;
    roleId: string;
}

export interface UserRolePermissions {
    userRole: UserRole;
    userRolePermissions: permissionsItem[];
}

export interface UserRoleRelation {
    id: string;
    userId: string;
    roleId: string;
    sourceId: string;
    organizationId: string;
    createTime: number;
    createUser: string;
    userRolePermissions: permissionsItem[];
    userRole: UserRole;
}

export interface UserState {
    id: string;
    name: string;
    email: string;
    phone: string;
    enable?: boolean;
    lastOrganizationId?: string;
    lastProjectId?: string;
    avatar?: string;
    userGroup?: string[];
    userRolePermissions?: UserRolePermissions[];
    userRoles?: UserRole[];
    userRoleRelations?: UserRoleRelation[];
}