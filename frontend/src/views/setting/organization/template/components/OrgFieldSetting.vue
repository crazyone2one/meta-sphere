<script setup lang="ts">

import FieldSetting from "/@/views/setting/organization/template/components/FieldSetting.vue";
</script>

<template>
<n-card>
  <field-setting mode="organization" :delete-permission="['ORGANIZATION_TEMPLATE:READ+DELETE']"
                 :update-permission="['ORGANIZATION_TEMPLATE:READ+UPDATE']"
                 :create-permission="['ORGANIZATION_TEMPLATE:READ+ADD']"/>
</n-card>
</template>

<style scoped>

</style>