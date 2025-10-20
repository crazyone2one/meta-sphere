<script setup lang="ts">

import {ref, watch} from "vue";

const showModal = defineModel<boolean>('showModal', {type: Boolean, default: false});
const sensorOptions = ref([
  {
    label: '1',
    value: '1'
  },
  {
    label: '2',
    value: '2'
  },
  {
    label: '3',
    value: '3'
  },
])
let customConfig = ref<Record<string, any>>({
  sensorIds: [],
  superthreshold: false, //超阈值
  thresholdInterval: '', //阈值区间
  alarmFlag: false //是否预警
})
const {config = {}} = defineProps<{
  config?: Record<string, any>
}>()
const emit = defineEmits<{
  (e: 'updateConfig', value: Record<string, any>, key: string): void;
  (e: 'close'): void;
}>();
const handleSubmit = () => {
  emit('updateConfig', customConfig.value, "customConfig")
  emit('close')
}
watch(() => config, (newValue) => {
  customConfig.value = {...newValue}
}, {deep: true})
</script>

<template>
  <n-modal v-model:show="showModal" preset="dialog" title="Dialog">
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
          <n-select v-model:value="customConfig.sensorIds" :options="sensorOptions" multiple
                    placeholder="选择测点sensor id"/>
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
        <n-button>取消</n-button>
        <n-button type="primary" @click="handleSubmit">确定</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>

</style>