<script setup lang="ts">
import type {AddOrUpdateField, DefinedFieldItem, fieldIconAndNameModal} from "/@/api/modules/setting/template/types.ts";
import {computed, h, onMounted, ref, watchEffect} from "vue";
import {useAppStore} from "/@/store";
import type {FormItemType} from "/@/api/types.ts";
import {dateOptions, fieldIconAndName, numberTypeOptions} from "/@/views/template/components/field-setting.ts";
import type {FormInst, FormRules, SelectOption} from "naive-ui";
import {useForm} from "alova/client";
import {templateApi} from "/@/api/modules/setting/template";

const {mode = 'project', data = []} = defineProps<{
  mode: 'organization' | 'project';
  data: DefinedFieldItem[];
}>()
const active = defineModel<boolean>('active', {type: Boolean, default: false});
const isEdit = ref<boolean>(false);
const formRef = ref<FormInst | null>(null)
const appStore = useAppStore();
const currentOrgId = computed(() => appStore.currentOrgId);
const currentProjectId = computed(() => appStore.currentProjectId);
const scopeId = computed(() => {
  return mode === 'organization' ? currentOrgId.value : currentProjectId.value;
});
const initFieldForm: AddOrUpdateField = {
  name: '',
  used: false,
  type: undefined,
  remark: '',
  scopeId: scopeId.value,
  scene: 'FUNCTIONAL',
  options: [],
  enableOptionKey: false,
};
// 字段类型列表选项
const fieldOptions = ref<fieldIconAndNameModal[]>([]);
// const fieldForm = ref<AddOrUpdateField>({...initFieldForm});
// 是否展示选项添加面板
const showOptionsSelect = computed(() => {
  const showOptionsType: FormItemType[] = ['RADIO', 'CHECKBOX', 'SELECT', 'MULTIPLE_SELECT'];
  return showOptionsType.includes(fieldForm.value.type as FormItemType);
});
const rules: FormRules = {
  name: [{required: true, message: '字段名称不能为空'}],
  type: [{required: true, message: '字段类型不能为空'}],
}
const selectNumber = ref<FormItemType>('INT'); // 数字格式
const selectFormat = ref<FormItemType>(); // 选择格式
const resetForm = () => {
  fieldForm.value = {...initFieldForm};
  selectFormat.value = undefined;
  // isMultipleSelectMember.value = false;
  // fieldType.value = undefined;
  // batchFormRef.value?.resetForm();
};
const handleCancel = () => {
  formRef.value?.restoreValidation()
  active.value = false;
  resetForm()
}
const {
  loading,
  form: fieldForm,
  send: addOrUpdate
} = useForm((formData) => templateApi.addOrUpdateProjectField(formData), {
  initialForm: {...initFieldForm},
  resetAfterSubmiting: true
})
const handleContinue = () => {

};
const handleOk = () => {
  formRef.value?.validate(errors => {
    if (!errors) {
      addOrUpdate(fieldForm.value).then(() => {
        window.$message.success(isEdit.value ? '更新成功' : '保存成功');
        handleCancel()
        resetForm();
        // emit('success', isEdit.value, res.id);
      })
    }
  })
};
const renderLabel = (option: SelectOption) => {
  return h('div', {class: 'flex items-center'}, {
    default: () => [
      h('div', {class: option.iconName}),
      h('div', {}, {default: () => option.label})
    ]
  })
}
onMounted(() => {
  const excludeOptions = ['MULTIPLE_MEMBER', 'DATETIME', 'SYSTEM', 'INT', 'FLOAT'];
  fieldOptions.value = fieldIconAndName.filter((item: any) => excludeOptions.indexOf(item.key) < 0);
});
watchEffect(() => {
  isEdit.value = !!fieldForm.value.id;
});
</script>

<template>
  <n-drawer
      v-model:show="active"
      :default-width="800"
      resizable
  >
    <n-drawer-content>
      <template #header>
        {{ isEdit ? '更新字段' : '新增字段' }}
      </template>
      <div class="form">
        <n-form
            ref="formRef"
            :model="fieldForm"
            :rules="rules"
            label-placement="left"
            label-width="auto"
            require-mark-placement="right-hanging"
        >
          <n-form-item label="字段名称" path="name">
            <n-input v-model:value="fieldForm.name" placeholder="请输入字段名称" :maxlength="255"/>
          </n-form-item>
          <n-form-item label="描述" path="remark">
            <n-input type="textarea" v-model:value="fieldForm.remark" placeholder="请对该字段进行描述" :maxlength="1000"
                     :autosize="{minRows:1}" style="resize: vertical"/>
          </n-form-item>
          <n-form-item label="字段类型" path="type">
            <n-select v-model:value="fieldForm.type" :options="fieldOptions" placeholder="请选择字段类型"
                      :render-label="renderLabel"/>
          </n-form-item>
          <n-form-item v-if="fieldForm.type === 'MEMBER'" label="允许添加多个成员" path="type">
            <n-switch size="small" :disabled="isEdit"/>
          </n-form-item>
          <n-form-item v-if="showOptionsSelect" label="选项内容" path="optionsModels">
            待处理。。。
          </n-form-item>
          <n-form-item v-if="fieldForm.type === 'NUMBER'" label="数字格式" path="selectNumber">
            <n-select v-model:value="selectNumber" :options="numberTypeOptions"
                      :default-value="numberTypeOptions[0]?.value" placeholder="请选择格式"
                      clearable :disabled="isEdit"/>
          </n-form-item>
          <n-form-item v-if="fieldForm.type === 'DATE'" label="日期格式" path="selectFormat">
            <n-select v-model:value="selectFormat" :options="dateOptions"
                      :default-value="dateOptions[0]?.value" placeholder="请选择格式"
                      clearable :disabled="isEdit"/>
          </n-form-item>
        </n-form>
      </div>
      <template #footer>
        <div class="flex items-center justify-between">
          <slot name="footerLeft"></slot>
          <div class="ml-auto flex gap-[12px]">
            <n-button :disabled="loading" @click="handleCancel">
              取消
            </n-button>
            <n-button
                v-if="!isEdit && data.length < 20"
                secondary
                :loading="loading"
                :disabled="data.length >= 20 && !isEdit"
                @click="handleContinue"
            >
              保存并继续添加
            </n-button>
            <n-button
                type="primary"
                :disabled="data.length >= 20 && !isEdit"
                :loading="loading"
                @click="handleOk"
            >
              {{ isEdit ? '更新字段' : '新增字段' }}
            </n-button>
          </div>
        </div>
      </template>
    </n-drawer-content>
  </n-drawer>
</template>

<style scoped>

</style>