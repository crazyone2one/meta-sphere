<script setup lang="ts">
import type {IScheduleInfo} from "/@/api/modules/schedule/types.ts";
import BaseCronSelect from "/@/components/BaseCronSelect.vue";
import {watch} from "vue";
import {useForm} from "alova/client";
import {scheduleApi} from "/@/api/modules/schedule";

const emit = defineEmits<{
  (e: 'close', shouldSearch: boolean, type: string): void;
}>();
const showModal = defineModel<boolean>('showModal', {type: Boolean, default: false});
const props = defineProps<{ task: IScheduleInfo }>()
const handleClose = (search: boolean) => {
  emit('close', search, 'config')
}

const addConfigItem = () => {
  const newKey = `param${Object.keys(model.value.runConfig).length + 1}`;
  model.value.runConfig[newKey] = '';

};
const updateConfigKey = (oldKey: string | number, newKey: string) => {
  if (oldKey === newKey || !newKey) return;
  const {[oldKey]: value, ...rest} = model.value.runConfig;
  model.value.runConfig = {...rest, [newKey]: value};
};
const removeConfigItem = (key: string | number) => {
  if (Object.keys(model.value.runConfig).length <= 1) {
    // 防止删除所有配置项
    return;
  }
  const {[key]: removed, ...rest} = model.value.runConfig;
  model.value.runConfig = rest;
};
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
        <n-scrollbar style="max-height: 240px">
          <n-form-item v-for="(_value, key) in model.runConfig" :key="key">
            <n-grid :cols="12" :x-gap="12">
              <n-form-item-gi :span="5">
                <n-input :value="String(key)" @update:value="(newKey) => updateConfigKey(key, newKey)"/>
              </n-form-item-gi>
              <n-form-item-gi :span="5">
                <n-input v-model:value="model.runConfig[key]"
                         placeholder="参数值"
                         clearable
                />
              </n-form-item-gi>
              <n-form-item-gi :span="2">
                <n-button text type="warning" @click="removeConfigItem(key)">
                  删除
                </n-button>
              </n-form-item-gi>
            </n-grid>
          </n-form-item>
        </n-scrollbar>

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
</template>

<style scoped>
.n-divider:not(.n-divider--vertical) {
  margin-bottom: 24px;
  margin-top: 1px;
}
</style>