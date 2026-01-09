<script setup lang="ts">
import {computed, onBeforeMount, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {ProjectManagementRouteEnum} from "/@/enums/common-enum.ts";
import usePermission from "/@/hook/use-permission.ts";
import MenuPanel from '/@/components/menu-panel/index.vue'

const router = useRouter();
const route = useRoute();
const permission = usePermission();
const memberPermissionShowCondition = () => {
  let show = false;
  const routerList = router.getRoutes();
  for (let i = 0; i < routerList.length; i++) {
    const rou:any = routerList[i];
    if (
        [
          ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
          ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_USER_GROUP,
        ].includes(rou.name)
    ) {
      show = permission.accessRouter(rou);
    }
    if (show) {
      break;
    }
  }
  return show;
}
const sourceMenuList = ref([
  {
    key: 'project',
    title: '项目',
    level: 1,
    name: '',
  },
  {
    key: 'projectBasicInfo',
    title: '基本信息',
    level: 2,
    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_BASIC_INFO,
  },
  {
    key: 'projectMenuManage',
    title: '应用设置',
    level: 2,
    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT,
  },
  {
    key: 'memberPermission',
    title: '成员权限',
    level: 1,
    name: '',
    showCondition: memberPermissionShowCondition,
  },
  {
    key: 'projectMember',
    title: '成员',
    level: 2,
    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
  },
  {
    key: 'projectUserGroup',
    title: '用户组',
    level: 2,
    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_USER_GROUP,
  },
]);
const menuList = computed(() => {
  const routerList = router.getRoutes();
  return sourceMenuList.value.filter((item) => {
    if (item.name) {
      const routerItem = routerList.find((rou) => rou.name === item.name);
      if (!routerItem) {
        return false;
      }
      return permission.accessRouter(routerItem);
    }
    return true;
  });
});
const currentKey = ref<string>('');
const toggleMenu = (itemName: string) => {
  if (itemName) {
    currentKey.value = itemName;
    router.push({name: itemName});
  }
};
const setInitRoute = () => {
  if (route?.name) currentKey.value = route.name as string;
};
onBeforeMount(() => {
  setInitRoute();
});
</script>

<template>
  <div class="wrapper flex ">
    <div>
      <menu-panel title="项目与权限" :default-key="currentKey" :menu-list="menuList"
                  class="mr-[16px] w-[208px] min-w-[208px] p-[16px]"
                  @toggle-menu="toggleMenu"/>
    </div>
    <n-card style="border-radius: 10px; ">
      <router-view/>
    </n-card>
  </div>
</template>

<style scoped>

</style>