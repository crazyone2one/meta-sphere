import {defineStore} from "pinia";
import type {AppState} from "/@/store/modules/app/types.ts";
import {projectApis} from "/@/api/modules/project";
import type {IProjectListItem} from "/@/api/modules/project/types.ts";
import type {RouteRecordRaw} from "vue-router";
import {cloneDeep} from "lodash-es";
import {featureRouteMap} from "/@/router/constants.ts";

const useAppStore = defineStore('app', {
    state: (): AppState => ({
        currentOrgId: '',
        currentProjectId: '',
        innerHeight: 0,
        menuCollapse: false,
        loading: false,
        loadingTip: '你不知道你有多幸运...',
        projectList: [],
        topMenus: [],
        currentTopMenu: {} as RouteRecordRaw,
        currentMenuConfig: Object.keys(featureRouteMap)
    }),

    getters: {
        getCurrentOrgId(state: AppState): string {
            return state.currentOrgId;
        },
        getCurrentProjectId(state: AppState): string {
            return state.currentProjectId;
        },
        getProjectList(state: AppState): Array<IProjectListItem> {
            return state.projectList;
        },
        getMenuCollapse(state: AppState): boolean {
            return state.menuCollapse;
        },
        getTopMenus(state: AppState): RouteRecordRaw[] {
            return state.topMenus;
        },
        getCurrentTopMenu(state: AppState): RouteRecordRaw {
            return state.currentTopMenu;
        },
    },

    actions: {
        setCurrentOrgId(id: string) {
            this.currentOrgId = id;
        },
        setCurrentProjectId(id: string) {
            this.currentProjectId = id;
        },
        setMenuCollapse(collapse: boolean) {
            this.menuCollapse = collapse;
        },
        showLoading(tip = '') {
            this.loading = true;
            this.loadingTip = tip || '你不知道你有多幸运...'
        },
        hideLoading() {
            this.loading = false
        },
        // 重置用户信息
        resetInfo() {
            this.$reset();
        },
        async initProjectList() {
            try {
                if (this.currentOrgId) {
                    const res = await projectApis.getProjectList(this.getCurrentOrgId);
                    this.projectList = res;
                } else {
                    this.projectList = [];
                }
            } catch (error) {
                console.log(error);
            }
        },
        setTopMenus(menus: RouteRecordRaw[] | undefined) {
            this.topMenus = menus ? [...menus] : [];
        },
        setCurrentTopMenu(menu: RouteRecordRaw) {
            this.currentTopMenu = cloneDeep(menu);
        },
        async setCurrentMenuConfig(menuConfig: string[]) {
            this.currentMenuConfig = menuConfig;
        },
    },
    persist: true
})
export default useAppStore