import {defineStore} from "pinia";
import type {UserState} from "/@/api/modules/auth/types.ts";

const useUserStore = defineStore('user', {
    state: (): UserState => ({
        id: '',
        name: '',
        lastProjectId: '',
        avatar: undefined,
        lastOrganizationId: '',
        email: '',
        phone: '',
    }),
    getters: {
        userInfo(state: UserState): UserState {
            return {...state};
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