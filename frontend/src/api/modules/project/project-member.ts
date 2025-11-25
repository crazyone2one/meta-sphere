import {get} from "/@/api";
import type {SelectOption} from "naive-ui";

export const projectMemberApis = {
    // 获取用户组下拉
    getProjectUserGroup: (projectId: string) => get<Array<SelectOption>>(`/project/member/get-role/option/${projectId}`),
}