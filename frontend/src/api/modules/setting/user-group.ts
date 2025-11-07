import {get, post} from "/@/api";
import type {
    SaveGlobalUSettingData,
    UserGroupAuthSetting,
    UserGroupItem,
    UserGroupParams
} from "/@/api/modules/setting/types.ts";
import type {SelectOption} from "naive-ui";

export const userGroupApi = {
    /**
     * 获取用户组列表
     */
    getUserGroupList: () => get<Array<UserGroupItem>>('/user/role/global/list', {}, {cacheFor: 0}),
    // 创建或修改用户组
    updateOrAddUserGroup: (data: UserGroupParams) => post<UserGroupItem>(data.id ? `/user/role/global/update` : '/user/role/global/save', data),
    saveGlobalUSetting: (data: SaveGlobalUSettingData) => post<UserGroupAuthSetting[]>('/user/role/global/permission/update', data),
    // 删除用户组
    removeUserGroup: (id: string) => get(`/user/role/global/remove/${id}`),
    // 获取用户组对应的权限配置
    getGlobalUSetting: (id: string) => get<Array<UserGroupAuthSetting>>(`/user/role/global/permission/setting/${id}`, {}, {cacheFor: 0}),
    //系统-获取需要关联的用户选项
    getSystemUserGroupOption: (id: string, keyword: string) => get<Array<SelectOption>>(`/user/role/relation/global/user/option/${id}`, {keyword}, {cacheFor: 0}),
    //系统-添加用户到用户组
    addUserToUserGroup: (data: { roleId: string; userIds: string[] }) => post('/user/role/relation/global/save', data),
    // 组织-添加用户到用户组
    addOrgUserToUserGroup: (data: {
        roleId: string;
        userIds: string[];
        organizationId: string
    }) => post('/user/role/organization/add-member', data),
}