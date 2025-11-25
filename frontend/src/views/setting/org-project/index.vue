<script setup lang="ts">

import {nextTick, onBeforeMount, ref, useTemplateRef} from "vue";
import SystemOrg from "/@/views/setting/org-project/components/SystemOrg.vue";
import SystemProject from "/@/views/setting/org-project/components/SystemProject.vue";
import AddProjectModal from "/@/views/setting/org-project/components/AddProjectModal.vue";
import {useRequest} from "alova/client";
import {orgAndProjectApi} from "/@/api/modules/setting/org-project";

const keyword = ref('')
const currentTable = ref('project');
const organizationCount = ref(0);
const projectCount = ref(0);
const orgTableRef = useTemplateRef<InstanceType<typeof SystemOrg>>('orgTableRef');
const projectTableRef = useTemplateRef<InstanceType<typeof SystemProject>>('projectTableRef');
const projectVisible = ref(false);
const organizationVisible = ref(false);
const handleAddOrganization = () => {
  if (currentTable.value === 'organization') {
    organizationVisible.value = true;
  } else {
    projectVisible.value = true;
  }
}
const tableSearch = () => {
  if (currentTable.value === 'organization') {
    if (orgTableRef.value) {
      orgTableRef.value.fetchData();
    } else {
      nextTick(() => {
        orgTableRef.value?.fetchData();
      });
    }
  } else if (projectTableRef.value) {
    projectTableRef.value.fetchData();
  } else {
    nextTick(() => {
      projectTableRef.value?.fetchData();
    });
  }
  initOrgAndProjectCount();
}
const handleAddProjectCancel = (shouldSearch: boolean) => {
  projectVisible.value = false;
  if (shouldSearch) {
    tableSearch();
  }
}
const {send: fetchOrgAndProjectCount} = useRequest(() => orgAndProjectApi.getOrgAndProjectCount(), {immediate: false})
const initOrgAndProjectCount = () => {
  fetchOrgAndProjectCount().then((res) => {
    organizationCount.value = res.organizationTotal
    projectCount.value = res.projectTotal
  })
}
onBeforeMount(() => {
  initOrgAndProjectCount()
})
</script>

<template>
  <n-card>
    <template #header>
      <n-flex>
        <n-radio-group v-model:value="currentTable" name="currentTable">
          <n-radio-button value="organization">组织({{ `${organizationCount}` }})</n-radio-button>
          <n-radio-button value="project">项目({{ `${projectCount}` }})</n-radio-button>
        </n-radio-group>
        <n-button v-if="currentTable !== 'organization'" v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+ADD']"
                  type="primary"
                  @click="handleAddOrganization">
          {{ currentTable === 'organization' ? '创建组织' : '创建项目' }}
        </n-button>
      </n-flex>
    </template>
    <template #header-extra>
      <n-space>
        <n-input v-model:value="keyword" clearable placeholder="通过 ID/名称搜索"/>
      </n-space>
    </template>
    <div>
      <system-org v-if="currentTable === 'organization'" ref="orgTableRef"/>
      <system-project v-if="currentTable === 'project'" ref="projectTableRef" v-model:keyword="keyword"/>
    </div>
  </n-card>
  <add-project-modal v-model:show-modal="projectVisible" @cancel="handleAddProjectCancel"/>
</template>

<style scoped>

</style>