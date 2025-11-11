<script setup lang="ts">
import initOptionsFunc, {UserRequestTypeEnum} from "/@/components/user-selector/utils.ts";
import {onBeforeMount, ref, watch} from "vue";
import type {SelectOption} from "naive-ui";

const props = withDefaults(
    defineProps<{
      disabled?: boolean; // 是否禁用
      disabledKey?: string; // 禁用的key
      valueKey?: string; // value的key
      placeholder?: string;
      firstLabelKey?: string; // 首要的的字段key
      secondLabelKey?: string; // 次要的字段key
      loadOptionParams: Record<string, any>; // 加载选项的参数
      type?: UserRequestTypeEnum; // 加载选项的类型
      atLeastOne?: boolean; // 是否至少选择一个
    }>(),
    {
      disabled: false,
      disabledKey: 'disabled',
      valueKey: 'id',
      firstLabelKey: 'name',
      secondLabelKey: 'email',
      type: UserRequestTypeEnum.SYSTEM_USER_GROUP,
      atLeastOne: false,
    }
);
const innerValue = ref<string[]>([]);
const currentValue = defineModel<(string | number)[]>('modelValue', {default: []});
const options = ref<Array<SelectOption>>([])
const loadList = async () => {
  options.value = []
  const {keyword, ...rest} = props.loadOptionParams;
  options.value = (await initOptionsFunc(props.type, {keyword, ...rest})) || [];
}
const renderLabel = (option: SelectOption): string => {
  if (props.secondLabelKey) {
    return `${option[props.firstLabelKey]}(${option[props.secondLabelKey]})`
  }
  return option[props.firstLabelKey] as string;
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
  <n-select :options="options" :placeholder="props.placeholder||'请选择成员'" multiple clearable
            :label-field="props.firstLabelKey" :value-field="props.valueKey"
            :render-label="renderLabel"/>
</template>

<style scoped>

</style>