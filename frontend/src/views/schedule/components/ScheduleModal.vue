<script setup lang="ts">
import type {FormInst, SelectOption} from "naive-ui";
import {computed, ref, watch} from "vue";
import BaseCronSelect from "/@/components/BaseCronSelect.vue";
import {useAppStore} from "/@/store";
import {scheduleApi} from "/@/api/modules/schedule";
import {useForm} from "alova/client";
import {getClassSimpleName} from "/@/utils";

const showModal = defineModel<boolean>('showModal', {type: Boolean, default: false});
const formRef = ref<FormInst | null>(null)
const appStore = useAppStore()
const emit = defineEmits<{
  (e: 'close', shouldSearch: boolean, type: string): void;
}>();

// UUID 生成函数，用于替换 crypto.randomUUID()
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

const uuid = computed(() => {
  return generateUUID()
})

const rules = {
  name: [
    {required: true, message: '请输入名称'},
  ],
  job: [
    {required: true, message: '请选择对应的job'},
  ],
  value: [
    {required: true, message: '请选择或者录入对应的cron'},
  ],
};
const {form: model, loading, send: submit} = useForm((formData) => scheduleApi.saveSchedule(formData), {
  initialForm: {
    name: '',
    resourceId: uuid.value,
    enable: true,
    value: '',
    key: uuid.value,
    projectId: appStore.currentProjectId,
    job: '',
    type: 'CRON',
    resourceType: 'CDDY'
  },
  immediate: false,
  resetAfterSubmiting: true
})
const jobOptions = ref<SelectOption[]>([])
const resourceTypeOptions = ref<SelectOption[]>([
  {label: "CDDY", value: "CDDY"},
  {label: "CDSS", value: "CDSS"},
]);
const handleSubmit = () => {
  formRef.value?.validate(error => {
    if (!error) {
      submit(model.value).then(() => {
        window.$message.success('创建成功');
        handleClose(true)
      })
    }
  })
}
const handleClose = (search: boolean) => {
  emit('close', search, 'modal')
  formRef.value?.restoreValidation()
}
watch(() => showModal.value, (value) => {
  if (value) {
    jobOptions.value = []
    scheduleApi.getScheduleNameList(appStore.currentProjectId).then(res => {
      res.map(item => {
        jobOptions.value.push({
          label: getClassSimpleName(item.label as string),
          value: item.value,
          disabled: item.disabled
        })
      })
    })
  }
})
</script>

<template>
  <n-modal v-model:show="showModal" preset="dialog" title="Dialog">
    <template #header>
      <div>创建定时任务</div>
    </template>
    <div>
      <n-form
          ref="formRef"
          :model="model"
          :rules="rules"
          label-placement="left"
          label-width="auto"
          require-mark-placement="right-hanging"
      >
        <n-form-item label="name" path="name">
          <n-input v-model:value="model.name" placeholder="输入任务名称" clearable/>
        </n-form-item>
        <n-form-item label="cron表达式" path="value">
          <base-cron-select v-model:model-value="model.value" placeholder="xxxxx"/>
        </n-form-item>
        <n-form-item label="job" path="job">
          <n-select v-model:value="model.job" :options="jobOptions"/>
        </n-form-item>
      </n-form>
    </div>
    <template #action>
      <div class="flex items-center justify-between">
        <div class="flex flex-row items-center justify-center mr-3">
          <n-select v-model:value="model.resourceType" :options="resourceTypeOptions" class="w-[90px]"/>
        </div>
        <n-space>
          <n-button :disabled="loading" @click="handleClose(false)">取消</n-button>
          <n-button type="primary" :disabled="loading" @click="handleSubmit">确定</n-button>
        </n-space>
      </div>

    </template>
  </n-modal>
</template>

<style scoped>

</style>