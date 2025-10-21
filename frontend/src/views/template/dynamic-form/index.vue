<script setup lang="ts">
import {reactive, ref, watch} from 'vue';
import {NCheckbox, NCollapse, NCollapseItem,NCode, type NForm} from "naive-ui";

// 字段类型定义
type FieldType = 'text' | 'number' | 'select' | 'checkbox' | 'radio' | 'textarea';
// 表单数据类型（动态key）
type FormData = Record<string, any>;

interface Option {
  label: string;
  value: string;
}

// 表单字段定义
interface FormField {
  key: string;
  label: string;
  type: FieldType;
  options?: Option[];
  required: boolean;
  disabled: boolean;
}

const newFieldType = ref<FieldType>('text');
const newFieldOptions = ref('');
const newFieldKey = ref('');
const newFieldLabel = ref('');
const formRef = ref<InstanceType<typeof NForm> | null>(null);
const formData = reactive<FormData>({});
const formFields = ref<FormField[]>([]);
// 弹窗状态
const showResultModal = ref(false);
const fieldTypeOptions = [
  {label: '文本输入', value: 'text'},
  {label: '数字输入', value: 'number'},
  {label: '下拉选择', value: 'select'},
  {label: '复选框', value: 'checkbox'},
  {label: '单选框', value: 'radio'},
  {value: 'textarea', label: '多行文本框',}
]
// 添加新字段
const addField = () => {
  // 检查key是否已存在
  if (formFields.value.some(field => field.key === newFieldKey.value)) {
    window.alert('字段标识已存在，请更换');
    return;
  }

  // 处理选项
  let options: Option[] = [];
  if (['select', 'radio', 'checkbox'].includes(newFieldType.value) && newFieldOptions.value) {
    options = newFieldOptions.value.split(',').map(option => ({
      label: option.trim(),
      value: option.trim()
    }));
  }

  // 添加新字段
  const newField: FormField = {
    key: newFieldKey.value,
    label: newFieldLabel.value,
    type: newFieldType.value,
    ...(options.length > 0 && {options}),
    required: false,
    disabled: false
  };

  formFields.value.push(newField);

  // 初始化表单数据
  if (newFieldType.value === 'checkbox') {
    formData[newFieldKey.value] = [];
  } else {
    formData[newFieldKey.value] = '';
  }

  // 重置表单
  resetNewFieldForm();
};
// 重置新增字段表单
const resetNewFieldForm = () => {
  newFieldKey.value = '';
  newFieldLabel.value = '';
  newFieldType.value = 'text';
  newFieldOptions.value = '';
};
// 移除字段
const removeField = (index: number) => {
  const removedField = formFields.value.splice(index, 1)[0];
  // 从表单数据中删除对应的字段
  // delete formData[removedField.key];
  if (removedField && removedField.key) {
    delete formData[removedField.key];
  }
};
// 切换字段禁用状态
const toggleFieldDisabled = (field: FormField) => {
  field.disabled = !field.disabled;
};
// 重置表单
const resetForm = () => {
  formFields.value = [];
  // 清空表单数据
  Object.keys(formData).forEach(key => {
    delete formData[key];
  });
  resetNewFieldForm();
};
// 验证表单
const validateForm = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
    showResultModal.value = true;
  } catch (errors) {
    console.log('表单验证失败:', errors);
  }
};
// 查看JSON数据
const showFormJson = () => {
  // showJsonModal.value = true;
  console.log({fields: formFields.value, data: formData.value})
};

// 监听字段变化，动态更新表单数据结构
watch(
    () => formFields.value,
    (newFields) => {
      // 移除已删除的字段
      Object.keys(formData).forEach(key => {
        if (!newFields.some(field => field.key === key)) {
          delete formData[key];
        }
      });

      // 初始化新增字段
      newFields.forEach(field => {
        if (formData[field.key] === undefined) {
          if (field.type === 'checkbox') {
            formData[field.key] = [];
          } else {
            formData[field.key] = '';
          }
        }
      });
    },
    {deep: true}
);
</script>

