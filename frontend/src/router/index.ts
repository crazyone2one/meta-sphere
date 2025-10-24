import {createRouter, createWebHashHistory, type RouteRecordRaw} from 'vue-router'
import createRouteGuard from "/@/router/guard";

const mainRoutes: RouteRecordRaw[] = [
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
                    title: '用户',
                    roles: ['SYSTEM_USER_ROLE:READ'],
                },
            },
            {
                path: 'user-group', name: 'settingUserGroup',
                component: () => import('/@/views/setting/user-group/index.vue'),
                meta: {
                    title: '用户组',
                    roles: ['SYSTEM_USER_ROLE:READ'],
                },
            }
        ]
    },
    {
        name: 'Template',
        path: '/template',
        component: null,
        children: [
            {
                name: 'DynamicForm',
                path: '/dynamic-form',
                component: () => import('/@/views/template/dynamic-form/index.vue'),
                meta: {
                    title: 'Dynamic Form',
                    icon: 'i-ant-design:table-outlined'
                }
            }
        ]
    },
]
const routes: RouteRecordRaw[] = [
    {
        name: 'login',
        path: '/login',
        component: () => import('/@/views/login/index.vue'),
        meta: {
            title: 'Sign In'
        }
    },
    {
        name: 'layout',
        path: '/',
        redirect: '/dashboard',
        component: () => import('/@/layouts/index.vue'),
        children: [...mainRoutes]
    },
    {
        name: 'not-found',
        path: '/:path*',
        component: () => import('/@/views/error/NotFound.vue'),
        meta: {
            title: 'Oh no!'
        }
    }
]
const router = createRouter({
    history: createWebHashHistory(),
    routes,
})
router.afterEach(to => {
    const items = [import.meta.env.VITE_APP_TITLE]
    to.meta.title != null && items.unshift(to.meta.title as string)
    document.title = items.join(' · ')
})
createRouteGuard(router);
export default router