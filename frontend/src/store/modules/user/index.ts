import {defineStore} from "pinia";
import type {UserState} from "/@/api/modules/auth/types.ts";
import {useAppStore} from "/@/store";
import {composePermissions} from "/@/utils/permission.ts";

const useUserStore = defineStore('user', {
    state: (): UserState => ({
        id: '',
        name: '',
        lastProjectId: '',
        avatar: undefined,
        lastOrganizationId: '',
        email: '',
        phone: '',
        userRolePermissions: [],
        userRoles: [],
        userRoleRelations: [],
    }),
    getters: {
        userInfo(state: UserState): UserState {
            return {...state};
        },
        currentRole(state: UserState): {
            projectPermissions: string[];
            orgPermissions: string[];
            systemPermissions: string[];
        } {
            const appStore = useAppStore();
            state.userRoleRelations?.forEach((ug) => {
                state.userRolePermissions?.forEach((gp) => {
                    if (gp.userRole.code === ug.roleCode) {
                        ug.userRolePermissions = gp.userRolePermissions;
                        ug.userRole = gp.userRole;
                    }
                });
            });
            return {
                projectPermissions: composePermissions(state.userRoleRelations || [], 'PROJECT', appStore.currentProjectId),
                orgPermissions: composePermissions(state.userRoleRelations || [], 'ORGANIZATION', appStore.currentOrgId),
                systemPermissions: composePermissions(state.userRoleRelations || [], 'SYSTEM', 'global'),
            };
        },
        isAdmin(state: UserState): boolean {
            if (!state.userRolePermissions) return false;
            return state.userRolePermissions.findIndex((ur) => ur.userRole.code === 'admin') > -1;
        },
    },
    actions: {
        // 设置用户信息
        setInfo(partial: Partial<UserState>) {
            this.$patch(partial);
        },
        // 重置用户信息
        resetInfo() {
            this.$reset();
        },
    },
    persist: true
})
export default useUserStore;