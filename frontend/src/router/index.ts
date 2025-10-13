import {createRouter, createWebHashHistory, type RouteRecordRaw} from 'vue-router'

const mainRoutes: RouteRecordRaw[] = [
    {
        name: 'dashboard',
        path: '/dashboard',
        component: () => import('/@/views/dashboard/index.vue'),
        meta: {
            title: 'Dashboard'
        }
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
export default router