<script setup lang="ts">

import {useAppStore, useUserStore} from "/@/store";
import {onMounted} from "vue";
import {projectApis} from "/@/api/modules/project";
import router from "/@/router";

const appStore = useAppStore()
const userStore = useUserStore()
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
  <n-select v-model:value="appStore.currentProjectId" class="mr-[8px] w-[200px]"
            filterable
            :options="appStore.projectList"
            label-field="name" value-field="id"
            @update:value="handleSelectProject">
    <template #action>
      <n-button type="primary" text disabled>
        <n-icon :size="24">
          <div class="i-ant-design:plus-circle-filled"/>
        </n-icon>
      </n-button>
    </template>
  </n-select>
</template>

<style scoped>

</style>