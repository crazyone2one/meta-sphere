<script setup lang="ts">
import {ref} from "vue";
import type {FormInst} from "naive-ui";

const {visible = false, id = undefined} = defineProps<{ visible: boolean, id?: string }>()
const emit = defineEmits<{
  (e: 'cancel', value: boolean): void;
  (e: 'submit', currentId: string): void;
}>();
const formRef = ref<FormInst | null>(null)
const form = ref({
  name: '',
});
const handleCancel = () => {
  form.value.name = '';
  emit('cancel', false);
};
const handleSubmit = () => {

};
const handleOutsideClick = () => {
  if (visible) {
    handleCancel();
  }
}
</script>

<template>
  <n-popover :show="visible" trigger="click" placement="bottom-end" class="w-[350px]">
    <template #trigger>
      <slot></slot>
    </template>
    <div v-outer="handleOutsideClick">
      <div class="form">
        <n-form ref="formRef" :model="form" label-placement="top">
          <div class="mb-[8px] text-[14px] font-medium">
            {{ id ? '重命名' : '创建用户组' }}
          </div>
          <n-form-item path="name">
            <n-input v-model:value="form.name" placeholder="请输入用户组名称" clearable :maxlength="255"
                     @keyup.esc="handleCancel"/>
            <span v-if="!id" class="mt-[8px] text-[13px] font-medium">该用户组将在整个系统范围内可用</span>
          </n-form-item>
        </n-form>
      </div>
      <div class="flex flex-row flex-nowrap justify-end gap-2">
        <n-button secondary size="tiny" @click="handleCancel">取消</n-button>
        <n-button type="primary" size="tiny" :disabled="form.name.length === 0" @click="handleSubmit">
          {{ id ? '确认' : '创建' }}
        </n-button>
      </div>
    </div>
  </n-popover>
</template>

<style scoped>

</style>