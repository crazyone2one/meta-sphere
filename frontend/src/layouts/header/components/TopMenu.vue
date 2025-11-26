<script setup lang="ts">
import {listenerRouteChange} from "/@/utils/route-listener.ts";
import {type RouteRecordName, type RouteRecordRaw, RouterLink} from "vue-router";
import appClientMenus from "/@/router/app-menu";
import {cloneDeep} from "lodash-es";
import usePermission from "/@/hook/use-permission.ts";
import {useAppStore} from "/@/store";
import {computed, h, type Ref, ref, watch} from "vue";
import {generateMenus} from "/@/layouts/sidebar/utils.ts";
import type {MenuOption} from "naive-ui";
import type {Menu} from "/@/layouts/sidebar/index.vue";

const permission = usePermission();
const appStore = useAppStore();
const copyRouters = cloneDeep(appClientMenus) as RouteRecordRaw[];
const activeMenus: Ref<RouteRecordName[]> = ref([]);
const activeKey = ref('')
const setCurrentTopMenu = (key: string) => {
  // 先判断全等，避免同级路由出现命名包含情况
  const secParentFullSame = appStore.topMenus.find((route: RouteRecordRaw) => {
    return key === route?.name;
  });

  // 非全等的情况下，一定是父子路由包含关系
  const secParentLike = appStore.topMenus.find((route: RouteRecordRaw) => {
    return key.includes(route?.name as string);
  });

  if (secParentFullSame) {
    appStore.setCurrentTopMenu(secParentFullSame);
  } else if (secParentLike) {
    appStore.setCurrentTopMenu(secParentLike);
  }
}

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

        let filterMenuTopRouter =
            currentParent?.children?.filter((item: any) => permission.accessRouter(item) && item.meta?.isTopMenu) || [];
        appStore.setTopMenus(filterMenuTopRouter);
        setCurrentTopMenu(name as string);
        return;
      }
    }
  }
  // 切换到没有顶部菜单的路由时，清空顶部菜单
  appStore.setTopMenus([]);
  setCurrentTopMenu('');
}, true)
const topMenu = computed(() => {
  return generateMenus(appStore.getTopMenus as RouteRecordRaw[])
})
const mapping = (items: Menu[]): MenuOption[] =>
    items.map(item => {
      return ({
        ...item,
        key: item.label,
        label: item.children ? item.label :
            item.name != null ? () => h(RouterLink, {to: {name:item.name}}, {default: () => item.label}) : item.label,
        icon: item.icon != null ? () => h("div", {class: item.icon}) : undefined,
        children: item.children && mapping(item.children)
      })
    })

const menuOptions = computed(() => (topMenu.value ? mapping(topMenu.value) : []))
watch(() => appStore.getCurrentTopMenu.name, (newValue) => {
  activeMenus.value = [newValue || ''];
}, {immediate: true})

</script>

<template>
  <n-menu
      v-model:value="activeKey"
      mode="horizontal"
      :options="menuOptions"
      responsive
  />
</template>

<style scoped>

</style>