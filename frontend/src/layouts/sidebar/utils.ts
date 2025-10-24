import type {RouteRecordRaw} from "vue-router";
import type {Menu} from "/@/layouts/sidebar/index.vue";

/**
 * 根据路由信息生成菜单数据
 * @param routers 路由信息
 */
export const generateMenus = (routers: RouteRecordRaw[]) => {
    return routers.map(router => {
        const menu: Menu = {
            label: (router.meta?.title as string) || (router.name as string) || '',
            name: (router.name as string) || '',
            icon: router.meta?.icon as string | undefined,
        }
        if (router.children) {
            menu.children = generateMenus(router.children)
        }
        return menu
    })
}