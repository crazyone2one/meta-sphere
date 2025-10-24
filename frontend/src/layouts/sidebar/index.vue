<script setup lang="ts">
import {computed, h, ref, watchEffect} from "vue";
import type {MenuOption} from "naive-ui";
import {RouterLink, useRoute} from "vue-router";
import useAppStore from "../../store/modules/app";

interface Menu {
  label: string
  icon?: string
  name: string
  params?: { [key: string]: string }
  children?: Menu[]
}

const appStore = useAppStore()
const route = useRoute()
const currentKey = ref<string>('')
const expandedKeys = ref<string[]>([])
const mapping = (items: Menu[]): MenuOption[] =>
    items.map(item => ({
      ...item,
      key: item.label,
      label: item.children ? item.label :
          item.name != null ? () => h(RouterLink, {to: item}, {default: () => item.label}) : item.label,
      icon: item.icon != null ? () => h("div", {class: item.icon}) : undefined,
      children: item.children && mapping(item.children)
    }))
const menus = computed(() => [
  {
    label: 'Dashboard',
    name: 'Dashboard',
    icon: 'i-ant-design:dashboard-outlined',
  },
  {
    label: 'Schedule',
    name: 'Schedule',
    icon: 'i-ant-design:schedule-outlined',
  },
  {
    label: 'Setting',
    name: 'setting',
    icon: 'i-ant-design:setting-outlined',
    children: [
      {
        label: 'User',
        name: 'settingUser',
      },
      {
        label: 'UserGroup',
        name: 'settingUserGroup',
      }
    ]
  },
  {
    label: 'Template',
    name: 'Template',
    icon: "i-ant-design:slack-outlined",
    children: [
      {
        label: 'DynamicForm',
        name: 'DynamicForm',
        icon: 'i-ant-design:table-outlined',
      }
    ]
  }
])
const options = computed(() => (menus.value ? mapping(menus.value) : []))
const routeMatched = (menu: Menu): boolean => {
  return route.name === menu.name && (menu.params == null || JSON.stringify(route.params) === JSON.stringify(menu.params))
}
const matchExpanded = (items: Menu[]): boolean => {
  let matched = false
  for (const item of items) {
    if (item.children != null) {
      matchExpanded(item.children) && expandedKeys.value.push(item.label)
    }
    if (routeMatched(item)) {
      currentKey.value = item.label
      matched = true
    }
  }
  return matched
}
watchEffect(() => menus.value && matchExpanded(menus.value))
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
        :value="currentKey"
        :default-expanded-keys="expandedKeys"
        :options="options"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :root-indent="18"
        class="mt-[64px]"
        @update:value="
        (k: string) => {
          currentKey = k
        }
      "
    />
  </n-layout-sider>
</template>

<style scoped>

</style>