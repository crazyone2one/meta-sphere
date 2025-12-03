<script setup lang="ts">
import {computed, ref, useAttrs, watch} from "vue";
import type {ConfirmValue} from "/@/components/RemoveButton.vue";
import type {FormInst, FormRules} from "naive-ui";

interface FieldConfig {
  field?: string;
  rules?: FormRules;
  placeholder?: string;
  maxLength?: number;
  isTextArea?: boolean;
  nameExistTipText?: string; // 添加重复提示文本
}

const props = withDefaults(
    defineProps<{
      title: string; // 文本提示标题
      subTitleTip?: string; // 子内容提示
      // type?: types;
      isDelete?: boolean; // 当前使用是否是移除
      loading?: boolean;
      okText?: string; // 确定按钮文本
      cancelText?: string;
      visible?: boolean; // 是否打开
      fieldConfig?: FieldConfig; // 表单配置项
      allNames?: string[]; // 添加或者重命名名称重复
      nodeId?: string; // 节点 id
    }>(),
    {
      type: 'warning',
      isDelete: true, // 默认移除pop
      okText: '移除',
    }
);
const emits = defineEmits<{
  (e: 'confirm', formValue: ConfirmValue, cancel?: () => void): void;
  (e: 'cancel'): void;
  (e: 'update:visible', visible: boolean): void;
}>();

const currentVisible = ref(props.visible || false);
const attrs = useAttrs();
const formRef = ref<FormInst | null>(null)
const form = ref({
  field: props.fieldConfig?.field || '',
});
// 获取当前标题的样式
const titleClass = computed(() => {
  return props.isDelete
      ? 'ml-2 font-medium text-[var(--color-text-1)] text-[14px]'
      : 'mb-[8px] font-medium text-[var(--color-text-1)] text-[14px] leading-[22px]';
});
// 重置
const reset = () => {
  form.value.field = '';
  formRef.value?.restoreValidation();
};
const handleCancel = () => {
  currentVisible.value = false;
  emits('cancel');
  reset();
};
const emitConfirm = () => emits('confirm', {...form.value, id: props.nodeId}, handleCancel);
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
watch(
    () => props.fieldConfig?.field,
    (val) => {
      form.value.field = val || '';
    }
);

watch(
    () => props.visible,
    (val) => {
      currentVisible.value = val;
    }
);

watch(
    () => currentVisible.value,
    (val) => {
      if (!val) {
        emits('cancel');
      }
      emits('update:visible', val);
    }
);
</script>

<template>
  <n-popover v-model:show="currentVisible" trigger="click" v-bind="attrs"
             :class="props.isDelete ? 'w-[352px]' : ''">
    <template #trigger>
      <slot name="trigger"></slot>
    </template>
    <div class="flex flex-row flex-nowrap items-center">
      <slot name="icon">
      </slot>
      <div :class="[titleClass]">
        {{ props.title || '' }}
      </div>
    </div>
    <!-- 描述展示 -->
    <div v-if="props.subTitleTip" class="ml-8 mt-2 text-sm text-[var(--color-text-2)]">
      {{ props.subTitleTip }}
    </div>
    <n-form v-else ref="formRef" :model="form" :rules="props.fieldConfig?.rules||{field:[
        { required: true, message: '名称不能为空！' },
    ]}">
      <n-form-item path="field">
        <n-input v-if="props.fieldConfig?.isTextArea" type="textarea" v-model:value="form.field"
                 :max-length="props.fieldConfig?.maxLength || 1000"/>
        <n-input v-else v-model:value="form.field" :placeholder="props.fieldConfig?.placeholder"
                 class="w-[245px]"/>
      </n-form-item>
    </n-form>
    <div class="mb-1 mt-4 flex flex-row flex-nowrap justify-end gap-2">
      <n-button secondary size="tiny" :disabled="props.loading" @click="handleCancel">
        {{ props.cancelText || '取消' }}
      </n-button>
      <n-button type="primary" size="tiny" :loading="props.loading" @click="handleConfirm">
        {{ props.okText || '确认' }}
      </n-button>
    </div>
  </n-popover>
</template>

<style scoped>

</style>