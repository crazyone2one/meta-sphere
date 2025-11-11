<script setup lang="ts">
import {type DropdownOption} from "naive-ui";

const {options = []} = defineProps<{ options: Array<DropdownOption> }>()
const emit = defineEmits(['select']);
const handleSelect = (key: string) => {
  emit('select', key)
}
</script>

<template>
  <n-dropdown
      :options="options"
      placement="bottom-start"
      trigger="click"
      @select="handleSelect"
  >
    <n-button text>
      <n-icon :size="20">
        <div class="i-ant-design:ellipsis-outlined"/>
      </n-icon>
    </n-button>
  </n-dropdown>
</template>

<style scoped>

</style>