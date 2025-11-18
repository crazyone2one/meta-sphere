import {get, post} from "/@/api";
import type {ITableQueryParams} from "/@/api/types.ts";

export const systemProjectApi = {
    /**
     * 获取用户组列表
     */
    postProjectTable: (data: ITableQueryParams) => post('/system-project/page', data, {cacheFor: 0}),
    deleteProject: (id: string) => get(`/system-project/remove/${id}`),
    enableOrDisableProject: (id: string, isEnable = true) => get(`${isEnable ? 'system-project/enable/' : 'system-project/disable/'}${id}`),
    renameProject: (data: { id: string; name: string; organizationId: string }) => post(`/system-project/rename`, data),
}