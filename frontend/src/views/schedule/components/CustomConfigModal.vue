<script setup lang="ts">

import {ref, watch} from "vue";
import type {ICustomConfig} from "/@/api/modules/schedule/types.ts";
import type {SelectOption} from "naive-ui";
import {scheduleApi} from "/@/api/modules/schedule";
import {useAppStore} from "/@/store";
import {useRequest} from "alova/client";
import {sensorTypeOptions} from "/@/views/schedule/components/index.ts";

const appStore = useAppStore();
const showModal = defineModel<boolean>('showModal', {type: Boolean, default: false});
const sensorOptions = ref<Array<SelectOption>>([])
let customConfig = ref<ICustomConfig>({
  sensorIds: '',
  superthreshold: false, //超阈值
  thresholdInterval: '', //阈值区间
  alarmFlag: false, //是否预警
  sensorType: '', // 测点类型
  sensorValueType: '', // 测点数值类型
})
const {config = {}, sensorGroup = ''} = defineProps<{
  config?: ICustomConfig;
  sensorGroup?: string
}>()
const emit = defineEmits<{
  (e: 'updateConfig', value: ICustomConfig, key: string): void;
  (e: 'close'): void;
}>();
const handleSubmit = () => {
  emit('updateConfig', customConfig.value, "customConfig")
  emit('close')
}
const handleSelectSensor = (_value: string, option: SelectOption) => {
  customConfig.value.sensorType = option.sensorType;
  customConfig.value.sensorValueType = option.sensorValueType;
}
const handleUpdateSensorTypeOption = (sensorType: string) => {
  const filteredOptions = sensorOptions.value.filter(item => item.sensorType === sensorType);
  if (filteredOptions.length === 0) {
    window.$message.warning('未找到该测点类型下的测点');
    sensorOptions.value = sensorOptions.value; // 保持原样或者从备份恢复
  } else {
    sensorOptions.value = filteredOptions;
  }
}
const {send: fetchSensorList, loading} = useRequest(() => {
  const param = {projectId: appStore.currentProjectId, sensorGroup: sensorGroup}
  return scheduleApi.getSensorList(param);
}, {immediate: false})

watch(() => config, (newValue) => {
  customConfig.value = {...newValue} as ICustomConfig
}, {deep: true})
watch(() => showModal.value, (show) => {
  if (show) {
    sensorOptions.value = [];
    fetchSensorList().then(res => sensorOptions.value = res)
  }
})
</script>

<template>
  <n-spin v-model:show="loading">
    <n-modal v-model:show="showModal" preset="dialog" title="Dialog" style="width: 500px">
      <template #header>
        <div>自定义参数</div>
      </template>
      <div>
        <n-form
            ref="formRef"
            :model="customConfig"
            label-placement="left"
            size="small" class="mt-[20px]"
        >
          <n-form-item label="测点id">
            <n-select v-model:value="customConfig.sensorIds"
                      :options="sensorOptions"
                      @update:value="handleSelectSensor"
                      filterable clearable
                      placeholder="选择测点sensor id"/>
            <n-select :options="sensorTypeOptions" @update:value="handleUpdateSensorTypeOption" style="width: 150px"
                      placeholder="过滤测点"/>
          </n-form-item>
          <n-form-item label="超阈值">
            <n-radio-group v-model:value="customConfig.superthreshold" name="superthreshold">
              <n-space>
                <n-radio key="true" :value="true">true</n-radio>
                <n-radio key="false" :value="false">false</n-radio>
              </n-space>
            </n-radio-group>
          </n-form-item>
          <n-form-item v-show="customConfig.superthreshold" label="阈值区间">
            <n-input v-model:value="customConfig.thresholdInterval" pair separator="-" placeholder="请输入阈值区间"/>
          </n-form-item>
          <n-form-item label="是否预警">
            <n-radio-group v-model:value="customConfig.alarmFlag" name="alarmFlag">
              <n-space>
                <n-radio key="true" :value="true">true</n-radio>
                <n-radio key="false" :value="false">false</n-radio>
              </n-space>
            </n-radio-group>
          </n-form-item>
        </n-form>
      </div>
      <template #action>
        <n-space>
          <n-button @click="emit('close')">取消</n-button>
          <n-button type="primary" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </n-spin>
</template>

<style scoped>

</style>