import type {RouteRecordRaw} from "vue-router";
import Setting from "./modules/setting.ts";
import ProjectManagement from "./modules/project-management.ts";

export const mainRoutes: RouteRecordRaw[] = [
    {
        name: 'Dashboard',
        path: '/dashboard',
        component: () => import('/@/views/dashboard/index.vue'),
        meta: {
            title: 'Dashboard',
            icon: 'i-ant-design:dashboard-outlined', order: 1
        }
    },
    {...Setting}, {...ProjectManagement}
]