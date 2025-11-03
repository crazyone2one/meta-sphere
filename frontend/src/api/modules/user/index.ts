import {get, post} from "/@/api";
import type {IPageResponse, ITableQueryParams} from "/@/api/types.ts";
import type {SystemRole, UserListItem} from "/@/api/modules/user/types.ts";

export const userApi = {
    // 获取用户列表
    getUserPage: (data: ITableQueryParams) => post<IPageResponse<UserListItem>>('/system-user/page', data),
    // 获取系统用户组
    getSystemRoles: () => get<SystemRole[]>('/system-user/get/global/system/role'),
}