<script setup lang="ts">
import useTemplateStore from "/@/store/modules/setting/template.ts";
import {computed, ref} from "vue";
import {hasAnyPermission} from "/@/utils/permission.ts";
import {useAppStore} from "/@/store";
import {useRequest} from "alova/client";
import {templateApi} from "/@/api/modules/setting/template";

const {cardItem = {key: '', enable: false, name: '', id: '', value: ''}} = defineProps<{
  cardItem: Record<string, any>;
  mode?: 'organization' | 'project';
}>()
const emit = defineEmits<{
  (e: 'fieldSetting', key: string): void;
  (e: 'templateManagement', key: string): void;
  (e: 'updateState'): void;
}>();
const appStore = useAppStore()
const templateStore = useTemplateStore()
const isEnableProject = computed(() => {
  return templateStore.projectStatus[cardItem.key];
});
const hasEnablePermission = computed(() => hasAnyPermission(['ORGANIZATION_TEMPLATE:READ+ENABLE']));
const isShow = computed(() => {
  if (cardItem.key === 'BUG') {
    return true;
  }
  return !hasEnablePermission.value ? false : !isEnableProject.value;
});
const orgName = computed(() => {
  return appStore.orgList.find((item: any) => item.id === appStore.currentOrgId)?.name;
});
const currentOrgId = computed(() => appStore.currentOrgId);
const fieldSetting = () => {
  emit('fieldSetting', cardItem.key);
};
const templateManagement = () => {
  emit('templateManagement', cardItem.key);
};
const showEnableVisible = ref<boolean>(false);
const validateKeyWord = ref<string>('');
const handleEnableTemplate = () => {
  showEnableVisible.value = true;
};
const handleCancel = () => {
  showEnableVisible.value = false;
  validateKeyWord.value = '';
}
const {loading, send} = useRequest((orgId, scene) => templateApi.enableOrOffTemplate(orgId, scene), {immediate: false})
const handleSubmit = () => {
  if (validateKeyWord.value.trim() !== '' && validateKeyWord.value !== orgName.value) {
    window.$message.success('组织名称不正确');
    return false;
  }
  send(currentOrgId.value, cardItem.key).then(async () => {
    window.$message.success('启用项目模板成功');
    await templateStore.getStatus()
    emit('updateState');
    showEnableVisible.value = false;
  })
}
</script>

<template>
  <div class="outerWrapper p-[3px]">
    <div class="innerWrapper">
      <div class="content">
        <div class="logo-img p-[4px]">
          <div class="i-ant-design:schedule-outlined text-[36px]"/>
        </div>
        <div class="template-operation">
          <div class="flex items-center">
            <span class="font-medium">{{ cardItem.name }}</span>
            <span v-if="isEnableProject" class="enable">已启用项目模板</span>
          </div>
          <div class="flex min-w-[300px] flex-nowrap items-center">
            <!-- 字段设置 -->
            <span class="operation hover:text-[rgb(var(--primary-5))]">
              <span @click="fieldSetting">字段设置</span>
              <n-divider vertical/>
            </span>
            <!-- 模板列表 -->
            <span class="operation hover:text-[rgb(var(--primary-5))]">
              <span @click="templateManagement">模板列表</span>
              <n-divider v-if="isShow" vertical/>
            </span>
            <span v-if="hasEnablePermission && mode === 'organization' && !isEnableProject" class="rounded p-[2px]">
              <n-button type="error" text @click="handleEnableTemplate">启用项目模板</n-button>
            </span>
          </div>
        </div>
      </div>
    </div>
    <n-modal v-model:show="showEnableVisible" preset="dialog" :mask-closable="false" @close="handleCancel">
      <template #header>
        <div>确认启用项目模板吗</div>
      </template>
      <div>启用后，不可恢复为组织模版，请谨慎操作！</div>
      <n-input v-model:value="validateKeyWord" placeholder="请输入组织名称" class="mb-4 mt-[8px]"/>
      <template #action>
        <n-flex>
          <n-button secondary @click="handleCancel">取消</n-button>
          <n-button type="error" :disabled="!validateKeyWord.trim().length"
                    :loading="loading"
                    @click="handleSubmit">确认启用
          </n-button>
        </n-flex>
      </template>
    </n-modal>
  </div>

</template>

<style scoped>
.outerWrapper {
  box-shadow: 0 6px 15px rgba(120 56 135/ 5%);
  @apply rounded;

  .innerWrapper {
    @apply rounded p-6;

    .content {
      @apply flex;
      .logo-img {
        @apply mr-3 flex items-center justify-center;
      }
      .template-operation {
        .operation {
          cursor: pointer;
        }

        .enable {
          height: 20px;
          font-size: 12px;
          color: #9597a4;
          line-height: 14px;
          @apply ml-4 rounded p-1;
        }

        @apply flex flex-col justify-between;
      }
    }
  }
}
</style>