import type {SystemScopeType, UserRole, UserRoleRelation} from "/@/api/modules/auth/types.ts";
import {useAppStore, useUserStore} from "/@/store";
import type {RouteLocationNormalized, RouteRecordRaw} from "vue-router";

export const composePermissions = (userRoleRelations: UserRoleRelation[], type: SystemScopeType, id: string) => {
    // 系统级别的权限
    if (type === 'SYSTEM') {
        return userRoleRelations
            .filter((ur) => ur.userRole && ur.userRole.type === 'SYSTEM')
            .flatMap((role) => role.userRolePermissions)
            .map((g) => g.permissionId);
    }
    // 项目和组织级别的权限
    let func: (role: UserRole) => boolean;
    switch (type) {
        case 'PROJECT':
            func = (role) => role && role.type === 'PROJECT';
            break;
        case 'ORGANIZATION':
            func = (role) => role && role.type === 'ORGANIZATION';
            break;
        default:
            func = (role) => role && role.type === 'SYSTEM';
            break;
    }

    return userRoleRelations
        .filter((ur) => func(ur.userRole))
        .filter((ur) => ur.sourceId === id)
        .flatMap((role) => role.userRolePermissions)
        .map((g) => g.permissionId);
}
export const hasPermission = (permission: string, typeList: string[]) => {
    const userStore = useUserStore();
    if (userStore.isAdmin) {
        return true;
    }
    const {projectPermissions, orgPermissions, systemPermissions} = userStore.currentRole;

    if (projectPermissions.length === 0 && orgPermissions.length === 0 && systemPermissions.length === 0) {
        return false;
    }

    if (typeList.includes('PROJECT') && projectPermissions.includes(permission)) {
        return true;
    }
    if (typeList.includes('ORGANIZATION') && orgPermissions.includes(permission)) {
        return true;
    }
    if (typeList.includes('SYSTEM') && systemPermissions.includes(permission)) {
        return true;
    }
    return false;
}
export const hasAnyPermission = (permissions: string[], typeList = ['PROJECT', 'ORGANIZATION', 'SYSTEM']) => {
    if (!permissions || permissions.length === 0) {
        return true;
    }
    return permissions.some((permission) => hasPermission(permission, typeList));
}
export const hasAllPermission = (permissions: string[], typeList = ['PROJECT', 'ORGANIZATION', 'SYSTEM']) => {
    if (!permissions || permissions.length === 0) {
        return true;
    }
    return permissions.every((permission) => hasPermission(permission, typeList));
}

export const topLevelMenuHasPermission = (route: RouteLocationNormalized | RouteRecordRaw) => {
    const userStore = useUserStore();
    const appStore = useAppStore();
    const {currentMenuConfig} = appStore;

    if (userStore.lastProjectId === 'no_such_project' || userStore.lastProjectId === '') {
        // 项目不存在, 不显示任何项目级别菜单, 展示无资源页面
        return false;
    }

    if (currentMenuConfig.length && !currentMenuConfig.includes(route.name as string)) {
        // 没有配置的菜单不显示
        return false;
    }
    if (userStore.isAdmin) {
        // 如果是系统管理员, 包含项目, 组织, 系统层级所有菜单权限
        return true;
    }
    return hasAnyPermission(route.meta?.roles as string[] || []);
}