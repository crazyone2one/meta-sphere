<script setup lang="ts">
import {nextTick, type PropType, ref} from "vue";
import type {InputInst} from "naive-ui";
import {hasAnyPermission} from "/@/utils/permission.ts";

interface OnUpdateValue {
  (value: string): void
}

const props = defineProps({
  value: {
    type: String,
    required: true,
  },
  onUpdateValue: {
    type: Function as PropType<OnUpdateValue>,
    required: true,
  },
  permission: {
    type: Array<string>,
    required: true,
  },
})
const isEdit = ref(false)
const inputRef = ref<InputInst | null>(null)
const inputValue = ref(props.value)
const handleOnClick = () => {
  if (hasAnyPermission(props.permission)) {
    isEdit.value = true
    nextTick(() => {
      inputRef.value?.focus()
    })
  }
}
const handleChange = () => {
  props.onUpdateValue?.(String(inputValue.value))
  isEdit.value = false
}
</script>

<template>
  <div style="min-height: 22px" @click="handleOnClick">
    <n-input ref="inputRef" v-if="isEdit" v-model:value="inputValue" @update:value="v=>inputValue=v"
             @change="handleChange" @blur="handleChange"/>
    <span v-else>{{ props.value }}</span>
  </div>
</template>

<style scoped>

</style>