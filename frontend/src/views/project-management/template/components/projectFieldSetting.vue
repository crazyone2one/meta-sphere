<script setup lang="ts">

import FieldSetting from "/@/views/setting/organization/template/components/FieldSetting.vue";
</script>

<template>
  <n-card>
    <field-setting mode="project" :delete-permission="['PROJECT_TEMPLATE:READ+DELETE']"
                   :update-permission="['PROJECT_TEMPLATE:READ+UPDATE']"
                   :create-permission="['PROJECT_TEMPLATE:READ+ADD']"/>
  </n-card>
</template>

<style scoped>

</style>