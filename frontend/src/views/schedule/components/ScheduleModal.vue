<script setup lang="ts">
import type {FormInst, SelectOption} from "naive-ui";
import {ref, computed, watch} from "vue";
import BaseCronSelect from "/@/components/BaseCronSelect.vue";
import {useAppStore} from "/@/store";
import {scheduleApi} from "/@/api/modules/schedule";
import {useForm} from "alova/client";

const showModal = defineModel<boolean>('showModal', {type: Boolean, default: false});
const formRef = ref<FormInst | null>(null)
const appStore = useAppStore()
const emit = defineEmits<{
  (e: 'close', shouldSearch: boolean, type: string): void;
}>();
const uuid = computed(() => {
  return crypto.randomUUID()
})

const rules = {
  name: [
    {required: true, message: '请输入名称'},
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
    type: 'CRON'
  },
  immediate: false,
  resetAfterSubmiting: true
})
const jobOptions = ref<SelectOption[]>([])
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
    scheduleApi.getScheduleNameList().then(res => {
      res.map(item => {
        jobOptions.value.push({
          label: item.label,
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
          <n-input v-model:value="model.name" placeholder="Input" clearable/>
        </n-form-item>
        <n-form-item label="cron表达式" path="value">
          <base-cron-select v-model:model-value="model.value"/>
        </n-form-item>
        <n-form-item label="job" path="job">
          <n-select v-model:value="model.job" :options="jobOptions"/>
        </n-form-item>
      </n-form>
    </div>
    <template #action>
      <n-space>
        <n-button :disabled="loading" @click="handleClose(false)">取消</n-button>
        <n-button type="primary" :disabled="loading" @click="handleSubmit">确定</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>

</style>