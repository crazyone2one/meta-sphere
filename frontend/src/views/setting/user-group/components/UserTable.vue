<script setup lang="ts">
import type {CurrentUserGroupItem} from "/@/api/modules/setting/types.ts";
import {usePagination, useRequest} from "alova/client";
import type {ITableQueryParams} from "/@/api/types.ts";
import {userGroupApi} from "/@/api/modules/setting/user-group.ts";
import {h, inject, watchEffect} from "vue";
import {type DataTableColumns} from "naive-ui";
import type {SimpleUserInfo} from "/@/api/modules/user/types.ts";
import BasePagination from "/@/components/BasePagination.vue";
import RemoveButton from "/@/components/RemoveButton.vue";
import {AuthScopeEnum, type AuthScopeEnumType} from "/@/enums/common-enum.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";

const {current = {code: '', id: ''}, readPermission = [], updatePermission = []} = defineProps<{
  current: CurrentUserGroupItem;
  deletePermission?: string[];
  readPermission?: string[];
  updatePermission?: string[];
}>()
const systemType = inject<AuthScopeEnumType>('systemType');
const currentKeyword = defineModel<string>('keyword')
const {page, pageSize, total, data, send: fetchData} = usePagination((page, pageSize) => {
  const param: ITableQueryParams = {
    page, pageSize,
    keyword: currentKeyword.value,
    roleId: current.code
  }
  return userGroupApi.fetchUserByUserGroup(param);
}, {
  initialData: {total: 0, data: []},
  immediate: false,
  data: resp => resp.records,
  total: resp => resp.totalRow,
  watchingStates: [currentKeyword]
})
const columns: DataTableColumns<SimpleUserInfo> = [
  {type: 'selection', fixed: 'left'},
  {title: '用户名', key: 'name', ellipsis: {tooltip: true}},
  {title: '邮箱', key: 'email', ellipsis: {tooltip: true}},
  {title: '手机', key: 'phone', ellipsis: {tooltip: true}},
  {
    title: '操作', key: 'actions', fixed: 'right', width: 100,
    render(record) {
      if (hasAnyPermission(updatePermission)) {
        return h(RemoveButton, {
          title: `确认移除 ${record.name} 这个用户吗？`, subTitleTip: '移除后，将失去用户组权限',
          disabled: systemType === AuthScopeEnum.SYSTEM && record.name === 'admin',
          onConfirm: () => handleRemove(record)
        }, {})
      }
    }

  }
]
const {send: deleteUserFromUserGroup} = useRequest((id) => userGroupApi.deleteUserFromUserGroup(id), {immediate: false})
const handleRemove = (record: SimpleUserInfo) => {
  handlePermission(updatePermission, () => {
    if (systemType === AuthScopeEnum.SYSTEM) {
      deleteUserFromUserGroup(record.id).then(() => {
        fetchData();
      })
    }
  })
}
const handlePermission = (permission: string[], cb: () => void) => {
  if (!hasAnyPermission(permission)) {
    return false;
  }
  cb();
};
watchEffect(() => {
  if (current.id) {
    handlePermission(readPermission, () => {
      fetchData();
    })
  }
})
defineExpose({fetchData})
</script>

<template>
  <n-data-table :columns="columns"
                :data="data"
                :row-key="(row: SimpleUserInfo) => row.name"/>
  <base-pagination v-model:page="page" v-model:page-size="pageSize" :count="total||0"/>
</template>

<style scoped>

</style>