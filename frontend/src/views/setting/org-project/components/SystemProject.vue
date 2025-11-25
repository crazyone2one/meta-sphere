<script setup lang="ts">
import {usePagination} from "alova/client";
import {orgAndProjectApi} from "/@/api/modules/setting/org-project";
import type {ITableQueryParams} from "/@/api/types.ts";
import {type DataTableColumns, type DataTableRowKey, NButton, NFlex, NSwitch} from "naive-ui";
import type {
  CreateOrUpdateSystemProjectParams,
  OrgProjectTableItem,
  UserItem
} from "/@/api/modules/setting/org-project/types.ts";
import BasePagination from "/@/components/BasePagination.vue";
import {h, onMounted, reactive, ref, type VNode} from "vue";
import dayjs from "dayjs";

import UserAdminDiv from "/@/components/UserAdminDiv.vue";
import {hasAnyPermission} from "/@/utils/permission.ts";
import ShowOrEdit from "/@/components/ShowOrEdit.vue";
import UserDrawer from "/@/views/setting/org-project/components/UserDrawer.vue";
import AddProjectModal from "/@/views/setting/org-project/components/AddProjectModal.vue";
import AddUserModal from "/@/views/setting/org-project/components/AddUserModal.vue";

const addProjectVisible = ref(false);
const userVisible = ref(false);
const currentProjectId = ref('');
const currentUpdateProject = ref<CreateOrUpdateSystemProjectParams>();
const keyword = defineModel<string>('keyword', {type: String});
const currentUserDrawer = reactive({
  visible: false,
  projectId: '',
  currentName: '',
});
const columns: DataTableColumns<OrgProjectTableItem> = [
  {type: 'selection', fixed: 'left', options: ['all', 'none']},
  {title: 'ID', key: 'num', ellipsis: {tooltip: true}},
  {
    title: '名称', key: 'name', ellipsis: {tooltip: true}, render(record) {
      return h(ShowOrEdit, {
        value: record.name,
        permission: ['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE'],
        onUpdateValue: (v) => handleNameChange(v, record)
      }, {})
    }
  },
  {
    title: '成员', key: 'memberCount', render(record) {
      if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+ADD_MEMBER', 'SYSTEM_ORGANIZATION_PROJECT:READ'])) {
        return h('span', {
          onClick: () => showUserDrawer(record)
        }, {default: () => record.memberCount});
      } else {
        return record.memberCount;
      }
    }
  },
  {
    title: '状态', key: 'enable', ellipsis: {tooltip: true},
    render(record) {
      return h(NSwitch, {
        value: record.enable, size: 'small',
        disabled: !hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE']),
        onUpdateValue: (v) => handleStatusChange(v, record)
      }, {})
    }
  },
  {title: '描述', key: 'description', ellipsis: {tooltip: true}},
  {title: '所属组织', key: 'organizationName', ellipsis: {tooltip: true}},
  {
    title: '创建人', key: 'createUser',
    render(record) {
      return h(UserAdminDiv, {isAdmin: record.projectCreateUserIsAdmin, name: record.createUser})
    }
  },
  {
    title: '创建时间', key: 'createTime', render(record) {
      return h('div', null, {default: () => dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss')})
    }
  },
  {
    title: '操作', key: 'actions', fixed: 'right', width: 180,
    render(record) {
      if (!record.enable) {
        if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+DELETE'])) {
          return h(NButton, {text: true, type: 'error', onClick: () => handleDelete(record)}, {default: () => '删除'});
        }
      } else {
        const tableActions: VNode[] = []
        if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE'])) {
          tableActions.push(h(NButton, {
            text: true,
            type: 'info',
            onClick: () => showAddProjectModal(record)
          }, {default: () => '编辑'}))
        }
        if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+ADD_MEMBER'])) {
          tableActions.push(h(NButton, {
            text: true,
            type: 'info',
            onClick: () => showAddUserModal(record)
          }, {default: () => '添加成员'}))
        }
        if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+DELETE'])) {
          tableActions.push(h(NButton, {text: true, type: 'error'}, {default: () => '删除'}))
        }
        return h(NFlex, {}, {default: () => tableActions})
      }
    }
  }
]
const showAddUserModal = (record: OrgProjectTableItem) => {
  currentProjectId.value = record.id;
  userVisible.value = true;
};
const handleNameChange = (v: string, record: OrgProjectTableItem) => {
  // todo rename project name
  console.log(record)
  console.log(v)
}
const showUserDrawer = (record: OrgProjectTableItem) => {
  currentUserDrawer.visible = true;
  currentUserDrawer.projectId = record.id;
  currentUserDrawer.currentName = record.name;
}
const handleUserDrawerCancel = () => {
  currentUserDrawer.visible = false;
  currentUserDrawer.projectId = '';
  currentUserDrawer.currentName = '';
};
const handleStatusChange = (v: boolean, record: OrgProjectTableItem) => {
  const title = v ? '启用项目' : '关闭项目';
  const content = v ? '开启后的项目展示在项目切换列表' : '结束后的项目不展示在项目切换列表';
  const okText = v ? '确认开启' : '确认关闭';
  window.$dialog.info({
    title, content,
    positiveText: okText,
    negativeText: '取消',
    maskClosable: false,
    onPositiveClick: async () => {
      await orgAndProjectApi.enableOrDisableProject(record.id, v);
      window.$message.success(v ? '开启成功' : '关闭成功');
      await fetchData();
    }
  })
}
const {page, pageSize, total, data, send: fetchData} = usePagination((page, pageSize) => {
  const param: ITableQueryParams = {
    page, pageSize,
    keyword: keyword.value,
  }
  return orgAndProjectApi.postProjectTable(param)
}, {
  initialData: {total: 0, data: []},
  immediate: false,
  data: resp => resp.records,
  total: resp => resp.totalRow,
  watchingStates: [keyword],
  async middleware(_, next) {
    await next({
      // 本次是否强制请求
      force: true
    });
  }
})
const checkedRowKeys = ref<DataTableRowKey[]>([])
const handleCheck = (rowKeys: DataTableRowKey[]) => {
  checkedRowKeys.value = rowKeys
}
const showAddProjectModal = (record: OrgProjectTableItem) => {
  const {id, name, num, description, enable, adminList, organizationId, moduleIds, resourcePoolList, allResourcePool} =
      record;
  addProjectVisible.value = true;
  currentUpdateProject.value = {
    id,
    name, num,
    description,
    enable,
    userIds: adminList.map((item: UserItem) => item.id),
    organizationId,
    moduleIds,
    resourcePoolIds: resourcePoolList,
    allResourcePool,
  };
}
const handleRevokeDelete = (record: OrgProjectTableItem) => {
  window.$dialog.error({
    title: `确认撤销删除 ${record.name} 这个项目吗？`,
    negativeText: '取消',
    maskClosable: false,
    positiveText: '撤销删除',
    onPositiveClick: async () => {
      await orgAndProjectApi.revokeDeleteProject(record.id)
      window.$message.success('已撤销删除');
      await fetchData();
    }
  })
}
const handleDelete = (record: OrgProjectTableItem) => {
  window.$dialog.error({
    title: `确认删除 ${record.name} 这个项目吗？`,
    content: '删除后，系统会在 30天 后执行删除项目 (含项目下所有业务数据)，请谨慎操作！',
    negativeText: '取消',
    maskClosable: false,
    positiveText: '确认删除',
    onPositiveClick: async () => {
      await orgAndProjectApi.deleteProject(record.id)
      window.$message.success(() => {
        return h('span', {}, {
          default: () => {
            return [
              h('span', {}, {default: () => '删除成功'}),
              h('span', {
                    class: 'ml-[8px] cursor-pointer text-[#cd3bff]',
                    directives: [{
                      name: 'permission',
                      value: ['SYSTEM_ORGANIZATION_PROJECT:READ+RECOVER'],
                    }],
                    onClick: () => handleRevokeDelete(record)
                  },
                  {default: () => '撤销'})
            ]
          }
        })
      })
      await fetchData()
    }
  })
}
const handleAddProjectModalCancel = (shouldSearch: boolean) => {
  if (shouldSearch) {
    fetchData();
  }
};
onMounted(() => {
  fetchData();
});

defineExpose({
  fetchData,
});
</script>

<template>
  <n-data-table :columns="columns"
                :data="data"
                :row-key="(row: OrgProjectTableItem) => row.id"
                @update:checked-row-keys="handleCheck"/>
  <base-pagination v-model:page="page" v-model:page-size="pageSize" :count="total||0"/>
  <user-drawer v-model:active="currentUserDrawer.visible"
               :project-id="currentUserDrawer.projectId"
               :current-name="currentUserDrawer.currentName"
               @cancel="handleUserDrawerCancel"
               @request-fetch-data="fetchData"/>
  <add-project-modal v-model:show-modal="addProjectVisible" :current-project="currentUpdateProject"
                     @cancel="handleAddProjectModalCancel"/>
  <add-user-modal v-model:show-modal="userVisible" :project-id="currentProjectId" @submit="fetchData"/>
</template>

<style scoped>

</style>