import {get, post} from "/@/api";
import type {
    SaveGlobalUSettingData,
    UserGroupAuthSetting,
    UserGroupItem,
    UserGroupParams
} from "/@/api/modules/setting/types.ts";

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
}