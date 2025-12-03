<script setup lang="ts">
import {computed, h, onMounted, ref} from "vue";
import {useAppStore} from "/@/store";
import useTemplateStore from "/@/store/modules/setting/template.ts";
import {useRoute} from "vue-router";
import type {DefinedFieldItem, SceneType} from "/@/api/modules/setting/template/types.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";
import EditFieldDrawer from "/@/views/setting/organization/template/components/EditFieldDrawer.vue";
import {usePagination} from "alova/client";
import {templateApi} from "/@/api/modules/setting/template";
import type {ITableQueryParams} from "/@/api/types.ts";
import {type DataTableColumns, NFlex, NButton, NDivider} from "naive-ui";
import BasePagination from "/@/components/BasePagination.vue";
import BasePopover from "/@/components/BasePopover.vue";
import {getIconType} from "/@/views/setting/organization/template/components/field-setting.ts";

const props = defineProps<{
  mode: 'organization' | 'project';
  deletePermission: string[];
  createPermission: string[];
  updatePermission: string[];
}>();
const route = useRoute();
const appStore = useAppStore();
const templateStore = useTemplateStore()
const keyword = ref('');
const scene = ref<SceneType>(route.query.type as string);
const currentOrd = computed(() => appStore.currentOrgId);
const currentProjectId = computed(() => appStore.currentProjectId);
const showDrawer = ref<boolean>(false);
const editFieldDrawerRef = ref<InstanceType<typeof EditFieldDrawer>>()
// 是否开启模板(项目/组织)
// const isEnableTemplate = computed(() => {
//   if (props.mode === 'organization') {
//     return templateStore.orgStatus[route.query.type as string];
//   }
//   return templateStore.projectStatus[route.query.type as string];
// });
const isEnabledTemplate = computed(() => {
  return props.mode === 'organization'
      ? templateStore.projectStatus[scene.value as string]
      : !templateStore.projectStatus[scene.value as string];
});
console.log('isEnabledTemplate',isEnabledTemplate.value)
const fieldHandler = () => {
  showDrawer.value = true;
}
const handleSuccess = () => {
  fetchData();
}
const hasOperationPermission = computed(() =>
    hasAnyPermission([...props.updatePermission, ...props.deletePermission])
);
const templateType = computed(() => {
  switch (route.query.type) {
    case 'API':
      return '接口';
    case 'BUG':
      return '缺陷';
    default:
      return '定时任务';
  }
});
const handleEdit = (record: DefinedFieldItem) => {
  showDrawer.value = true;
  editFieldDrawerRef.value?.handleEdit(record)
}
const columns: DataTableColumns<DefinedFieldItem> = [
  {title: '字段名称', key: 'name', width: 300},
  {
    title: '字段类型', key: 'type',
    render(record) {
      return h('span', {}, {default: () => getIconType(record.type)?.label})
    }
  },
  {title: '字段描述', key: 'remark', width: 300},
  {
    title: hasOperationPermission.value ? '操作' : '',
    key: 'actions',
    fixed: 'right',
    width: hasOperationPermission.value ? 200 : 50,
    render(record) {
      if (isEnabledTemplate.value) {
        return h(NFlex, {}, {
          default: () => {
            const res = []
            if (!record.internal && hasAnyPermission(props.updatePermission)) {
              res.push(h(BasePopover, {
                type: 'error', title: `确认更新 ${record.name} 吗？`,
                subTitleTip: `更新后，使用该字段的${templateType.value}将同步更新`,
                okText: '确定', onConfirm: () => handleEdit(record)
              }, {
                trigger: () => h(NButton, {disabled: record.internal, text: true}, {default: () => '编辑'})
              }),)
            }
            if (!record.internal && hasAnyPermission(props.updatePermission) && hasAnyPermission(props.deletePermission)) {
              res.push(h(NDivider, {vertical: true}, {}))
            }
            if (!record.internal) {
              res.push(h(NButton, {disabled: record.internal, text: true}, {default: () => '删除'}))
            }
            return res
          }
        })
      }
    }
  }
]
const {send: fetchData, data, page, pageSize, total} = usePagination((page, pageSize) => {
  const param: ITableQueryParams = {
    page, pageSize,
    keyword: keyword.value,
    scene: scene.value as string,
    scopedId: props.mode === 'organization' ? currentOrd.value : currentProjectId.value,
  }
  return templateApi.getProjectFieldPage(param)
}, {
  initialData: {total: 0, data: []},
  immediate: false,
  data: resp => resp.records,
  total: resp => resp.totalRow,
  watchingStates: [keyword]
})
onMounted(() => {
  fetchData()
})
</script>

<template>
  <div>
    <div class="mb-4 flex items-center justify-between">
      <span v-if="isEnabledTemplate" class="font-medium">字段列表</span>
      <n-button v-if="!isEnabledTemplate && hasAnyPermission(props.createPermission)"
                type="primary" @click="fieldHandler">新增字段
      </n-button>
      <div>
        <n-input placeholder="通过名称搜索" class="w-[230px]" clearable/>
      </div>
    </div>
    <n-data-table :columns="columns"
                  :data="data"
                  :row-key="(row: DefinedFieldItem) => row.id"/>
    <base-pagination v-model:page="page" v-model:page-size="pageSize" :count="total||0"/>
  </div>
  <edit-field-drawer ref="editFieldDrawerRef" v-model:active="showDrawer" :mode="props.mode" :data="[]"
                     @success="handleSuccess"/>
</template>

<style scoped>

</style>