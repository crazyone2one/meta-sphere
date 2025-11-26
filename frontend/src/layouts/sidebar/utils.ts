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
        if (router.children && router.children.length > 0) {
            menu.children = generateMenus(router.children)
        }
        return menu
    })
}
/**
 * 根据路由信息生成菜单图标
 * @param routers
 */
export const generateMenuIcons = (routers: RouteRecordRaw[]) => {
    const menuIcons: Array<string> = [];
    routers.forEach(router => {
        if (router.meta?.icon) {
            menuIcons.push(router.meta.icon as string)
        }
    })
    return menuIcons
}