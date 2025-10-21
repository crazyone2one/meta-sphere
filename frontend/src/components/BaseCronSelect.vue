<script setup lang="ts">
const {size = 'medium', placeholder = '可直接输入cron表达式'} = defineProps<{
  size?: 'tiny' | 'small' | 'medium' | 'large',
  placeholder?: string
}>()
const cron = defineModel<string>('modelValue', {
  required: true,
});
const options = [
  {label: '(每 1 分钟)', value: '0 0/1 * * * ?'},
  {label: '(每 5 分钟)', value: '0 0/5 * * * ?'},
  {label: '(每 10 分钟)', value: '0 0/10 * * * ?'},
  {label: '(每小时)', value: '0 0 0/1 * * ?'},
  {label: '(每 6 小时)', value: '0 0 0/6 * * ?'},
  {label: '(每 12 小时)', value: '0 0 0/12 * * ?'},
  {label: '(每天)', value: '0 0 0 * * ?'},
]
const emit = defineEmits<{
  (
      e: 'changeCron',
      value: string
  ): void;
}>();
</script>

<template>
  <n-select
      v-model:value="cron"
      tag
      filterable
      :placeholder='placeholder'
      :options="options"
      :size="size"
      @update:value="emit('changeCron', $event)"
      :fallback-option="(val:string)=>({label: `${val}`,value: val})"
  />
</template>

<style scoped>

</style>