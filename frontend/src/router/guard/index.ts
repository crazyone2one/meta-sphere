import type {LocationQueryRaw, Router} from 'vue-router';
import {hasToken} from "/@/utils/auth.ts";

function setupPageGuard(router: Router) {
    router.beforeEach((to, _from, next) => {
        if (to.name !== 'login' && hasToken()) {
            next();
        } else {
            if (to.name === 'login') {
                next();
                return;
            }
            next({
                name: 'login',
                query: {
                    redirect: to.name,
                    ...to.query,
                } as LocationQueryRaw,
            });
            // await sleep(0);
        }
    })
}

export default function createRouteGuard(router: Router) {
    // 设置路由监听守卫
    setupPageGuard(router);
}