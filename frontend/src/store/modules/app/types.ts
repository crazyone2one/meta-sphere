import type {IProjectListItem} from "/@/api/modules/project/types.ts";
import type {RouteRecordRaw} from "vue-router";

export interface AppState {
    currentOrgId: string;
    currentProjectId: string;
    innerHeight: number;
    menuCollapse: boolean; // 菜单是否折叠
    loading: boolean
    loadingTip: string
    projectList: IProjectListItem[];
    topMenus: RouteRecordRaw[];
    currentTopMenu: RouteRecordRaw;
    currentMenuConfig: string[];
    orgList: { id: string; name: string }[];
}