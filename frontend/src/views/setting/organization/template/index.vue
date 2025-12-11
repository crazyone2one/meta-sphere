<script setup lang="ts">

import BaseCardList from "/@/components/BaseCardList.vue";
import {onBeforeMount, ref} from "vue";
import TemplateItem from "/@/views/setting/organization/template/components/TemplateItem.vue";
import {getCardList} from "/@/views/template/components/field-setting.ts";
import useTemplateStore from "/@/store/modules/setting/template.ts";
import {useRoute, useRouter} from "vue-router";
import {SettingRouteEnum} from "/@/enums/common-enum.ts";

const router = useRouter();
const route = useRoute();
const templateStore = useTemplateStore()
const cardList = ref<Record<string, any>[]>([]);
const updateState = () => {
  cardList.value = [...getCardList('organization')];
};
const fieldSetting = (key: string) => {
  router.push({
    name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_FILED_SETTING,
    query: {
      ...route.query,
      type: key,
    },
  });
};
onBeforeMount(() => {
  templateStore.getStatus();
  updateState();
});
</script>

<template>
  <n-card>
    <base-card-list mode="static"
                    :card-min-width="360"
                    class="flex-1"
                    :shadow-limit="50"
                    :list="cardList"
                    :is-proportional="false"
                    :gap="16"
                    padding-bottom-space="16px">
      <template #item="{ item, index }">
        <template-item :card-item="item"
                       :index="index"
                       mode="organization" @field-setting="fieldSetting"/>
      </template>
    </base-card-list>
  </n-card>
</template>

<style scoped>

</style>