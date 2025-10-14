<script setup lang="ts">
import {computed, h, onMounted, ref} from "vue";
import type {IScheduleInfo} from "/@/api/modules/schedule/types.ts";
import {type DataTableColumns, type DataTableRowKey, NButton, NSwitch} from "naive-ui";
import type {ITableQueryParams} from "/@/api/types.ts";
import {scheduleApi} from "/@/api/modules/schedule";
import {usePagination} from "alova/client";
import BasePagination from "/@/components/BasePagination.vue";
import BaseCronSelect from "/@/components/BaseCronSelect.vue";
import ScheduleModal from "/@/views/schedule/components/ScheduleModal.vue";
import {useAppStore} from "/@/store";

const scheduleModalRef = ref<InstanceType<typeof ScheduleModal>>();
const appStore = useAppStore();
const keyword = ref('');
const showScheduleModalVisible = ref(false);
const columns: DataTableColumns<IScheduleInfo> = [
  {
    type: 'selection', fixed: 'left'
  },
  {title: '任务 ID', key: 'num', width: 150, ellipsis: {tooltip: true}},
  {title: '任务名称', key: 'name', width: 160},
  {
    title: '状态', key: 'enable', width: 80,
    render(record) {
      return h(NSwitch, {value: record.enable, size: 'small'}, {})
    }
  },
  {
    title: '运行规则', key: 'value', width: 180,
    render(record) {
      return h(BaseCronSelect, {modelValue: record.value, size: 'small'});
    }
  },
  {title: '操作人', key: 'createUser', width: 100},
  {title: '操作时间', key: 'createTime', width: 150, ellipsis: {tooltip: true}},
  {title: '上次完成时间', key: 'lastTimeAsLocalDateTime', width: 150},
  {title: '下次执行时间', key: 'nextTimeAsLocalDateTime', width: 150},
  {
    title: '操作', key: 'actions', fixed: 'right', width: 200,
    render: (record) => {
      const actions = [
        h(NButton, {size: 'small', text: true, type: 'primary', class: '!mr-[12px]'}, {default: () => '详情'}),
        h(NButton, {size: 'small', text: true, type: 'primary', class: '!mr-[12px]'}, {default: () => '修改配置'}),
        h(NButton, {size: 'small', text: true, type: 'error'}, {default: () => '删除'})
      ]
      return actions;
    }
  },
]
const checkedRowKeys = ref<DataTableRowKey[]>([])
const handleCheck = (rowKeys: DataTableRowKey[]) => {
  checkedRowKeys.value = rowKeys
}
const currentProjectId = computed(() => {
  return appStore.currentProjectId
})
const {page, pageSize, total, data, send: fetchData} = usePagination((page, pageSize) => {
  const param: ITableQueryParams = {page, pageSize, keyword: keyword.value, projectId: currentProjectId.value}
  return scheduleApi.getScheduleList(param);
}, {
  initialData: {total: 0, data: []},
  immediate: false,
  data: resp => resp.records,
  total: resp => resp.totalRow,
  watchingStates: [keyword, currentProjectId]
})
const handleAdd = () => {
  showScheduleModalVisible.value = true;
}
const handleClose = (search: boolean) => {
  showScheduleModalVisible.value = false;
  if (search) {
    fetchData();
  }
}
onMounted(() => {
  fetchData();
});
</script>

<template>
  <n-card>
    <template #header>
      <n-button type="primary" text @click="handleAdd">
        <n-icon :size="24">
          <div class="i-ant-design:plus-circle-filled"/>
        </n-icon>
      </n-button>
    </template>
    <template #header-extra>
      <n-input v-model:value="keyword" clearable placeholder="通过 ID/名称搜索"/>
    </template>
    <n-data-table :columns="columns"
                  :data="data"
                  :row-key="(row: IScheduleInfo) => row.id"
                  @update:checked-row-keys="handleCheck"/>
    <base-pagination v-model:page="page" v-model:page-size="pageSize" :count="total||0"/>
  </n-card>
  <schedule-modal ref="scheduleModalRef" v-model:show-modal="showScheduleModalVisible"
                  @close="handleClose"/>
</template>

<style scoped>

</style>