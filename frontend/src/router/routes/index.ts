import type {RouteRecordRaw} from "vue-router";

export const mainRoutes: RouteRecordRaw[] = [
    {
        name: 'Dashboard',
        path: '/dashboard',
        component: () => import('/@/views/dashboard/index.vue'),
        meta: {
            title: 'Dashboard',
            icon: 'i-ant-design:dashboard-outlined'
        }
    },
    {
        name: 'Schedule',
        path: '/schedule',
        component: () => import('/@/views/schedule/index.vue'),
        meta: {
            title: 'Schedule',
            icon: 'i-ant-design:schedule-outlined'
        }
    },
    {
        path: '/setting',
        name: 'setting',
        component: null,
        meta: {
            title: 'Setting',
            icon: 'i-ant-design:setting-outlined'
        },
        children: [
            {
                path: 'user', name: 'settingUser',
                component: () => import('/@/views/setting/user/index.vue'),
                meta: {
                    title: 'User',
                    roles: ['SYSTEM_USER_ROLE:READ'],
                },
            },
            {
                path: 'user-group', name: 'settingUserGroup',
                component: () => import('/@/views/setting/user-group/index.vue'),
                meta: {
                    title: 'UserGroup',
                    roles: ['SYSTEM_USER_ROLE:READ'],
                },
            },
            {
                path: 'organization-and-project', name: 'settingOrgProject',
                component: () => import('/@/views/setting/org-project/index.vue'),
                meta: {
                    title: 'SettingOrgProject',
                    roles: ['SYSTEM_USER_ROLE:READ'],
                },
            },
        ]
    },
    {
        name: 'Template',
        path: '/template',
        meta: {
            title: 'Template',
            icon: 'i-ant-design:table-outlined'
        },
        component: null,
        children: [
            {
                name: 'CustomField',
                path: 'custom-field',
                component: () => import('/@/views/template/custom-field/index.vue'),
                meta: {
                    title: 'CustomField',
                }
            },
            {
                name: 'DynamicForm',
                path: 'dynamic-form',
                component: () => import('/@/views/template/dynamic-form/index.vue'),
                meta: {
                    title: 'Dynamic Form',
                }
            }
        ]
    },
]