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
            icon: 'i-ant-design:dashboard-outlined',order: 2
        }
    },
    // {
    //     name: 'Schedule',
    //     path: '/schedule',
    //     component: () => import('/@/views/schedule/index.vue'),
    //     meta: {
    //         title: 'Schedule',
    //         icon: 'i-ant-design:schedule-outlined'
    //     }
    // },
    {...Setting},{...ProjectManagement}
    // {
    //     name: 'Template',
    //     path: '/template',
    //     meta: {
    //         title: 'Template',
    //         icon: 'i-ant-design:table-outlined'
    //     },
    //     component: null,
    //     children: [
    //         {
    //             name: 'CustomField',
    //             path: 'custom-field',
    //             component: () => import('/@/views/template/custom-field/index.vue'),
    //             meta: {
    //                 title: 'CustomField',
    //             }
    //         },
    //         {
    //             name: 'DynamicForm',
    //             path: 'dynamic-form',
    //             component: () => import('/@/views/template/dynamic-form/index.vue'),
    //             meta: {
    //                 title: 'Dynamic Form',
    //             }
    //         }
    //     ]
    // },
]