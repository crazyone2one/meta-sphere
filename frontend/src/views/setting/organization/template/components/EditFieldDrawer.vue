<script setup lang="ts">
import {useRoute} from "vue-router";
import type {
  AddOrUpdateField,
  DefinedFieldItem,
  fieldIconAndNameModal,
  FieldOptions,
  SceneType
} from "/@/api/modules/setting/template/types.ts";
import {computed, onMounted, type Ref, ref, watch, watchEffect} from "vue";
import {useAppStore} from "/@/store";
import type {FormInst} from "naive-ui";
import {dateOptions, fieldIconAndName, numberTypeOptions} from "/@/views/template/components/field-setting.ts";
import type {FormItemType} from "/@/api/types.ts";
import {cloneDeep} from "lodash-es";
import {useForm, useRequest} from "alova/client";
import {templateApi} from "/@/api/modules/setting/template";
import BatchForm from '/@/components/batch-form/index.vue'
import type {FormItemModel} from "/@/components/batch-form/types.ts";
import {getGenerateId} from "/@/utils";

const props = defineProps<{
  mode: 'organization' | 'project';
  data: DefinedFieldItem[];
}>();
const emit = defineEmits(['success']);
const active = defineModel<boolean>('active', {type: Boolean, default: false});
const route = useRoute();
const appStore = useAppStore();
// const sceneType = route.query.type;
const isEdit = ref<boolean>(false);
const currentOrgId = computed(() => appStore.currentOrgId);
const currentProjectId = computed(() => appStore.currentProjectId);
const formRef = ref<FormInst | null>(null)

const optionsModels: Ref<FormItemModel[]> = ref([]);
const batchFormRef = ref<InstanceType<typeof BatchForm>>();
const fieldDefaultValues = ref<FormItemModel[]>([]);
const fieldType = ref<FormItemType>(); // 整体字段类型
const onlyOptions: Ref<FormItemModel> = ref({
  field: 'text',
  type: 'input',
  label: '',
  rules: [
    {required: true, message: '选项内容不能为空'},
    {notRepeat: true, message: '选项内容不可以重复'},
  ],
  placeholder: '请输入选项',
  hideAsterisk: true,
  hideLabel: true,
});
const scopeId = computed(() => {
  return props.mode === 'organization' ? currentOrgId.value : currentProjectId.value;
});
const fieldOptions = ref<fieldIconAndNameModal[]>([]);
const initFieldForm: AddOrUpdateField = {
  name: '',
  used: false,
  type: undefined,
  remark: '',
  scopeId: scopeId.value,
  scene: 'SCHEDULE',
  options: [],
  enableOptionKey: false,
};
// const fieldForm = ref<AddOrUpdateField>({...initFieldForm});
const rules = {
  name: [{required: true, message: '字段名称不能为空', trigger: ['blur', 'change'],},],
  type: [{required: true, message: '字段类型不能为空', trigger: ['blur', 'change'],},],
  optionsModels: {message: '选项内容不能为空'},
};
const isMultipleSelectMember = ref<boolean | undefined>(false); // 成员多选
const selectNumber = ref<FormItemType>('INT'); // 数字格式
const selectFormat = ref<FormItemType>(); // 选择格式

