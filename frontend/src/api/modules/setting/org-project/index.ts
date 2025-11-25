import {get, post} from "/@/api";
import type {IPageResponse, ITableQueryParams} from "/@/api/types.ts";
import type {
    AddUserToOrgOrProjectParams,
    OrgProjectTableItem,
    SystemOrgOption
} from "/@/api/modules/setting/org-project/types.ts";
import type {SelectOption} from "naive-ui";

export interface SystemGetUserByOrgOrProjectIdParams extends ITableQueryParams {
    projectId?: string;
    organizationId?: string;
}

export const orgAndProjectApi = {
    /**
     * 获取用户组列表
     */
    postProjectTable: (data: ITableQueryParams) => post<IPageResponse<OrgProjectTableItem>>('/system-project/page', data, {cacheFor: 0}),
    // 根据 orgId 或 projectId 获取用户列表
    postUserPageByOrgIdOrProjectId: (data: SystemGetUserByOrgOrProjectIdParams) =>
        post<IPageResponse<OrgProjectTableItem>>(data.organizationId ? '/system/organization/list-member' : '/system-project/member-list', data),

    createOrUpdateProject: (data: Partial<OrgProjectTableItem>) => post(data.id ? '/system-project/update' : '/system-project/save', data),
    deleteProject: (id: string) => get(`/system-project/remove/${id}`),
    enableOrDisableProject: (id: string, isEnable = true) => get(`${isEnable ? 'system-project/enable/' : 'system-project/disable/'}${id}`),
    renameProject: (data: { id: string; name: string; organizationId: string }) => post(`/system-project/rename`, data),
    //  获取组织下拉选项
    getSystemOrgOption: () => get<Array<SystemOrgOption>>(`/system/organization/list`),
    // 系统-获取管理员下拉选项
    getAdminByOrganizationOrProject: (keyword: string) => get<Array<SelectOption>>(`/system-project/user-list`, {keyword}),
    // 删除组织或项目成员
    deleteUserFromOrgOrProject: (sourceId: string, userId: string, isOrg = true) =>
        get(isOrg ? `/system/organization/remove-member/${sourceId}/${userId}` : `/system-project/remove-member/${sourceId}/${userId}`, {}, {cacheFor: 0}),
    // 给组织或项目添加成员
    addUserToOrgOrProject: (data: AddUserToOrgOrProjectParams) => post(data.projectId ? '/system-project/add-member' : `/system/organization/add-member`, data),
    // 组织-添加项目成员
    addProjectMemberByOrg: (data: AddUserToOrgOrProjectParams) => post(`/organization/project/add-members`, data),
    // 获取项目和组织的总数
    getOrgAndProjectCount: () => get<{ projectTotal: number; organizationTotal: number }>(`/system/organization/total`),
    revokeDeleteProject: (id: string) => get(`/system-project/revoke/${id}`),
}