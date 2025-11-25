import { get, post } from "/@/api";
import type { AddOrUpdateField } from "/@/api/modules/setting/template/types.ts";
import type { ITableQueryParams } from "/@/api/types.ts";

export const templateApi = {
    // 创建自定义字段(组织)
    addOrUpdateProjectField: (data: AddOrUpdateField) => {
        if (data.id) {
            return post<AddOrUpdateField>('/project/custom/field/update', data)
        }
        return post<AddOrUpdateField>('/project/custom/field/save', data)
    },
    // 获取自定义字段列表(组织)
    getProjectFieldList: (params: ITableQueryParams) => get(`/project/custom/field/list/${params.scopedId}/${params.scene}`),
    // 获取模板列表的状态(组织)
    getOrgTemplate: (scopedId: string) => get<Record<string, boolean>>(`/organization/template/enable/config/${scopedId}`),
    // 获取模板列表的状态(项目)
    getProTemplate: (scopedId: string) => get<Record<string, boolean>>(`/project/template/enable/config/${scopedId}`),

}