<template>
  <n-card title="动态表单构建器" class=" mx-auto">
    <!-- 表单配置区 -->
    <div class="mb-6">
      <n-space align="center" class="mb-4">
        <n-select v-model:value="newFieldType" :options="fieldTypeOptions" placeholder="选择字段类型"
                  style="width: 180px"/>
        <n-input
            v-model:value="newFieldKey"
            placeholder="输入字段标识 (key)"
            style="width: 180px"
        />
        <n-input
            v-model:value="newFieldLabel"
            placeholder="输入字段标签"
            style="width: 180px"
        />
        <n-button type="primary" :disabled="!newFieldKey || !newFieldLabel" @click="addField">添加字段</n-button>
      </n-space>
      <!-- 选项配置 (仅当下拉选择、单选、复选时显示) -->
      <n-collapse v-if="['select', 'radio', 'checkbox'].includes(newFieldType)">
        <n-collapse-item title="选项配置" name="options">
          <n-input
              v-model:value="newFieldOptions"
              placeholder="输入选项，用逗号分隔 (例如: 选项1,选项2,选项3)"
          />
          <n-text tag="p" type="info" class="mt-2 text-sm">
            提示: 选项将被转换为键值对，例如 "选项1" 会变成 {label: "选项1", value: "选项1"}
          </n-text>
        </n-collapse-item>
      </n-collapse>
    </div>
    <!-- 动态表单预览区 -->
    <div class="dynamic-form-preview border rounded-lg p-4 mb-6 bg-neutral-50 dark:bg-neutral-900/50">
      <n-form ref="formRef" :model="formData" label-placement="left"
              label-width="auto" size="small"
              require-mark-placement="right-hanging">
        <template v-for="(field, index) in formFields" :key="field.key">
          <div class="form-field-item relative">
            <!-- 字段操作按钮 -->
            <div class="field-actions absolute -top-8 right-0">
              <n-button
                  size="tiny"
                  type="error"
                  circle
                  @click="removeField(index)"
                  class="h-7 w-7"
              >
                <n-icon size="14">
                  <div class="i-ant-design:minus-circle-filled"/>
                </n-icon>
              </n-button>
            </div>
            <!-- 根据字段类型渲染不同的表单控件 -->
            <n-form-item :label="field.label"
                         :rules="field.required ? [{ required: true, message: '此字段为必填项' }] : []">
              <template v-if="field.type === 'text'">
                <n-input v-model:value="formData[field.key]" :placeholder="`请输入${field.label}`"/>
              </template>
              <template v-else-if="field.type === 'select'">
                <n-select v-model:value="formData[field.key]" :options="field.options" multiple clearable filterable
                          :placeholder="`请选择${field.label}`"/>
              </template>
            </n-form-item>
            <!-- 字段属性设置 -->
            <n-space size="small" class="mb-4">
              <n-checkbox v-model:checked="field.required" size="small">
                设为必填
              </n-checkbox>

              <n-button size="tiny" @click="toggleFieldDisabled(field)">
                {{ field.disabled ? '启用' : '禁用' }}
              </n-button>
            </n-space>
          </div>
        </template>
      </n-form>
    </div>
    <!-- 表单操作区 -->
    <div class="form-actions flex justify-between">
      <n-button
          type="warning"
          @click="resetForm"
          :disabled="formFields.length === 0"
      >
        清空表单
      </n-button>

      <n-space>
        <n-button
            @click="showFormJson"
            :disabled="formFields.length === 0"
        >
          查看JSON数据
        </n-button>

        <n-button
            type="primary"
            @click="validateForm"
            :disabled="formFields.length === 0"
        >
          提交表单
        </n-button>
      </n-space>
    </div>
    <!-- JSON数据弹窗 -->
    <n-modal
        v-model:show="showResultModal" preset="dialog"
        title="表单提交结果"
    >
      <n-code language="json">
        {{JSON.stringify(formData, null, 2)}}
      </n-code>
    </n-modal>
  </n-card>

</template>

<style scoped>
.form-field-item {
  margin-bottom: 1.5rem;
  padding-top: 1rem;
}

.field-actions {
  display: flex;
  gap: 0.5rem;
}

.form-actions {
  margin-top: 1rem;
}
</style>