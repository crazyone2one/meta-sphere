<script setup lang="ts">
import {useRoute} from "vue-router";
import type {
  AddOrUpdateField,
  DefinedFieldItem,
  fieldIconAndNameModal,
  SceneType
} from "/@/api/modules/setting/template/types.ts";
import {computed, onMounted, ref} from "vue";
import {useAppStore} from "/@/store";
import type {FormInst} from "naive-ui";
import {dateOptions, fieldIconAndName, numberTypeOptions} from "/@/views/template/components/field-setting.ts";
import type {FormItemType} from "/@/api/types.ts";
import {cloneDeep} from "lodash-es";
import {useForm} from "alova/client";
import {templateApi} from "/@/api/modules/setting/template";

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
  // fieldType.value = undefined;
}
const handleClose = () => {
  active.value = false;
  formRef.value?.restoreValidation()
  resetForm();
};
const handleConfirm = () => {
  formRef.value?.validate(errors => {
    if (!errors) {
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
        handleClose();
        emit('success', isEdit.value, res.id);
      })
    }
  })
}
const handleEdit = (item: DefinedFieldItem) => {
  isMultipleSelectMember.value = item.type === 'MULTIPLE_MEMBER';
  if (item.id) {
    // getFieldDetail(item.id);
    window.$message.info('执行编辑操作');
  }
}
onMounted(() => {
  const excludeOptions = ['MULTIPLE_MEMBER', 'DATETIME', 'SYSTEM', 'INT', 'FLOAT'];
  fieldOptions.value = fieldIconAndName.filter((item: any) => excludeOptions.indexOf(item.key) < 0);
});
defineExpose({
  handleEdit,
});
</script>

<template>
  <n-drawer v-model:show="active" :width="800">
    <n-drawer-content>
      <template #header>
        {{ isEdit ? '更新字段' : '新增字段' }}
      </template>
      <n-form
          ref="formRef"
          :model="fieldForm"
          :rules="rules"
          label-placement="top"
          label-width="auto"
          require-mark-placement="right-hanging"
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
                    class="w-[260px]"/>
        </n-form-item>
        <n-form-item v-if="fieldForm.type === 'MEMBER'" label="允许添加多个成员" path="type">
          <n-switch v-model:value="isMultipleSelectMember" size="small" :disabled="isEdit"/>
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
                      @click="handleConfirm">{{ isEdit ? '更新字段' : '新增字段' }}
            </n-button>
          </n-flex>
        </slot>
      </template>
    </n-drawer-content>
  </n-drawer>
</template>

<style scoped>

</style>