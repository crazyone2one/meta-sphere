<script setup lang="ts">
import {onBeforeMount, ref} from "vue";
import {getCardList} from "/@/views/template/components/field-setting.ts";
import useTemplateStore from "/@/store/modules/setting/template.ts";
import BaseCardList from "/@/components/BaseCardList.vue";
import TemplateItem from "/@/views/setting/organization/template/components/TemplateItem.vue";
import {ProjectManagementRouteEnum} from "/@/enums/common-enum.ts";
import {useRoute, useRouter} from "vue-router";

const router = useRouter();
const route = useRoute();
const templateStore = useTemplateStore()
const cardList = ref<Record<string, any>[]>([]);
const updateState = () => {
  cardList.value = getCardList('project');
}
const fieldSetting = (key: string) => {
  router.push({
    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_FIELD_SETTING,
    query: {
      ...route.query,
      type: key,
    },
  });
};
// 模板管理
const templateManagement = (key: string) => {
  router.push({
    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT,
    query: {
      ...route.query,
      type: key,
    },
  });
};
onBeforeMount(() => {
  templateStore.getStatus();
  updateState();
})
</script>

<template>
  <base-card-list mode="static" :card-min-width="360" :shadow-limit="50" class="flex-1"
                  :list="cardList"
                  :is-proportional="false"
                  :gap="16"
                  padding-bottom-space="16px">
    <template #item="{ item, index }">
      <template-item :card-item="item"
                     :index="index"
                     mode="project"
                     @field-setting="fieldSetting"
                     @template-management="templateManagement"
                     @update-state="updateState"/>
    </template>
  </base-card-list>
</template>

<style scoped>

</style>