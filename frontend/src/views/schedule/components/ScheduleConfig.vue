<script setup lang="ts">
import type {ICustomConfig, IScheduleInfo} from "/@/api/modules/schedule/types.ts";
import BaseCronSelect from "/@/components/BaseCronSelect.vue";
import {reactive, ref, watch} from "vue";
import {useForm} from "alova/client";
import {scheduleApi} from "/@/api/modules/schedule";
import DynamicFormDrawer from "/@/components/dynamic-form-drawer/index.vue";
import {NCode} from "naive-ui";

const emit = defineEmits<{
  (e: 'close', shouldSearch: boolean, type: string): void;
}>();
const showModal = defineModel<boolean>('showModal', {type: Boolean, default: false});
const props = defineProps<{ task: IScheduleInfo }>()
const showDynamicFormModalVisible = ref(false);
const dynamicFormDrawerRef = ref<InstanceType<typeof DynamicFormDrawer>>();
let customConfig = reactive<ICustomConfig>({
  alarmFlag: false,
  sensorIds: '',
  superthreshold: false,
  thresholdInterval: ''
})
const handleClose = (search: boolean) => {
  emit('close', search, 'config')
}

const addConfigItem = () => {
  showDynamicFormModalVisible.value = true;
};
const handleCloseDynamicFormModal = () => {
  showDynamicFormModalVisible.value = false;
}
const {form: model, send: submit, loading} = useForm((formData) => scheduleApi.runScheduleConfig(formData), {
  immediate: false,
  initialForm: {
    resourceId: "",
    cron: "",
    enable: false,
    runConfig: {} as Record<string, any>
  }
})
const handleSubmit = () => {
  submit().then(() => {
    handleClose(true);
    window.$message.success('修改成功');
  })
}

const handleSubmitConfig = (value: Record<string, any>): void => {
  // 清空现有配置
  Object.keys(model.value.runConfig).forEach(key => {
    delete model.value.runConfig[key];
  });
  // 添加新配置
  Object.keys(value).forEach(key => {
    model.value.runConfig[key] = value[key];
  });
  model.value.runConfig['customConfig'] = customConfig;
  handleCloseDynamicFormModal()
}
const handleCustomConfig = (config: ICustomConfig) => {
  customConfig = {...config};
}
watch(() => props.task, (newValue) => {
  model.value.resourceId = newValue.resourceId;
  model.value.cron = newValue.value;
  model.value.enable = newValue.enable;
  model.value.runConfig = newValue.runConfig ?? {};
})
</script>

<template>
  <n-modal v-model:show="showModal" preset="dialog" title="Dialog">
    <template #header>
      <div>配置定时任务</div>
    </template>
    <div>
      <n-form
          ref="formRef"
          :model="model"
          label-placement="left"
          size="small" class="mt-[20px]"
      >
        <n-form-item label="cron表达式" path="cron">
          <base-cron-select v-model:model-value="model.cron" size="small"/>
        </n-form-item>
        <n-divider title-placement="left">运行配置</n-divider>
        <n-code :code="JSON.stringify(model.runConfig, null, 2)" language="json"/>
        <n-form-item>
          <n-button secondary type="info" size="tiny" @click="addConfigItem">
            添加参数
          </n-button>
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
  <dynamic-form-drawer ref="dynamicFormDrawerRef"
                       v-model:show-modal="showDynamicFormModalVisible" :config="model.runConfig"
                       :resourceType="task.resourceType==='CDSS'"
                       @close="handleCloseDynamicFormModal"
                       @update="handleSubmitConfig"
                       @update-config="handleCustomConfig"/>
</template>

<style scoped>
.n-divider:not(.n-divider--vertical) {
  margin-bottom: 24px;
  margin-top: 1px;
}
</style>