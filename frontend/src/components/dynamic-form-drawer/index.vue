<script setup lang="ts">
import {
  NCheckbox,
  NCheckboxGroup,
  NCode,
  NCollapse,
  NCollapseItem,
  NDrawer,
  NDrawerContent,
  type NForm,
  NInputNumber
} from "naive-ui";
import {computed, reactive, ref, watch} from 'vue';
import CustomConfigModal from "/@/views/schedule/components/CustomConfigModal.vue";
import type {ICustomConfig} from "/@/api/modules/schedule/types.ts";

// 字段类型定义
type FieldType = 'text' | 'number' | 'select' | 'checkbox' | 'radio' | 'textarea' | 'custom';
// 表单数据类型（动态key）
export type FormData = Record<string, any>;

interface Option {
  label: string;
  value: string;
}

// 表单字段定义
interface FormField {
  key: string;
  label: string;
  type?: FieldType;
  options?: Option[];
  required?: boolean;
  disabled?: boolean;
}

const customConfigModalRef = ref<InstanceType<typeof CustomConfigModal>>();
const showCustomConfigModalVisible = ref(false);
const showModal = defineModel<boolean>('showModal', {type: Boolean, default: false});

const {config = {}, resourceType = false} = defineProps<{
  config: FormData,
  resourceType?: boolean,
  sensorType?: string
}>()
const emit = defineEmits<{
  (e: 'close'): void,
  (e: 'update', value: Record<string, any>): void;
  (e: 'updateConfig', value: ICustomConfig): void;
}>();
const newFieldType = ref<FieldType>('text');
const newFieldOptions = ref('');
const newFieldKey = ref('');
const newFieldLabel = ref('');
const formRef = ref<InstanceType<typeof NForm> | null>(null);
const formData = reactive<FormData>({});
const formFields = ref<FormField[]>([]);

