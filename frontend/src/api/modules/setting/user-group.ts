import {get, post} from "/@/api";
import type {
    SaveGlobalUSettingData,
    UserGroupAuthSetting,
    UserGroupItem,
    UserGroupParams
} from "/@/api/modules/setting/types.ts";
import type {SelectOption} from "naive-ui";
import type {IPageResponse, ITableQueryParams} from "/@/api/types.ts";
import type {SimpleUserInfo} from "/@/api/modules/user/types.ts";

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
    addUserToUserGroup: (data: { code: string; userIds: string[] }) => post('/user/role/relation/global/save', data),
    // 组织-添加用户到用户组
    addOrgUserToUserGroup: (data: {
        code: string;
        userIds: string[];
        organizationId: string
    }) => post('/user/role/organization/add-member', data),
    // 系统-获取用户组对应的用户列表
    fetchUserByUserGroup: (data: ITableQueryParams) => post<IPageResponse<SimpleUserInfo>>('/user/role/relation/global/page', data),
    // 系统-删除用户组对应的用户
    deleteUserFromUserGroup: (id: string) => get(`/user/role/relation/global/delete/${id}`),
}