<script setup lang="ts">
import {computed, ref, watch} from "vue";
import type {FormInst, FormItemRule} from "naive-ui";

export interface ConfirmValue {
  field: string;
  id?: string;
}

interface FieldConfig {
  field?: string;
  nameExistTipText?: string;
  placeholder?: string;
  isTextArea?: boolean;
}

const {
  title = '',
  isDelete = true,
  subTitleTip = '',
  okText = '移除',
  cancelText = '',
  loading = false,
  nodeId = '',
  fieldConfig = {},
  allNames = [],
  removeText = '移除',
  disabled = false
} = defineProps<{
  title: string;
  subTitleTip?: string;
  removeText?: string;
  isDelete?: boolean;
  disabled?: boolean;
  okText?: string;
  cancelText?: string;
  loading?: boolean;
  nodeId?: string;
  fieldConfig?: FieldConfig;
  allNames?: string[]
}>();
const emits = defineEmits<(e: 'confirm', formValue: ConfirmValue, cancel?: () => void) => void>();
const currentVisible = ref(false);
const form = ref({
  field: fieldConfig?.field || '',
});
// 获取当前标题的样式
const titleClass = computed(() => {
  return isDelete
      ? 'ml-2 font-medium text-[14px]'
      : 'mb-[8px] font-medium text-[14px] leading-[22px]';
});
const formRef = ref<FormInst | null>(null)
const reset = () => {
  form.value.field = '';
  formRef.value?.restoreValidation()
};
const handleCancel = () => {
  currentVisible.value = false;
  reset()
};
const emitConfirm = () => emits('confirm', {...form.value, id: nodeId}, handleCancel);
const handleConfirm = () => {
  if (!formRef.value) {
    emitConfirm();
    return;
  }
  formRef.value?.validate((errors) => {
    if (!errors) {
      emitConfirm();
    }
  });
};
const showPopover = () => {
  currentVisible.value = true;
}
const nameValidator = computed(() => {
  return {
    required: true, trigger: 'blur', validator: (_rule: FormItemRule, value: string) => {
      if ((allNames || []).includes(value)) {
        if (fieldConfig && fieldConfig.nameExistTipText) {
          return new Error(fieldConfig.nameExistTipText);
        } else {
          return new Error('名称已存在！');
        }
      }
      return true
    }
  }
})

watch(
    () => fieldConfig?.field,
    (val) => {
      form.value.field = val || '';
    }
);

</script>

<template>
  <n-popover trigger="click" :show="currentVisible" type="warning">
    <template #trigger>
      <n-button type="primary" text :disabled="disabled" @click="showPopover">{{ removeText }}</n-button>
    </template>
    <template #default>
      <div class="flex flex-row flex-nowrap items-center">
        <slot name="icon">
          <div v-if="isDelete" class="mr-[2px] text-xl i-ant-design:warning-filled text-orange"/>
        </slot>
        <div :class="[titleClass]">
          {{ title || '' }}
        </div>
      </div>
      <!-- 描述展示 -->
      <div v-if="subTitleTip" class="ml-8 mt-2 text-sm text-[var(--color-text-2)]">
        {{ subTitleTip }}
      </div>
      <n-form v-else ref="formRef" :model="form">
        <n-form-item path="field" :rule="nameValidator">
          <n-input v-model:value="form.field" :placeholder="fieldConfig.placeholder" class="w-[245px]"/>
        </n-form-item>
      </n-form>
      <div class="mb-1 mt-4 flex flex-row flex-nowrap justify-end gap-2">
        <n-button secondary size="tiny" :disabled="loading" @click="handleCancel">
          {{ cancelText || '取消' }}
        </n-button>
        <n-button type="primary" size="tiny" :loading="loading" @click="handleConfirm">
          {{ okText || '确认' }}
        </n-button>
      </div>
    </template>
  </n-popover>
</template>

<style scoped>
.n-popover__content {
  padding: 16px;
}
</style>