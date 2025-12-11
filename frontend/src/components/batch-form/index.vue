<script setup lang="ts">
import {ref, unref, watchEffect} from "vue";
import type {FormInst, FormItemRule} from "naive-ui";
import type {FormItemModel, FormMode} from "/@/components/batch-form/types.ts";
import {VueDraggable} from "vue-draggable-plus";
import {scrollIntoView} from "/@/utils/dom.ts";

export interface BatchFormProps {
  models: FormItemModel[];
  formMode: FormMode;
  addText?: string;
  maxHeight?: string;
  defaultVals?: any[]; // 当外层是编辑状态时，可传入已填充的数据
  isShowDrag?: boolean; // 是否可以拖拽
  formWidth?: string; // 自定义表单区域宽度
  showEnable?: boolean; // 是否显示启用禁用switch状态
  hideAdd?: boolean; // 是否隐藏添加按钮
  addToolTip?: string;
  enableType?: 'circle' | 'round' | 'line';
}

const props = withDefaults(defineProps<BatchFormProps>(), {
  maxHeight: '30vh',
  isShowDrag: false,
  hideAdd: false,
  enableType: 'line',
})
const formRef = ref<FormInst | null>(null)
const emit = defineEmits(['change']);
const defaultForm = {
  list: [] as Record<string, any>[],
};
const form = ref<Record<string, any>>({list: [...defaultForm.list]});
const formItem: Record<string, any> = {};
const getFormResult = () => {
  return unref<Record<string, any>[]>(form.value.list);
}
const formValidate = (cb: (res?: Record<string, any>[]) => void, isSubmit = true) => {
  formRef.value?.validate(err => {
    if (err) {
      scrollIntoView(document.querySelector('.n-form-item-feedback__line'), {block: 'center'});
      return;
    }
    if (typeof cb === 'function') {
      if (isSubmit) {
        cb(getFormResult());
        return;
      }
      cb();
    }
  })
}
const addField = () => {
  const newItem = {...formItem};
  if (newItem && Object.keys(newItem).length > 0) {
    formValidate(() => {
      form.value.list.push(newItem); // 序号自增，不会因为删除而重复
    }, false);
  }
}
const resetForm = () => {
  formRef.value?.restoreValidation();
}
watchEffect(() => {
  props.models.forEach((e) => {
    // 默认填充表单项
    let value: string | number | boolean | string[] | number[] | undefined;
    if (e.type === 'inputNumber') {
      value = undefined;
    } else if (e.type === 'tagInput') {
      value = [];
    } else {
      value = e.defaultValue;
    }
    formItem[e.field] = value;
    if (props.showEnable) {
      // 如果有开启关闭状态，将默认禁用
      formItem.enable = false;
    }
    // 默认填充表单项的子项
    e.children?.forEach((child) => {
      formItem[child.field] = child.type === 'inputNumber' ? null : child.defaultValue;
    });
  });
  form.value.list = [{...formItem}];
  if (props.defaultVals?.length) {
    // 取出defaultVals的表单 field
    form.value.list = props.defaultVals.map((e) => e);
  }
})
const fieldNotRepeat = (value: string | undefined, index: number, field: string, msg?: string) => {
  if (value === '' || value === undefined) return false;
  // 遍历其他同 field 名的输入框的值，检查是否与当前输入框的值重复
  for (let i = 0; i < form.value.list.length; i++) {
    if (i !== index && form.value.list[i][field].trim() === value) {
      return new Error(msg || '')
    }
  }
}
defineExpose({
  formValidate,
  getFormResult,
  resetForm,
  // setFields,
});
</script>

<template>
  <n-form ref="formRef" :model="form" size="small">
    <div class="mb-[16px] overflow-y-auto rounded-[4px] border border-[var(--color-text-n8)] p-[12px]"
         :style="{ width: props.formWidth || '100%' }">
      <n-scrollbar :style="{ 'max-height': props.maxHeight }">
        <VueDraggable v-model:model-value="form.list" ghost-class="ghost"
                      drag-class="dragChosenClass"
                      :disabled="!props.isShowDrag"
                      :force-fallback="true"
                      :animation="150"
                      handle=".dragIcon">
          <div v-for="(element, index) in form.list" :key="`${element.field}${index}`"
               :class="[props.isShowDrag ? 'cursor-move' : '']">
            <n-flex v-if="props.isShowDrag" align="center">
              <div class="i-mdi:drag block text-[16px] text-[#9597a4]"/>

              <n-form-item v-for="model of props.models" :key="`${model.field}-${index}`"
                           :path="`list[${index}].${model.field}`"
                           :class="index > 0 ? 'hidden-item' : 'mb-0 flex-1'"
                           :rule="model.rules?.map(e=>{
                              if(e.notRepeat===true){
                                return {
                                  validator: (_rule: FormItemRule, value: string) => fieldNotRepeat(value, index, model.field, e.message as string),
                                  trigger: 'input' // 添加触发时机
                                  };
                              }
                              return e;
                            })">
                <n-input v-if="model.type === 'input'" v-model:value="form.list[index][model.field]"
                         :placeholder="model.placeholder || ''"
                         :maxlength="model.maxLength||255" clearable
                         :disabled="model.disabled"
                         @change="emit('change')"/>
                <n-select v-else-if="model.type === 'select'" v-model:value="element[model.field]"
                          :options="model.options"/>
              </n-form-item>
              <div v-if="showEnable">
                <n-switch v-model:value="element.enable" class="mt-[8px]"
                          :style="{ 'margin-top': index === 0 && !props.isShowDrag ? '36px' : '' }"/>
              </div>
              <div v-if="!props.hideAdd"
                   v-show="form.list.length > 1" class="text-[16px] cursor-pointer"
                   :style="{ 'margin-top': index === 0 && !props.isShowDrag ? '30px' : '' }">
                <div class="i-mdi:close-circle-outline"/>
              </div>
            </n-flex>
          </div>
        </VueDraggable>
      </n-scrollbar>
      <div v-if="props.formMode === 'create' && !props.hideAdd" class="w-full">
        <n-popover trigger="hover" :disabled="!props.addToolTip">
          <template #trigger>
            <n-button class="px-0" text @click="addField">
              <template #icon>
                <div class="i-mdi:plus text-[14px] "/>
              </template>
              {{ props.addText || '添加' }}
            </n-button>
          </template>
          <span>{{ props.addToolTip }}</span>
        </n-popover>
      </div>
    </div>

  </n-form>
</template>

<style scoped>
</style>