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
  name: [{required: true, message: '请输入名称'},],
  job: [{required: true, message: '请选择对应的job'},],
  value: [{required: true, message: '请选择或者录入对应的cron'}],
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
    resourceType: 'CDDY',
    sensorGroup: 'aqjk',
    sensorType: '',
  },
  immediate: false,
  resetAfterSubmiting: true
})
const jobOptions = ref<SelectOption[]>([])
const resourceTypeOptions = ref<SelectOption[]>([
  {label: "CDDY", value: "CDDY"},
  {label: "CDSS", value: "CDSS"},
]);
const sensorGroupOptions = computed<Array<SelectOption>>(() => {
  return [
    {label: '安全监控', value: 'aqjk'},
    {label: '矿压', value: 'ky'},
    {label: '水害防治', value: 'shfz'},
  ];
});
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
          label-placement="top"
          require-mark-placement="right-hanging"
      >
        <n-grid :cols="12" :x-gap="12">
          <n-form-item-gi :span="12" label="name" path="name">
            <n-input v-model:value="model.name" placeholder="输入任务名称" clearable/>
          </n-form-item-gi>
          <n-form-item-gi :span="6" label="cron表达式" path="value">
            <base-cron-select v-model:model-value="model.value" placeholder="xxxxx"/>
          </n-form-item-gi>
          <n-form-item-gi :span="6" label="job" path="job">
            <n-select v-model:value="model.job" :options="jobOptions"/>
          </n-form-item-gi>
          <n-form-item-gi :span="4" label="资源类型" path="resourceType">
            <n-select v-model:value="model.resourceType" :options="resourceTypeOptions"/>
          </n-form-item-gi>
          <n-form-item-gi :span="4" label="传感器分组" path="sensorGroup">
            <n-select v-model:value="model.sensorGroup" :options="sensorGroupOptions"/>
          </n-form-item-gi>
          <n-form-item-gi :span="4" label="传感器类型" path="sensorType">
            <n-input v-model:value="model.sensorType" clearable placeholder="输入传感器类型"/>
          </n-form-item-gi>
        </n-grid>
      </n-form>
    </div>
    <template #action>
      <n-flex>
        <n-button :disabled="loading" @click="handleClose(false)">取消</n-button>
        <n-button type="primary" :disabled="loading" @click="handleSubmit">确定</n-button>
      </n-flex>
    </template>
  </n-modal>
</template>

<style scoped>

</style>