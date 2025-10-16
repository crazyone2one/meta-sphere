<script setup lang="ts">
import {NSpin} from 'naive-ui';
import {useAppStore} from "/@/store";
const appStore = useAppStore();
</script>

<template>
  <n-layout-content content-style="padding: 20px 40px; min-height: 85vh">
    <n-spin :show="appStore.loading" :size="66">
      <router-view/>
      <template #description>
        {{ appStore.loadingTip }}
      </template>
    </n-spin>
  </n-layout-content>
</template>

<style scoped>

</style>