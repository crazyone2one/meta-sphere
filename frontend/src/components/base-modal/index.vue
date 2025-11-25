<script setup lang="ts">
import {actionClasses} from "/@/components/base-modal/utils.ts";

const showModal = defineModel<boolean>('showModal', {type: Boolean, default: false});
const {preset = 'dialog', okText = '确定', loading = false, disable = false} = defineProps<{
  preset?: 'dialog' | 'card';
  okText?: string,
  loading?: boolean
  disable?: boolean
}>();

const emit = defineEmits<{
  (e: 'cancel', param: boolean): void
  (e: 'update', value?: string): void
}>()
const handleCancel = (param: boolean) => {
  emit('cancel', param)
  showModal.value = false;
}
const handleSubmit = () => {
  emit('update', '')
  handleCancel(true)
}
</script>

<template>
  <n-modal v-model:show="showModal" :preset="preset" title="Dialog"
           :action-style="actionClasses"
           :modal-footer="false"
           :mask-closable="false"
           @close="handleCancel(false)">
    <template #header>
      <div>
        <slot name="title"></slot>
      </div>
    </template>
    <div>
      <slot></slot>
    </div>
    <template #action>
      <div class="flex flex-row items-center gap-[4px]">
        <slot name="actionLeft"></slot>
      </div>
      <div class="flex flex-row gap-[14px]">
        <slot name="actionRight"></slot>
        <n-button secondary :disabled="loading" @click="handleCancel(false)">取消</n-button>
        <n-button type="primary" :loading="loading" :disabled="disable" @click="handleSubmit">{{ okText }}</n-button>
      </div>
    </template>
  </n-modal>
</template>

<style scoped>

</style>