import {get, post} from "/@/api";
import type {AddOrUpdateField, DefinedFieldItem, SceneType} from "/@/api/modules/setting/template/types.ts";
import type {IPageResponse, ITableQueryParams} from "/@/api/types.ts";

export const templateApi = {
    // 创建自定义字段(组织)
    addOrUpdateProjectField: (data: AddOrUpdateField) => {
        if (data.id) {
            return post<AddOrUpdateField>('/project/custom/field/update', data)
        }
        return post<AddOrUpdateField>('/project/custom/field/save', data)
    },
    addOrUpdateOrdField: (data: AddOrUpdateField) => {
        if (data.id) {
            return post<AddOrUpdateField>('/organization/custom/field/update', data)
        }
        return post<AddOrUpdateField>('/organization/custom/field/add', data)
    },
    // 获取自定义字段列表(组织)
    getProjectFieldList: (params: ITableQueryParams) => get<DefinedFieldItem[]>(`/project/custom/field/list/${params.scopedId}/${params.scene}`),
    // 获取自定义字段详情选项(组织)
    getOrgFieldDetail: (id: string) => get<DefinedFieldItem>(`/organization/custom/field/get/${id}`),
    getProjectFieldDetail: (id: string) => get<DefinedFieldItem>(`/project/custom/field/get/${id}`),
    getProjectFieldPage: (params: ITableQueryParams) => post<IPageResponse<DefinedFieldItem>>(`/project/custom/field/page`, params),
    getOrgFieldPage: (params: ITableQueryParams) => post<IPageResponse<DefinedFieldItem>>(`/organization/custom/field/page`, params),
    // 获取模板列表的状态(组织)
    getOrgTemplate: (scopedId: string) => get<Record<string, boolean>>(`/organization/template/enable/config/${scopedId}`, {}, {cacheFor: 0}),
    // 获取模板列表的状态(项目)
    getProTemplate: (scopedId: string) => get<Record<string, boolean>>(`/project/template/enable/config/${scopedId}`, {}, {cacheFor: 0}),
    // 关闭组织模板||开启项目模板
    enableOrOffTemplate: (organizationId: string, scene: SceneType) => get(`/organization/template/disable/${organizationId}/${scene}`),

}