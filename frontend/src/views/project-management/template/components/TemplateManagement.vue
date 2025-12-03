<script setup lang="ts">
import {useRoute} from "vue-router";
import {getTemplateName} from "/@/views/setting/organization/template/components/field-setting.ts";
import {computed, onMounted, ref} from "vue";
import {useAppStore} from "/@/store";
import router from "/@/router";
import {ProjectManagementRouteEnum} from "/@/enums/common-enum.ts";

const route = useRoute();
const appStore = useAppStore()
const keyword = ref('');
const currentProjectId = computed(() => appStore.currentProjectId);
const routeName = ref<string>('');
const createTemplate = () => {
  router.push({
    name: routeName.value,
    query: {
      ...route.query,
      type: route.query.type,
    },
    params: {
      mode: 'create',
    },
  });
}
console.log(currentProjectId.value)
onMounted(() => {
  if (route.query.type === 'FUNCTIONAL') {
    routeName.value = ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_CASE_DETAIL;
  } else if (route.query.type === 'API') {
    routeName.value = ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_API_DETAIL;
  } else {
    routeName.value = ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_SCHEDULE_DETAIL;
  }
})
</script>

<template>
  <n-card>
    <div class="mb-4 flex items-center justify-between">
      <!--      <span class="font-medium">-->
      <!--        {{ `${getTemplateName('project', route.query.type as string)}列表` }}-->
      <!--      </span>-->
      <n-button @click="createTemplate">{{ `创建${getTemplateName('project', route.query.type as string)}` }}</n-button>
      <div>
        <n-input v-model:value="keyword" placeholder="通过名称搜索" class="w-[230px]"/>
      </div>
    </div>
  </n-card>
</template>

<style scoped>

</style>