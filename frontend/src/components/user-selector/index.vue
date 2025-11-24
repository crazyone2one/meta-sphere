<script setup lang="ts">
import initOptionsFunc, {UserRequestTypeEnum} from "/@/components/user-selector/utils.ts";
import {onBeforeMount, ref, watch} from "vue";
import type {SelectOption} from "naive-ui";
import type {iLoadOptionParams} from "/@/api/types.ts";

const {
  loadOptionParams = {keyword: ''},
  type = UserRequestTypeEnum.SYSTEM_USER_GROUP,
  secondLabelKey = 'email',
  firstLabelKey = 'name',
  valueKey = 'id'
} = defineProps<{
  disabled?: boolean; // 是否禁用
  disabledKey?: string; // 禁用的key
  valueKey?: string; // value的key
  placeholder?: string;
  firstLabelKey?: string; // 首要的的字段key
  secondLabelKey?: string; // 次要的字段key
  loadOptionParams?: iLoadOptionParams; // 加载选项的参数
  type?: UserRequestTypeEnum; // 加载选项的类型
  atLeastOne?: boolean; // 是否至少选择一个
}>()
const innerValue = ref<string[]>([]);
const currentValue = defineModel<(string | number)[]>('modelValue', {default: []});
const options = ref<Array<SelectOption>>([])
const loadList = async () => {
  options.value = []
  const {keyword, ...rest} = loadOptionParams;
  options.value = (await initOptionsFunc(type, {keyword, ...rest})) || [];
}
const renderLabel = (option: SelectOption): string => {
  if (secondLabelKey) {
    return `${option[firstLabelKey]}(${option[secondLabelKey]})`
  }
  return option[firstLabelKey] as string;
}
watch(
    () => innerValue.value,
    (value) => {
      const values: (string | number)[] = [];
      for (const item of value) {
        values.push(item);
      }
      currentValue.value = values;
    }
);
onBeforeMount(() => {
  loadList()
})
</script>

<template>
  <n-select v-model:value="innerValue" :options="options" :placeholder="placeholder||'请选择成员'" multiple
            clearable
            :label-field="firstLabelKey" :value-field="valueKey"
            :render-label="renderLabel"/>
</template>

<style scoped>

</style>