<script setup lang="ts">
import {computed, onMounted, ref, watchEffect} from "vue";
import {
  getTemplateName,
  getTotalFieldOptionList
} from "/@/views/setting/organization/template/components/field-setting.ts";
import {useRoute, useRouter} from "vue-router";
import type {FormInst} from "naive-ui";
import {useAppStore} from "/@/store";
import type {ActionTemplateManage, CustomField, DefinedFieldItem} from "/@/api/modules/setting/template/types.ts";
import EditFieldDrawer from "/@/views/setting/organization/template/components/EditFieldDrawer.vue";
import AddFieldToTemplateDrawer from "/@/views/setting/organization/template/components/AddFieldToTemplateDrawer.vue";
import {useRequest} from "alova/client";
import {templateApi} from "/@/api/modules/setting/template";
import {VueDraggable} from 'vue-draggable-plus';
import {ProjectManagementRouteEnum, SettingRouteEnum} from "/@/enums/common-enum.ts";

const route = useRoute();
const router = useRouter();

const title = ref('');
const props = defineProps<{
  mode: 'organization' | 'project';
}>();
const isEdit = computed(() => !!route.query.id);
const formRef = ref<FormInst | null>(null)
const appStore = useAppStore();
const currentOrgId = computed(() => appStore.currentOrgId);
const currentProjectId = computed(() => appStore.currentProjectId);
const initTemplateForm: ActionTemplateManage = {
  id: '',
  name: '',
  remark: '',
  scopeId: props.mode === 'organization' ? currentOrgId.value : currentProjectId.value,
  enableThirdPart: false,
  platForm: '',
  uploadImgFileIds: [],
};
const templateForm = ref<ActionTemplateManage>({...initTemplateForm});
const selectData = ref<DefinedFieldItem[]>([]);
const totalTemplateField = ref<DefinedFieldItem[]>([]);
const activeIndex = ref(-1);
const fieldDrawerRef = ref();
const showFieldDrawer = ref<boolean>(false);
const showDrawer = ref<boolean>(false);
const selectFiled = ref<DefinedFieldItem[]>([]);
// 编辑更新已选择字段
const isEditField = ref<boolean>(false);
const createField = () => showFieldDrawer.value = true
const associatedField = () => showDrawer.value = true
const handleEditField = (record: DefinedFieldItem) => {
  showFieldDrawer.value = true;
  fieldDrawerRef.value.handleEdit(record);
}
const {send: getClassifyField} = useRequest(() => {
  const params = {
    scopedId: props.mode === 'organization' ? currentOrgId.value : currentProjectId.value,
    scene: route.query.type as string,
  }
  if (props.mode === 'organization') {
    return templateApi.getOrgFieldList(params);
  }
  return templateApi.getProjectFieldList(params);
}, {immediate: false, force: true})
const handleConfirm = (dataList: DefinedFieldItem[]) => {
  const selectFieldIds = selectData.value.map((e) => e.id);
  const newData = dataList.filter((item) => !selectFieldIds.includes(item.id));
  const newIds = dataList.map((item) => item.id);
  // @desc 原先已经选择过的选项value值不能再次添加的时候置空
  const selectDataValue = selectData.value.filter((item) => newIds.includes(item.id));
  selectData.value = [...selectDataValue, ...newData];
}
const getTemplateParams = () => {
  console.log('getTemplateParams', selectData.value)
  const result = selectData.value.map((item) => {
    if (item.formRules?.length) {
      const {value} = item.formRules[0];
      let setValue;
      if (typeof value === 'number') {
        setValue = value;
      } else {
        setValue = value || '';
      }
      return {
        fieldId: item.id,
        required: item.required,
        apiFieldId: item.apiFieldId || '',
        defaultValue: setValue,
      };
    }
    return [];
  });
  const {name, remark, enableThirdPart, id} = templateForm.value;
  return {
    id,
    name,
    remark,
    enableThirdPart,
    customFields: result as CustomField[],
    scopeId: props.mode === 'organization' ? currentOrgId.value : currentProjectId.value,
    scene: route.query.type,
  };
}
const changeDrag = () => {
  activeIndex.value = -1;
}
const {send: saveTemplate} = useRequest((param) => props.mode === 'organization' ? templateApi.createOrganizeTemplateInfo(param) : templateApi.createProjectTemplateInfo(param),{immediate: false})
const {send: updateTemplate} = useRequest((param) => props.mode === 'organization' ? templateApi.updateOrganizeTemplateInfo(param) : templateApi.updateProjectTemplateInfo(param),{immediate: false})
const handleSave = () => {
  const params = getTemplateParams();
  if (isEdit.value) {
    updateTemplate(params).then(() => {
      window.$message.success('更新成功');
    });
  } else {
    saveTemplate(params).then(() => {
      window.$message.success('保存成功');
    });
  }
  if (props.mode === 'organization') {
    router.push({name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT, query: route.query});
  } else {
    router.push({name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT, query: route.query});
  }
}
const handleCheckedChange = (value: boolean, formItem: DefinedFieldItem) => {
  formItem.required = value
}
const updateFieldHandler = async (editFlag: boolean, fieldId: string) => {
  selectFiled.value = selectData.value;
  isEditField.value = editFlag;
  await getClassifyField().then(res => {
    totalTemplateField.value = res;
  })
  totalTemplateField.value = getTotalFieldOptionList(totalTemplateField.value as DefinedFieldItem[]);
  // 编辑字段
  if (isEditField.value) {
    const index = selectData.value.findIndex((e: any) => e.id === fieldId);
    const newUpdateData = totalTemplateField.value.find((item: any) => item.id === fieldId);
    if (index > -1 && newUpdateData) {
      selectData.value.splice(index, 1);
      selectData.value.splice(index, 0, newUpdateData);
    }
  }
  // 新增字段
  if (!isEditField.value && fieldId) {
    const newUpdateData = totalTemplateField.value.find((item: any) => item.id === fieldId);
    if (newUpdateData) {
      selectData.value.push(newUpdateData);
    }
  }
}
watchEffect(async () => {
  if (isEdit.value && route.params.mode === 'copy') {
    title.value = `复制${getTemplateName('organization', route.query.type as string)}模板`;
  } else if (isEdit.value) {
    title.value = `更新${getTemplateName('organization', route.query.type as string)}`;
  } else {
    title.value = `创建${getTemplateName('organization', route.query.type as string)}`;
  }
});
onMounted(() => {
  selectData.value = []
  totalTemplateField.value = [];
  getClassifyField().then(res => {
    totalTemplateField.value = res;
  })
})
</script>

