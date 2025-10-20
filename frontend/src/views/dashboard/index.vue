<script setup lang="ts">

import DynamicFormModal from "/@/components/dynamic-form-modal/DynamicFormModal.vue";
import {type FormData} from "/@/components/dynamic-form-modal/DynamicFormModal.vue";
import {ref} from "vue";

const showDynamicFormModalVisible = ref(false);
const dynamicFormModalRef = ref<InstanceType<typeof DynamicFormModal>>();
const config = ref<FormData>({})
const handleOpenDynamicFormModal = () => {
  showDynamicFormModalVisible.value = true;
  config.value = {k1: `${crypto.randomUUID()}`, k2: 2, k3Ids: ['1', '2'],k4: 'true',k5:false}
}
const handleCloseDynamicFormModal = () => {
  showDynamicFormModalVisible.value = false;
}

const handleSubmit = (formData: FormData) => {
  console.log(formData)
}

</script>

<template>
  <n-button @click="handleOpenDynamicFormModal">打开动态表单弹框</n-button>
  <dynamic-form-modal ref="dynamicFormModalRef"
                      v-model:show-modal="showDynamicFormModalVisible"
                      :config="config"
                      @close="handleCloseDynamicFormModal"
                      @update="handleSubmit"/>
</template>

<style scoped>

</style>