const {form: fieldForm, loading, send} = useForm(formData => {
  if (props.mode === 'project') {
    return templateApi.addOrUpdateProjectField(formData)
  } else {
    return templateApi.addOrUpdateOrdField(formData)
  }
}, {
  initialForm: {...initFieldForm},
  immediate: false,
  resetAfterSubmiting: true,
})
const resetForm = () => {
  fieldForm.value = {...initFieldForm};
  selectFormat.value = undefined;
  isMultipleSelectMember.value = false;
  fieldType.value = undefined;
  batchFormRef.value?.resetForm();
}
const showOptionsSelect = computed(() => {
  const showOptionsType: FormItemType[] = ['RADIO', 'CHECKBOX', 'SELECT', 'MULTIPLE_SELECT'];
  return showOptionsType.includes(fieldForm.value.type as FormItemType);
});
const handleClose = () => {
  active.value = false;
  formRef.value?.restoreValidation()
  resetForm();
};
const formFiledValidate = (cb: () => void) => {
  formRef.value?.validate(errors => {
    if (errors) {
      return true;
    }
    if (showOptionsSelect.value) {
      batchFormRef.value?.formValidate((list: any) => {
        fieldDefaultValues.value = [...list];
        if (showOptionsSelect.value) {
          let startPos = 1;
          fieldForm.value.options = (batchFormRef.value?.getFormResult() || []).map((item: any) => {
            const currentItem: FieldOptions = {
              text: item.text,
              value: item.value ? item.value : getGenerateId(),
              pos: startPos,
            };
            if (item.fieldId) {
              currentItem.fieldId = item.fieldId;
            }
            startPos += 1;
            return currentItem;
          });
        }
        cb();
      })
    } else {
      cb();
    }
  })
}
const handleConfirm = (isContinue = false) => {
  const formCopy = cloneDeep(fieldForm.value);

  formCopy.scene = route.query.type as SceneType;
  formCopy.scopeId = scopeId.value;
  // 如果选择是日期
  if (selectFormat.value) {
    formCopy.type = selectFormat.value;
  }

  // 如果选择是成员（单选||多选）
  if (isMultipleSelectMember.value) {
    formCopy.type = isMultipleSelectMember.value ? 'MULTIPLE_MEMBER' : 'MEMBER';
  }
  // 如果选择是数值
  if (formCopy.type === 'NUMBER') {
    formCopy.type = selectNumber.value;
  }

  // 处理参数
  const {id, name, options, scene, type, remark, enableOptionKey} = formCopy;

  const params: AddOrUpdateField = {
    name,
    used: false,
    options,
    scopeId: scopeId.value,
    scene,
    type,
    remark,
    enableOptionKey,
  };
  if (id) {
    params.id = id;
  }
  send(params).then((res) => {
    window.$message.success(isEdit.value ? '更新成功' : '添加字段成功');
    if (!isContinue) {
      handleClose();
    }
    resetForm()
    emit('success', isEdit.value, res.id);
  })
}
const handleDrawerConfirm = (isContinue: boolean) => {
  formFiledValidate(() => handleConfirm(isContinue));
};
const getSpecialHandler = (itemType: FormItemType): FormItemType => {
  switch (itemType) {
    case 'INT':
      selectNumber.value = itemType;
      return 'NUMBER';
    case 'FLOAT':
      selectNumber.value = itemType;
      return 'NUMBER';
    case 'MULTIPLE_MEMBER':
      return 'MEMBER';
    case 'DATETIME':
      selectFormat.value = itemType;
      return 'DATE';
    default:
      selectFormat.value = itemType;
      return itemType;
  }
};
const {send: detail} = useRequest(id => {
  return props.mode === 'organization' ? templateApi.getOrgFieldDetail(id) : templateApi.getProjectFieldDetail(id)
}, {immediate: false, force: true})
const getFieldDetail = async (id: string) => {
  const fieldDetail = await detail(id);
  fieldForm.value = {
    ...fieldDetail,
    type: getSpecialHandler(fieldDetail.type),
  };
  fieldDefaultValues.value = fieldDetail.options?.map((item: any) => {
    return {
      ...item,
    };
  }) ?? [];
  if (fieldDefaultValues.value.length == 0) {
    optionsModels.value = [{...onlyOptions.value}];
  }
}
const handleEdit = (item: DefinedFieldItem) => {
  isMultipleSelectMember.value = item.type === 'MULTIPLE_MEMBER';
  if (item.id) {
    getFieldDetail(item.id);
  }
}
const fieldChangeHandler = () => {
  optionsModels.value = [{...onlyOptions.value}];
  fieldDefaultValues.value = [];
}
onMounted(() => {
  const excludeOptions = ['MULTIPLE_MEMBER', 'DATETIME', 'SYSTEM', 'INT', 'FLOAT'];
  fieldOptions.value = fieldIconAndName.filter((item: any) => excludeOptions.indexOf(item.key) < 0);
});
watch(() => fieldForm.value.enableOptionKey, (newValue) => {
  if (newValue) {
    optionsModels.value = [{...onlyOptions.value}];
  }
}, {immediate: true})
watchEffect(() => {
  isEdit.value = !!fieldForm.value.id;
});
defineExpose({
  handleEdit,
});
</script>

<template>
  <n-drawer v-model:show="active" :width="800" :mask-closable="false">
    <n-drawer-content>
      <template #header>
        {{ isEdit ? '更新字段' : '新增字段' }}
      </template>
      <n-form ref="formRef" :model="fieldForm" :rules="rules"
              label-placement="left" label-width="auto" require-mark-placement="right-hanging" size="small"
      >
        <n-form-item label="字段名称" path="name">
          <n-input v-model:value="fieldForm.name" placeholder="请输入字段名称" :disabled="fieldForm.internal"
                   :maxlength="255"/>
        </n-form-item>
        <n-form-item label="描述" path="remark">
          <n-input v-model:value="fieldForm.remark" type="textarea"
                   placeholder="请对该字段进行描述" :autosize="{minRows:1}"/>
        </n-form-item>
        <n-form-item label="字段类型" path="type">
          <n-select v-model:value="fieldForm.type" :options="fieldOptions" placeholder="请选择字段类型"
                    class="w-[260px]" clearable @update:value="fieldChangeHandler"/>
        </n-form-item>
        <n-form-item v-if="fieldForm.type === 'MEMBER'" label="允许添加多个成员" path="type">
          <n-switch v-model:value="isMultipleSelectMember" size="small" :disabled="isEdit"/>
        </n-form-item>
        <n-form-item v-if="showOptionsSelect" label="选项内容" path="optionsModels" class="relative"
                     :class="[!fieldForm?.enableOptionKey ? 'max-w-[340px]' : 'w-full']">
          <batch-form ref="batchFormRef"  form-mode="create"
                      add-text="添加一个选项" :is-show-drag="true"
                      :form-width="!fieldForm?.enableOptionKey ? '340px' : ''"
                      :default-vals="fieldDefaultValues"/>
        </n-form-item>
        <n-form-item v-if="fieldForm.type === 'NUMBER'" label="数字格式" path="selectNumber">
          <n-select v-model:value="selectNumber" :options="numberTypeOptions" placeholder="请选择格式" class="w-[260px]"
                    :disabled="isEdit"/>
        </n-form-item>
        <n-form-item v-if="fieldForm.type === 'DATE'" label="日期格式" path="selectFormat">
          <n-select v-model:value="selectFormat" :options="dateOptions" placeholder="请选择格式" class="w-[260px]"
                    :disabled="isEdit"/>
        </n-form-item>
      </n-form>
      <template #footer>
        <slot name="footer">
          <n-flex>
            <slot name="footerLeft"></slot>
            <n-button secondary :disabled="props.data.length>20 && !isEdit" @click="handleClose">取消</n-button>
            <n-button type="primary" :loading="loading" :disabled="props.data.length>20 && !isEdit"
                      @click="handleDrawerConfirm(false)">
              {{ isEdit ? '更新字段' : '新增字段' }}
            </n-button>
          </n-flex>
        </slot>
      </template>
    </n-drawer-content>
  </n-drawer>
</template>

<style scoped>

</style>