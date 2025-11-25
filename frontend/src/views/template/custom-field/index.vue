<script setup lang="ts">
import EditFieldDrawer from "/@/views/template/components/EditFieldDrawer.vue";
import {ref} from "vue";

const editFieldDrawerRef = ref<InstanceType<typeof EditFieldDrawer>>();
const editFieldDrawer = ref(false)
const handleAdd = () => {
  editFieldDrawer.value = true;
}
</script>

<template>
  <n-card>
    <n-button @click="handleAdd">创建用户组</n-button>
  </n-card>
  <edit-field-drawer ref="editFieldDrawerRef" v-model:active="editFieldDrawer" mode="project" :data="[]"/>
</template>

<style scoped>

</style>