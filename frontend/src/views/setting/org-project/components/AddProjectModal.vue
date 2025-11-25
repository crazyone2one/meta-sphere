<script setup lang="ts">
import type {CreateOrUpdateSystemProjectParams, SystemOrgOption} from "/@/api/modules/setting/org-project/types.ts";
import {computed, ref, watchEffect} from "vue";
import type {FormInst, FormRules} from "naive-ui";
import {useForm, useRequest} from "alova/client";
import {orgAndProjectApi} from "/@/api/modules/setting/org-project";
import UserSelector from "/@/components/user-selector/index.vue";
import {UserRequestTypeEnum} from "/@/components/user-selector/utils.ts";
import {useAppStore} from "/@/store";
import BaseModal from '/@/components/base-modal/index.vue'

const showModal = defineModel<boolean>('showModal', {type: Boolean, default: false});
const appStore = useAppStore()
const {
  currentProject = {
    allResourcePool: false, description: "", enable: false, name: "", resourcePoolIds: [], userIds: [],
    id: '', num: ''
  }
} = defineProps<{ currentProject?: CreateOrUpdateSystemProjectParams; }>();
const isEdit = computed(() => !!currentProject?.id);
const formRef = ref<FormInst | null>(null)
const emit = defineEmits<{
  (e: 'cancel', shouldSearch: boolean): void;
}>();
// const allModuleIds = ['bugManagement', 'caseManagement', 'apiTest', 'testPlan', 'scheduleManagement'];
const allModuleIds = ['apiTest', 'testPlan', 'scheduleManagement'];
const showPoolModuleIds = ['apiTest', 'testPlan', 'scheduleManagement'];

const moduleOption = [
  // { label: 'menu.workbench', value: 'workstation' },
  {label: '任务管理', value: 'scheduleManagement'},
  {label: '测试计划', value: 'testPlan'},
  // { label: 'menu.bugManagement', value: 'bugManagement' },
  // { label: 'menu.caseManagement', value: 'caseManagement' },
  {label: '接口测试', value: 'apiTest'},
  // { label: 'menu.uiTest', value: 'uiTest' },
  // { label: 'menu.performanceTest', value: 'loadTest' },
];
const {
  form,
  reset, send: submitForm, loading
} = useForm(formData => orgAndProjectApi.createOrUpdateProject({id: isEdit.value ? currentProject.id : '', ...formData}), {
  initialForm: {
    name: '',
    num: '',
    userIds: [],
    organizationId: '',
    description: '',
    enable: true,
    moduleIds: allModuleIds,
    resourcePoolIds: [],
    allResourcePool: true,
  },
  resetAfterSubmiting: true
})
const showPool = computed(() => showPoolModuleIds.some((item) => form.value.moduleIds?.includes(item)));
const rules: FormRules = {
  name: [{required: true, message: '项目名称不能为空', trigger: ['blur', 'input']}, {
    max: 255,
    message: '项目名称长度不能超过255个字符'
  }],
  num: [{required: true, message: '项目num不能为空', trigger: ['blur', 'input']}, {
    max: 255,
    message: '项目num长度不能超过255个字符'
  }],
  organizationId: {required: true, message: '所属组织不能为空', trigger: ['blur', 'change']},
  userIds: {type: 'array', required: true, message: '项目管理员不能为空', trigger: ['blur', 'change']},
  allResourcePool: {required: showPool.value, message: '资源池不能为空1', trigger: 'change'},
  resourcePoolIds: {required: showPool.value, message: '资源池不能为空2', trigger: 'change'}
}
const {send: initAffiliatedOrgOption} = useRequest(() => orgAndProjectApi.getSystemOrgOption(), {immediate: false})
const handleCancel = (shouldSearch: boolean) => {
  formRef.value?.restoreValidation();
  showModal.value = false;
  emit('cancel', shouldSearch);
};
const handleSubmit = () => {
  formRef.value?.validate(errors => {
    if (!errors) {
      submitForm().then(() => {
        window.$message.success(isEdit.value ? '更新项目成功' : '创建项目成功');
        handleCancel(true);
        appStore.initProjectList()
      })
    }
  })
};
const affiliatedOrgOption = ref<SystemOrgOption[]>([]);
watchEffect(() => {
  initAffiliatedOrgOption().then(res => affiliatedOrgOption.value = res)
  if (currentProject.id) {
    if (currentProject) {
      form.value = {
        id: currentProject.id,
        name: currentProject.name,
        num: currentProject.num,
        description: currentProject.description,
        enable: currentProject.enable,
        moduleIds: currentProject.moduleIds,
        resourcePoolIds: currentProject.resourcePoolIds,
        allResourcePool: currentProject.allResourcePool,
        userIds: currentProject.userIds,
        organizationId: currentProject.organizationId
      };
    } else {
      reset()
    }
  }
})

</script>

<template>
  <base-modal v-model:show-modal="showModal"
              :ok-text=" isEdit ? '更新' : '创建'"
              :loading="loading"
              @cancel="handleCancel(false)" @update="handleSubmit">
    <template #title>
      <span v-if="isEdit">
        更新项目
        <span class="text-[#9597a4]">({{ currentProject?.name }})</span>
      </span>
      <span v-else>
        创建项目
      </span>
    </template>
    <n-form ref="formRef" :model="form"
            :rules="rules"
            label-placement="left"
            label-width="auto"
            require-mark-placement="right-hanging">
      <n-form-item path="name" label="项目名称">
        <n-input v-model:value="form.name" placeholder="请输入项目名称，不可与其他项目名称重复"/>
      </n-form-item>
      <n-form-item path="num" label="项目num">
        <n-input v-model:value="form.num" placeholder="请输入项目num，不可与其他项目num重复"/>
      </n-form-item>
      <n-form-item path="organizationId" label="所属组织">
        <n-select v-model:value="form.organizationId" :options="affiliatedOrgOption" placeholder="请选择所属组织"
                  label-field="name" value-field="id"/>
      </n-form-item>
      <n-form-item path="userIds" label="项目管理员">
        <user-selector v-model:model-value="form.userIds" :type="UserRequestTypeEnum.SYSTEM_PROJECT_ADMIN"
                       placeholder="请选择项目管理员" :at-least-one="true"/>
      </n-form-item>
      <n-form-item path="module" label="开启模块">
        <n-checkbox-group v-model:value="form.moduleIds">
          <n-space item-style="display: flex;">
            <n-checkbox v-for="item in moduleOption" v-model:checked="item.value"
                        :value="item.value">
              {{ item.label }}
            </n-checkbox>
          </n-space>
        </n-checkbox-group>
      </n-form-item>
      <n-form-item path="description" label="项目描述">
        <n-input v-model:value="form.description" type="textarea" placeholder="请对该项目进行描述"
                 clearable :autosize="{minRows:1}"/>
      </n-form-item>
    </n-form>
    <template #actionLeft>
      <n-switch v-model:value="form.enable"/>
      <span>状态</span>
    </template>
  </base-modal>
</template>

<style scoped>
</style>