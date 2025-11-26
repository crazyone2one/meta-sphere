<script setup lang="ts">
import {computed} from "vue";
import {useAppStore, useUserStore} from "/@/store";
import router from "/@/router";
import {authApi} from "/@/api/modules/auth";

const userStore = useUserStore()
const appStore = useAppStore()
const options = computed(() => {
  return [
    {
      label: '用户资料',
      key: 'profile',
    },
    {
      label: '退出登录',
      key: 'logout',
    }
  ]
})
const userInitial = computed(() => {
  if (userStore.name) {
    // 如果用户名存在，返回首字母
    return userStore.name.charAt(0).toUpperCase();
  } else if (userStore.email) {
    // 如果邮箱存在，返回邮箱首字母
    return userStore.email.charAt(0).toUpperCase();
  } else {
    // 默认显示
    return 'M';
  }
})
const logout = () => {
  window.$dialog.warning({
    title: '确定要退出登录吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      authApi.logout().then(() => {
        const currentRoute = router.currentRoute.value;
        router.push({
          name: 'login',
          query: {
            ...router.currentRoute.value.query,
            redirect: currentRoute.name as string,
          }
        })
        userStore.resetInfo()
        appStore.setTopMenus([])
      })
    },
  })
}
const handleSelect = (key: string) => {
  switch (key) {
    case 'profile':
      break;
    case 'logout':
      logout()
      break;
  }
}
</script>

<template>
  <n-dropdown trigger="click" :options="options" @select="handleSelect">
    <n-avatar round :size="26" class="cursor-pointer ml-[5px] color-purple">
      {{ userInitial }}
    </n-avatar>
  </n-dropdown>
</template>

<style scoped>

</style>