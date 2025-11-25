import {defineStore} from "pinia";
import {computed} from "vue";
import {useAppStore} from "/@/store";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {templateApi} from "/@/api/modules/setting/template";

const appStore = useAppStore()
const useTemplateStore = defineStore('template', {
    persist: true,
    state: (): {
        orgStatus: Record<string, boolean>;
        projectStatus: Record<string, boolean>;
    } => ({
        orgStatus: {
            FUNCTIONAL: false,
            API: false,
            UI: false,
            TEST_PLAN: false,
            BUG: false,
            SCHEDULE: false,
        },
        projectStatus: {
            FUNCTIONAL: false,
            API: false,
            UI: false,
            TEST_PLAN: false,
            BUG: false,
            SCHEDULE: false,
        },
    }),
    actions: {
        // 模板列表的状态
        async getStatus() {
            const currentOrgId = computed(() => appStore.currentOrgId);
            const currentProjectId = computed(() => appStore.currentProjectId);
            try {
                if (currentOrgId.value && hasAnyPermission(['ORGANIZATION_TEMPLATE:READ'])) {
                    this.orgStatus = await templateApi.getOrgTemplate(currentOrgId.value);
                }
                if (
                    currentProjectId.value &&
                    hasAnyPermission(['PROJECT_TEMPLATE:READ']) &&
                    currentProjectId.value !== 'no_such_project'
                ) {
                    this.projectStatus = await templateApi.getProTemplate(currentProjectId.value);
                }
            } catch (error) {
                console.log(error);
            }
        },
    },
})
export default useTemplateStore;