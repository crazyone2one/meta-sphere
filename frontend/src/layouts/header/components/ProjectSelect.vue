<script setup lang="ts">

import {useAppStore, useUserStore} from "/@/store";
import {onMounted, ref} from "vue";
import {projectApis} from "/@/api/modules/project";
import router from "/@/router";

const appStore = useAppStore()
const userStore = useUserStore()
const show = ref(false)
const handleSelectProject = (value: string) => {
  projectApis.switchProject({
    projectId: value,
    userId: userStore.id || '',
  }).then(() => {
    router.replace({
      name: router.currentRoute.value.name,
      query: {
        orgId: appStore.currentOrgId,
        pId: value,
      }
    })
  })
}
onMounted(async () => {
  await appStore.initProjectList()
})
</script>

<template>
  <n-select
      v-model:show="show"
      v-model:value="appStore.currentProjectId"
      class="mr-[8px] w-[200px]"
      filterable
      :options="appStore.projectList"
      label-field="name" value-field="id"
      @update:value="handleSelectProject">
    <template #arrow>
      <div v-if="!show" class="color-purple i-ant-design:caret-down-filled"/>
      <div v-else class="i-ant-design:search-outlined"/>
    </template>
    <template #action>
      <n-button type="primary" text disabled>
        <n-icon :size="20" class="mx-2">
          <div class="i-ant-design:plus-circle-filled"/>
        </n-icon>
        新建项目
      </n-button>
    </template>
  </n-select>
</template>

<style scoped>

</style>