<template>
  <n-card
      :title="title"
      :segmented="{
      content: true,
      footer: 'soft',
    }"
  >
    <template #header>

    </template>
    <template #header-extra>
      <!--      #header-extra-->
    </template>
    <div class="wrapper-preview">
      <div class="preview-left pr-4">
        <n-form
            ref="formRef"
            :model="templateForm"
            label-placement="left"
            label-width="auto"
            require-mark-placement="right-hanging"
            size="small"
            class="mt-1 max-w-[710px]"
        >
          <n-form-item v-if="!templateForm?.internal" path="name" class="mb-0 max-w-[710px]">
            <n-input v-model:value="templateForm.name" placeholder="请输入模板名称" :maxlength="255"
                     :disabled="templateForm?.internal" class="max-w-[732px]"/>
          </n-form-item>
          <span v-else class="font-medium text-[var(--color-text-1)] underline">{{ templateForm.name }}</span>
          <n-form-item path="remark" class="mb-0 max-w-[710px]">
            <n-input v-model:value="templateForm.remark" type="textarea" placeholder="请输入模板描述" :maxlength="255"/>
          </n-form-item>
        </n-form>
      </div>
      <div class="preview-right pr-4">
        <div>
          <VueDraggable v-model="selectData" handle=".form" ghost-class="ghost" @change="changeDrag">
            <div v-for="(formItem) of selectData" :key="formItem.id" class="customWrapper">
              <n-flex justify="space-between">
                <div class="ml-4">{{ formItem.name }}</div>
                <div class="action">
                  <span class="required">
                    <n-checkbox v-model:checked="formItem.required" :disabled="formItem.internal" class="mr-1"
                                @update:checked="(v)=>handleCheckedChange(v,formItem)">
                      必填
                    </n-checkbox>
                  </span>
                  <div class="actionList">
                    <n-tooltip trigger="hover">
                      <template #trigger>
                        <div class="i-mdi:chevron-up text-[16px]"/>
                      </template>
                      上移
                    </n-tooltip>
                    <n-divider vertical class="!m-0 !mx-2"/>
                    <n-tooltip trigger="hover">
                      <template #trigger>
                        <div class="i-mdi:chevron-down text-[16px]"/>
                      </template>
                      下移
                    </n-tooltip>
                    <n-divider v-if="!formItem.internal" vertical class="!m-0 !mx-2"/>
                    <n-tooltip trigger="hover">
                      <template #trigger>
                        <div class="i-mdi:mdi:invoice-text-edit-outline text-[16px]"
                             @click="handleEditField(formItem)"/>
                      </template>
                      编辑
                    </n-tooltip>
                    <n-divider v-if="!formItem.internal" vertical class="!m-0 !mx-2"/>
                    <n-tooltip trigger="hover">
                      <template #trigger>
                        <div class="i-mdi:selection-ellipse-remove text-[16px]"/>
                      </template>
                      删除
                    </n-tooltip>
                  </div>
                </div>
              </n-flex>
              <div class="form">
                <div class="mb-[8px] flex w-full items-center">
                  <n-tooltip trigger="hover">
                    <template #trigger>
                      <div class="one-line-text max-w-[calc(100%-200px)]">
                        {{ formItem.formRules && formItem.formRules[0] ? formItem?.formRules[0]?.title : '' }}
                      </div>
                    </template>
                    {{ formItem.formRules && formItem.formRules[0] ? formItem?.formRules[0]?.title : '' }}
                  </n-tooltip>
                </div>
              </div>
            </div>
          </VueDraggable>
          <div class="tagWrapper">

          </div>
          <div class="flex items-center">
            <n-button text class="mr-1 mt-1 px-0" @click="associatedField">
              <template #icon>
                <div class="i-mdi:plus-circle text-[14px]"/>
              </template>
              关联字段
            </n-button>
            <n-tooltip trigger="hover">
              <template #trigger>
                <div class="i-mdi:help-circle mr-8 mt-1 h-[16px] w-[16px]"/>
              </template>
              关联已添加的字段
            </n-tooltip>
            <n-button text class="mr-1 mt-1 px-0" @click="createField">
              <template #icon>
                <div class="i-mdi:plus-circle text-[14px]"/>
              </template>
              新增字段
            </n-button>
            <n-tooltip trigger="hover">
              <template #trigger>
                <div class="i-mdi:help-circle mr-8 mt-1 h-[16px] w-[16px]"/>
              </template>
              新增一个新的字段
            </n-tooltip>
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <n-flex>
        <n-button secondary>取消</n-button>
        <n-button type="primary" @click="handleSave">创建</n-button>
      </n-flex>
    </template>
    <!--    <template #action>-->
    <!--      #action-->
    <!--    </template>-->
  </n-card>
  <edit-field-drawer ref="fieldDrawerRef" v-model:active="showFieldDrawer" :mode="props.mode"
                     :data="(totalTemplateField as DefinedFieldItem[])"
                     @success="updateFieldHandler"/>
  <add-field-to-template-drawer v-model:active="showDrawer" :mode="props.mode"
                                :total-data="(totalTemplateField as DefinedFieldItem[])"
                                :table-select-data="(selectData as DefinedFieldItem[])"
                                @confirm="handleConfirm"/>
</template>

<style scoped>
.wrapper-preview {
  display: flex;
  height: 100%;

  .preview-left {
    width: 100%;
    border-right: 1px solid;
  }

  .preview-right {
    padding-top: 8px;
    width: 928px;
    min-width: 928px;

    .customWrapper {
      position: relative;
      margin-bottom: 4px;
      border: 1px solid transparent;
      border-radius: 6px;
      @apply flex flex-col justify-between;

      .form {
        padding: 8px;
        border: 1px solid transparent;
        border-radius: 6px;
      }

      .action {

        @apply flex items-center justify-end;

        .actionList {
          padding: 4px;
          border-radius: 4px;
          @apply flex items-center justify-center;
        }

        .required > .arco-checkbox {
          padding: 2px 4px;
          border-radius: 4px;
          box-shadow: 0 4px 10px -1px rgba(100 100 102/ 15%);
        }
      }

      &:hover > .action {
        opacity: 1;
      }

      &:hover > .action > .actionList {
        box-shadow: 0 4px 10px -1px rgba(100 100 102/ 15%);
      }
    }
  }
}
</style>