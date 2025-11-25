<script setup lang="ts">

import {computed, h, onMounted, ref} from "vue";
import type {SystemRole, UserForm, UserListItem} from "/@/api/modules/user/types.ts";
import AddUserModal from "/@/views/setting/user/components/AddUserModal.vue";
import {
  type DataTableColumns,
  type DataTableRowKey,
  NButton,
  NFlex,
  NSelect,
  NSwitch,
  type SelectOption
} from "naive-ui";
import {usePagination, useRequest} from "alova/client";
import type {BatchActionQueryParams, ITableQueryParams} from "/@/api/types.ts";
import {userApi} from "/@/api/modules/user";
import BasePagination from "/@/components/BasePagination.vue";
import TagGroup from '/@/components/tag-group/index.vue'
import TableMoreAction from '/@/components/table-more-action/index.vue'
import {hasAnyPermission} from "/@/utils/permission.ts";

export type UserModalMode = 'create' | 'edit';


const keyword = ref('');
const userFormMode = ref<UserModalMode>('create');
const visible = ref(false)
const defaultUserForm = {
  list: [
    {
      name: '',
      email: '',
      phone: '',
    },
  ],
  userGroup: [],
};
const userForm = ref<UserForm>({...defaultUserForm});
const userGroupOptions = ref<SystemRole[]>([]);
const checkedRowKeys = ref<DataTableRowKey[]>([])
const handleCheck = (rowKeys: DataTableRowKey[]) => {
  checkedRowKeys.value = rowKeys
}
const {page, pageSize, total, data, send: fetchData} = usePagination((page, pageSize) => {
  const param: ITableQueryParams = {
    page, pageSize,
    keyword: keyword.value,
  }
  return userApi.getUserPage(param);
}, {
  initialData: {total: 0, data: []},
  immediate: false,
  data: resp => resp.records,
  total: resp => resp.totalRow,
  watchingStates: [keyword]
})
const tableActions = [
  {
    label: '重置密码',
    key: 'resetPassword'
  },
  {
    type: 'divider',
    key: 'd1'
  },
  {
    label: '删除',
    key: 'delete'
  },
]
const hasOperationSysUserPermission = computed(() =>
    hasAnyPermission(['SYSTEM_USER:READ+UPDATE', 'SYSTEM_USER:READ+DELETE'])
);
const columns: DataTableColumns<UserListItem> = [
  {
    type: 'selection', fixed: 'left', options: ['all', 'none']
  },
  {title: '用户名', key: 'name', ellipsis: {tooltip: true}},
  {title: '邮箱', key: 'email', ellipsis: {tooltip: true}},
  {title: '手机', key: 'phone', ellipsis: {tooltip: true}, width: 160},

  {
    title: '用户组', key: 'userRoleList', width: 300,
    render(record) {
      if (!record.selectUserGroupVisible) {
        return h(TagGroup, {tagList: record.userRoleList, onClick: () => handleTagClick(record)}, {});
      }
      return h(NSelect, {
        value: record.userRoleList.map(e => e.code), multiple: true,
        options: userGroupOptions.value, labelField: 'name', valueField: 'id',
        clearable: true, onUpdateValue: (v, o) => handleUserGroupChange(v, o, record)
      })
    }
  },
  {
    title: '状态', key: 'enable', width: 300,
    render(record) {
      return h(NSwitch, {
        value: record.enable, size: 'small',
        disabled: !hasAnyPermission(['SYSTEM_USER:READ+UPDATE']),
        onUpdateValue: (v) => handleStatusChange(v, record)
      }, {})
    }
  },
  {
    title: hasOperationSysUserPermission.value ? '操作' : '',
    key: 'actions',
    fixed: 'right',
    width: hasOperationSysUserPermission.value ? 150 : 50,
    render(record) {
      if (record.enable) {
        return h(NFlex, {}, {
          default: () => {
            const result = [];
            if (hasAnyPermission(['SYSTEM_USER:READ+UPDATE'])) {
              result.push(h(NButton, {
                text: true,
                onClick: () => showUserModal('edit', record)
              }, {default: () => '编辑'}),);
            }
            if (hasAnyPermission(['SYSTEM_USER:READ+UPDATE', 'SYSTEM_USER:READ+DELETE'])) {
              result.push(h(TableMoreAction, {options: tableActions, onSelect: (key) => handleSelect(key, record)}, {}))
            }
            return result;
          }
        })
      }
      if (hasAnyPermission(['SYSTEM_USER:READ+DELETE'])) {
        return h(NButton, {text: true}, {default: () => '删除'});
      }
    }
  }
];
const handleSelect = (key: string, record: UserListItem) => {
  switch (key) {
    case 'resetPassword':
      resetPassword(record);
      break;
    case 'delete':
      deleteUser(record)
      break
    default:
      break
  }
}
const handleUserGroupChange = (value: string, option: SelectOption, record: UserListItem) => {
  console.log(value)
  console.log(option)
  console.log(record)
}
const handleTagClick = (record: UserListItem) => {
  console.log('handleTagClick', record)
}
const handleStatusChange = (v: boolean, record: UserListItem) => v ? enableUser(record) : disabledUser(record);
const {send: toggleUserStatus} = useRequest((p) => userApi.toggleUserStatus(p), {immediate: false})
const {send: deleteUserInfo} = useRequest((p) => userApi.deleteUserInfo(p), {immediate: false})
const deleteUser = (record?: UserListItem, _isBatch?: boolean, _params?: BatchActionQueryParams) => {
  let title = `确认删除 ${record?.name} 这个用户吗？`;
  window.$dialog.error({
    title,
    content: '仅删除用户信息，不处理该用户的系统数据',
    positiveText: '确认删除',
    negativeText: '取消',
    maskClosable: false,
    onPositiveClick: () => {
      deleteUserInfo(record?.id).then(() => {
        window.$message.success('删除成功');
        fetchData();
      })
    },
  })
}
const {send: resetUserPassword} = useRequest((p) => userApi.resetUserPassword(p), {immediate: false})
const resetPassword = (record?: UserListItem, _isBatch?: boolean, params?: BatchActionQueryParams) => {
  let title = `是否将 ${record?.name} 的密码重置为初始密码？`;
  let selectIds = [record?.id || ''];
  window.$dialog.warning({
    title,
    content: '初始的密码为用户邮箱，下次登录时生效',
    positiveText: '确认重置',
    negativeText: '取消',
    maskClosable: false,
    onPositiveClick: () => {
      resetUserPassword({
        selectIds,
        selectAll: !!params?.selectAll,
        excludeIds: params?.excludeIds || [],
        condition: {keyword: keyword.value},
      }).then(() => {
        window.$message.success('重置成功');
        fetchData();
      })
    },
  })
}
const enableUser = (record?: UserListItem, _isBatch?: boolean, params?: BatchActionQueryParams) => {
  let selectIds = [record?.id || ''];
  let title = `确认启用 ${record?.name} 这个用户吗？`;
  // if (isBatch) {
  //   title = `确认启用已选中的 ${checkedRowKeys.value.length} 个用户吗？`;
  //   selectIds = checkedRowKeys.value;
  // }
  window.$dialog.info({
    title,
    content: '启用后用户可以登录系统',
    positiveText: '确认启用',
    negativeText: '取消',
    maskClosable: false,
    onPositiveClick: () => {
      toggleUserStatus({
        selectIds,
        enable: true,
        selectAll: !!params?.selectAll,
        excludeIds: params?.excludeIds || [],
      }).then(() => {
        window.$message.success('启用成功');
        fetchData();
      })
    },
  })
}
const disabledUser = (record?: UserListItem, _isBatch?: boolean, params?: BatchActionQueryParams) => {
  let selectIds = [record?.id || ''];
  let title = `确认禁用 ${record?.name} 这个用户吗？`;
  // if (isBatch) {
  //   title = `确认禁用已选中的 ${checkedRowKeys.value.length} 个用户吗？`;
  //   selectIds = checkedRowKeys.value.map(e => e.id);
  // }
  window.$dialog.warning({
    title,
    content: '禁用的用户无法登录系统',
    positiveText: '确认禁用',
    negativeText: '取消',
    maskClosable: false,
    onPositiveClick: () => {
      toggleUserStatus({
        selectIds,
        enable: false,
        selectAll: !!params?.selectAll,
        excludeIds: params?.excludeIds || [],
      }).then(() => {
        window.$message.success('禁用成功');
        fetchData();
      })
    },
  })
}
const showUserModal = (mode: UserModalMode, record?: UserListItem) => {
  visible.value = true;
  userFormMode.value = mode;
  if (mode === 'edit' && record) {
    userForm.value.list = [
      {
        id: record.id,
        name: record.name,
        email: record.email,
        phone: record.phone ? record.phone.replace(/\s/g, '') : record.phone,
      },
    ];
    userForm.value.userGroup = record.userRoleList.map(e => e.code);
  }
}
const handleCloseAddModal = (shouldFetchData: boolean) => {
  visible.value = false;
  if (shouldFetchData) {
    fetchData();
  }
}
const initUserGroupOption = async () => {
  userGroupOptions.value = await userApi.getSystemRoles();
}
onMounted(() => {
  fetchData();
  initUserGroupOption()
})
</script>

<template>
  <n-card>
    <template #header>
      <n-button v-permission.all="['SYSTEM_USER:READ+ADD']" type="primary" text @click="showUserModal('create')">
        <n-icon :size="24">
          <div class="i-ant-design:plus-circle-filled"/>
        </n-icon>
      </n-button>
    </template>
    <template #header-extra>
      <n-space>
        <n-input v-model:value="keyword" clearable placeholder="通过姓名/邮箱/手机搜索"/>
      </n-space>
    </template>
    <n-data-table :columns="columns"
                  :data="data"
                  :row-key="(row: UserListItem) => row.id"
                  @update:checked-row-keys="handleCheck"/>
    <base-pagination v-model:page="page" v-model:page-size="pageSize" :count="total||0"/>
  </n-card>
  <add-user-modal v-model:show-modal="visible" v-model:user-form="userForm"
                  :mode="userFormMode"
                  :user-group-options="userGroupOptions"
                  @close="handleCloseAddModal"/>
</template>

<style scoped>

</style>