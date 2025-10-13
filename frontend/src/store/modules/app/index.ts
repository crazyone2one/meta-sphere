import {defineStore} from "pinia";
import {computed, ref} from "vue";
import type {AppState} from "/@/store/modules/app/types.ts";

const useAppStore = defineStore('app', () => {
    const appState = ref<AppState>({
        currentOrgId: '',
        currentProjectId: '',
        innerHeight: 0,
        menuCollapse: false,
        loading: false,
        loadingTip: '你不知道你有多幸运...',
    })
    const currentOrgId = computed(() => {
        return appState.value.currentOrgId
    })
    const currentProjectId = computed(() => {
        return appState.value.currentProjectId
    })
    const loadingStatus = computed(() => {
        return appState.value.loading
    })
    const setCurrentOrgId = (id: string) => {
        appState.value = {
            ...appState.value,
            currentOrgId: id
        };
    }
    const setCurrentProjectId = (id: string) => {
        appState.value = {
            ...appState.value,
            currentProjectId: id
        };
    }
    const showLoading = (tip = '') => {
        appState.value = {
            ...appState.value,
            loading: true,
            loadingTip: tip || '加载中...'
        };
    }
    const hideLoading = () => {
        appState.value = {
            ...appState.value,
            loading: false
        };
    }
    const setMenuCollapse = (value: boolean) => {
      appState.value = {
          ...appState.value,
          menuCollapse: value
      };
    }
    return {
        appState,
        currentOrgId,
        currentProjectId,
        loadingStatus,
        setCurrentOrgId,
        setCurrentProjectId,
        showLoading,
        hideLoading,
        setMenuCollapse
    }
}, {
    persist: {
        pick: ['appState.currentOrgId', 'appState.currentProjectId', 'appState.menuCollapse']
    }
})
export default useAppStore