import {get, post, del} from "/@/api";
import type {BatchActionQueryParams, IPageResponse, ITableQueryParams} from "/@/api/types.ts";
import type {
    CreateUserParams,
    SystemRole,
    UpdateUserInfoParams,
    UpdateUserStatusParams,
    UserListItem
} from "/@/api/modules/user/types.ts";

export const userApi = {
    // 获取用户列表
    getUserPage: (data: ITableQueryParams) => post<IPageResponse<UserListItem>>('/system-user/page', data),
    // 获取系统用户组
    getSystemRoles: () => get<SystemRole[]>('/system-user/get/global/system/role'),
    // 批量创建用户
    batchCreateUser: (data: CreateUserParams) => post('/system-user/save', data),
    // 更新用户信息
    updateUserInfo: (data: UpdateUserInfoParams) => post('/system-user/update', data),
    // 更新用户启用/禁用状态
    toggleUserStatus: (data: UpdateUserStatusParams) => post('/system-user/update/enable', data),
    // 重置用户密码
    resetUserPassword: (data: BatchActionQueryParams) => post('/system-user/reset/password', data),
    // 删除用户
    deleteUserInfo: (id: string) => del(`/system-user/remove/${id}`),
}