const fieldTypeOptions = computed(() => {
  const options = [
    {label: '文本输入', value: 'text'},
    {label: '数字输入', value: 'number'},
    {label: '下拉选择', value: 'select'},
    {label: '复选框', value: 'checkbox'},
    {label: '单选框', value: 'radio'},
    {value: 'textarea', label: '多行文本框',}
  ]
  if (resourceType) {
    options.push({label: '特殊值', value: 'custom'})
  }
  return options
})
// 添加新字段
const addField = () => {
  if (newFieldType.value === 'custom') {
    showCustomConfigModalVisible.value = true;
  } else {
    // 检查key是否已存在
    if (formFields.value.some(field => field.key === newFieldKey.value)) {
      window.$message.warning('字段标识已存在，请更换');
      return;
    }

    // 处理选项
    let options: Option[] = [];
    if (['select', 'checkbox'].includes(newFieldType.value) && newFieldOptions.value) {
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
    if (['select', 'checkbox'].includes(newFieldType.value)) {
      formData[newFieldKey.value] = [];
    } else {
      formData[newFieldKey.value] = newFieldType.value === 'number' ? 0 : '';
    }
    // 重置表单
    resetNewFieldForm();
  }
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
const handleUpdateConfig = (config: ICustomConfig, key: string) => {
  formData[key] = config;
  emit('updateConfig', config)
};
const handleCloseConfigModal = () => {
  showCustomConfigModalVisible.value = false;
}
// 监听字段变化，动态更新表单数据结构
watch(
    () => formFields.value,
    (newFields) => {
      // 移除已删除的字段
      if (formData) {
        Object.keys(formData).forEach(key => {
          if (!newFields.some(field => field.key === key)) {
            delete formData[key];
          }
        });
      }
      // 初始化新增字段
      newFields.forEach(field => {
        if (formData[field.key] === undefined) {
          if (field.type === 'checkbox') {
            // 检查原始配置中是否为布尔值或表示布尔值的字符串
            const originalValue = config[field.key];
            if (typeof originalValue === 'boolean') {
              formData[field.key] = originalValue;
            } else if (typeof originalValue === 'string') {
              const lowerValue = originalValue.toLowerCase();
              if (['yes', 'true', '1'].includes(lowerValue)) {
                formData[field.key] = true;
              } else if (['no', 'false', '0'].includes(lowerValue)) {
                formData[field.key] = false;
              } else {
                formData[field.key] = false; // 默认值
              }
            } else {
              formData[field.key] = [];
            }
          } else if (field.type === 'number') {
            formData[field.key] = 0;
          } else {
            formData[field.key] = '';
          }
        }
      });
    },
    {deep: true}
);
watch(() => config, (newValue) => {
  if (newValue) {
    // 清空现有数据
    resetForm();
    Object.keys(newValue).forEach(key => {
      // 设置表单数据
      formData[key] = config[key];
      // 处理非标准布尔值（如'yes'/'no'）
      if (typeof config[key] === 'string') {
        const lowerValue = config[key].toLowerCase();
        if (lowerValue === 'yes' || lowerValue === 'true' || lowerValue === '1') {
          formData[key] = true;
        } else if (lowerValue === 'no' || lowerValue === 'false' || lowerValue === '0') {
          formData[key] = false;
        }
      }
      // 推断字段类型
      let fieldType: FieldType;
      if (typeof config[key] === 'number') {
        fieldType = 'number';
      } else if (Array.isArray(config[key])) {
        fieldType = 'select';
      } else if (typeof config[key] === 'boolean' ||
          (typeof config[key] === 'string' && ['yes', 'no', 'true', 'false', '1', '0'].includes(config[key].toLowerCase()))) {
        fieldType = 'radio'; // 布尔值用复选框表示
      } else if (typeof config[key] === 'string' && (key.includes('Ids') || key.includes('type') || key.includes('category'))) {
        // 通过key的命名规则推断可能是select类型
        fieldType = 'select';
      } else if (typeof config[key] === 'string') {
        fieldType = 'text';
      } else {
        fieldType = 'custom';
      }
      // 创建表单字段
      const field: FormField = {
        key: key,
        label: key, // 默认使用key作为标签名
        type: fieldType,
        required: false,
        disabled: false
      };

      formFields.value.push(field);
    })
  }
}, {immediate: true})
const handleClose = () => {
  emit('close')
  resetForm()
}
const handleSubmit = () => {
  if (!formRef.value) return;
  formRef.value.validate(errors => {
    if (!errors) {
      emit('update', formData)
    }
  })
}
const addFieldButtonDisabled = computed(() => {
  if (newFieldType.value === 'custom') {
    return false
  }
  return !newFieldKey.value || !newFieldLabel.value
});
let customConfig = reactive<ICustomConfig>({
  alarmFlag: false,
  sensorIds: '',
  superthreshold: false,
  thresholdInterval: ''
})
const handleUpdateCustomConfig = (config: ICustomConfig) => {
  customConfig = {...config}
  showCustomConfigModalVisible.value = true;
}
</script>

<template>
  <n-drawer v-model:show="showModal" :width="750" :mask-closable="false">
    <n-drawer-content>
      <template #header>
        动态表单构建器
      </template>
      <div class="mb-6">
        <n-space align="center" class="mb-4">
          <n-select v-model:value="newFieldType" :options="fieldTypeOptions" placeholder="选择字段类型"
                    style="width: 120px" size="small"/>
          <n-input v-model:value="newFieldKey" placeholder="输入字段标识 (key)" style="width: 140px" size="small"
                   :disabled="newFieldType === 'custom'"/>
          <n-input v-model:value="newFieldLabel" placeholder="输入字段标签" style="width: 140px" size="small"
                   :disabled="newFieldType === 'custom'"/>
          <n-button type="primary" size="small" :disabled="addFieldButtonDisabled" @click="addField">
            {{ addFieldButtonDisabled ? '添加字段' : '录入自定义参数' }}
          </n-button>
        </n-space>
        <!-- 选项配置 (仅当下拉选择、单选、复选时显示) -->
        <n-collapse v-if="['select', 'checkbox'].includes(newFieldType)" :expanded-names="'options'">
          <n-collapse-item title="选项配置" name="options">
            <n-input
                v-model:value="newFieldOptions"
                placeholder="输入选项，用逗号分隔 (例如: 选项1,选项2,选项3)"
                size="small"
            />
            <n-text tag="p" type="info" class="mt-2 text-sm">
              提示: 选项将被转换为键值对，例如 "选项1" 会变成 {label: "选项1", value: "选项1"}
            </n-text>
          </n-collapse-item>
        </n-collapse>
      </div>
      <!-- 动态表单预览区 -->
      <div class="border rounded-lg p-4  bg-neutral-50 dark:bg-neutral-900/50">
        <n-form ref="formRef" :model="formData" label-placement="left"
                label-width="auto" size="small"
                require-mark-placement="right-hanging">
          <template v-for="(field, index) in formFields" :key="field.key">
            <n-space justify="space-between">

              <!-- 根据字段类型渲染不同的表单控件 -->
              <n-form-item :label="field.label"
                           :rules="field.required ? [{ required: true, message: '此字段为必填项' }] : []">
                <template v-if="field.type === 'text'">
                  <n-input v-model:value="formData[field.key]" :placeholder="`请输入${field.label}`"
                           style="width: 300px"/>
                </template>
                <template v-else-if="field.type === 'select'">
                  <n-select v-model:value="formData[field.key]" :options="field.options" multiple clearable filterable
                            :placeholder="`请选择${field.label}`" style="width: 300px"/>
                </template>
                <template v-else-if="field.type === 'number'">
                  <n-input-number
                      v-model:value="formData[field.key]"
                      :placeholder="`请输入${field.label}`"
                  />
                </template>
                <template v-else-if="field.type === 'radio'">
                  <n-radio-group v-model:value="formData[field.key]">
                    <n-radio :value="true">true</n-radio>
                    <n-radio :value="false">false</n-radio>
                  </n-radio-group>
                </template>
                <template v-else-if="field.type === 'checkbox'">
                  <n-checkbox-group v-model:value="formData[field.key]">
                    <n-checkbox
                        v-for="option in field.options"
                        :key="option.value"
                        :value="option.value"
                    >
                      {{ option.label }}
                    </n-checkbox>
                  </n-checkbox-group>
                </template>
                <template v-else-if="field.type === 'custom'">
                  <n-code :code="JSON.stringify(formData[field.key], null, 1)" language="json"/>
                </template>
              </n-form-item>

              <!-- 字段属性设置 -->
              <n-space size="small">
                <div v-if="field.key!=='customConfig'">
                  <n-checkbox v-model:checked="field.required" size="small">
                    设为必填
                  </n-checkbox>
                </div>
                <div v-else>
                  <n-button size="tiny" @click="handleUpdateCustomConfig(formData[field.key])">
                    编辑
                  </n-button>
                </div>
                <n-button size="tiny" @click="toggleFieldDisabled(field)">
                  {{ field.disabled ? '启用' : '禁用' }}
                </n-button>
                <!-- 字段操作按钮 -->
                <n-button size="tiny" type="error" text @click="removeField(index)">
                  <n-icon :size="20">
                    <div class="i-ant-design:minus-circle-filled"/>
                  </n-icon>
                </n-button>
              </n-space>
            </n-space>
          </template>
        </n-form>
      </div>
      <template #footer>
        <n-space>
          <n-button @click="handleClose">取消</n-button>
          <n-button type="primary" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-drawer-content>
  </n-drawer>
  <custom-config-modal ref="customConfigModalRef" v-model:show-modal="showCustomConfigModalVisible"
                       :config="customConfig"
                       @update-config="handleUpdateConfig"
                       @close="handleCloseConfigModal"/>
</template>

<style scoped>

</style>