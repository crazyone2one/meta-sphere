<script setup lang="ts">
import {computed, h, ref} from "vue";
import type {MenuOption} from "naive-ui";
import {type RouteMeta, type RouteRecordRaw, RouterLink} from "vue-router";
import useAppStore from "../../store/modules/app";
import {generateMenus} from "/@/layouts/sidebar/utils.ts";
import useMenuTree from "/@/layouts/sidebar/use-menu-tree.ts";
import {getFirstRouterNameByCurrentRoute} from "/@/utils/permission.ts";
import {listenerRouteChange} from "/@/utils/route-listener.ts";
import {cloneDeep} from "lodash-es";
import appClientMenus from "/@/router/app-menu";
import usePermission from "/@/hook/use-permission.ts";

export interface Menu {
  label: string
  key: string
  icon?: string
  name: string
  params?: { [key: string]: string }
  children?: Menu[]
  meta?: RouteMeta
}

const {menuTree} = useMenuTree();
const appStore = useAppStore()
const copyRouters = cloneDeep(appClientMenus) as RouteRecordRaw[];

const permission = usePermission();
const mapping = (items: Menu[]): MenuOption[] =>
    items.map(item => {
      return ({
        ...item,
        key: item.name,
        label: item.children ? item.label :
            item.name != null ? () => h(RouterLink, {
              to:
                  {name: item.meta?.hideChildrenInMenu ? getFirstRouterNameByCurrentRoute(item.name) : item.name}
            }, {default: () => item.label}) : item.label,
        icon: item.icon != null ? () => h("div", {class: item.icon}) : undefined,
        children: item.children && mapping(item.children)
      })
    })
const menus = computed(() => {
  return generateMenus(menuTree.value as RouteRecordRaw[])
})
const options = computed(() => (menus.value ? mapping(menus.value) : []))
const openKeys = ref<string>('');
const selectedKey = ref<string[]>([]);
listenerRouteChange((newRoute) => {
  const {name} = newRoute;
  for (let i = 0; i < copyRouters.length; i++) {
    const firstRoute = copyRouters[i] as RouteRecordRaw;
    // 权限校验通过
    if (permission.accessRouter(firstRoute)) {
      if (name && firstRoute?.name && (name as string).includes(firstRoute.name as string)) {
        // 先判断二级菜单是否顶部菜单
        let currentParent = firstRoute?.children?.some((item) => item.meta?.isTopMenu)
            ? (firstRoute as RouteRecordRaw)
            : undefined;
        if (!currentParent) {
          // 二级菜单非顶部菜单，则判断三级菜单是否有顶部菜单
          currentParent = firstRoute?.children?.find(
              (item) => name && item?.name && (name as string).includes(item.name as string)
          );
        }
        openKeys.value = currentParent ? currentParent.name as string : firstRoute.name as string;
        selectedKey.value = [firstRoute.name as string, currentParent?.name as string]
        return;
      }
    }
  }
}, true)

</script>

<template>
  <n-layout-sider
      :native-scrollbar="false"
      bordered
      :collapsed-width="64"
      collapse-mode="width"
      :width="240"
      :collapsed="appStore.getMenuCollapse"
      show-trigger
      @collapse="appStore.setMenuCollapse(true)"
      @expand="appStore.setMenuCollapse(false)"
  >
    <n-menu
        :value="openKeys"
        :default-expanded-keys="selectedKey"
        :options="options"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :root-indent="18"
        class="mt-[64px]"
    />
  </n-layout-sider>
</template>

<style scoped>

</style>