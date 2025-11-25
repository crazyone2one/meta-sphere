import {get, post} from "/@/api";
import type {IPageResponse, ITableQueryParams} from "/@/api/types.ts";
import type {UserListItem} from "/@/api/modules/user/types.ts";
import type {SelectOption} from "naive-ui";

export const memberApi = {
    /**
     * 系统设置-系统-组织与项目-获取添加成员列表
     */
    getSystemMemberListPage: (data: ITableQueryParams) =>
        post<IPageResponse<UserListItem>>('/system/organization/member-list', data, {cacheFor: 0}),
    // 系统设置-组织-项目-分页获取成员列表
    getOrganizationMemberListPage: (data: ITableQueryParams) =>
        post<IPageResponse<UserListItem>>('organization/project/user-list', data, {cacheFor: 0}),
    getGlobalUserGroup: (organizationId: string) => get<Array<SelectOption>>(`/organization/user/role/list/${organizationId}`),
};