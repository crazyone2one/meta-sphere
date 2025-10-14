import {get, post} from "/@/api";
import type {IProjectListItem} from "/@/api/modules/project/types.ts";

export const projectApis = {
    getProjectList: (organizationId: string) => get<Array<IProjectListItem>>(`/system-project/list/options/${organizationId}`, {}, {cacheFor: 0}),
    switchProject: (data: { projectId: string, userId: string }) => post('/system-project/switch', data),
}