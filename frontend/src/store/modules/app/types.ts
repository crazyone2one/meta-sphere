import type {IProjectListItem} from "/@/api/modules/project/types.ts";

export interface AppState {
    currentOrgId: string;
    currentProjectId: string;
    innerHeight: number;
    menuCollapse: boolean; // 菜单是否折叠
    loading: boolean
    loadingTip: string
    projectList: IProjectListItem[];
}