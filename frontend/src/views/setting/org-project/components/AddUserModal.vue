<script setup lang="ts">

import {ref, watch} from "vue";
import type {DataTableColumns, DataTableRowKey, SelectOption} from "naive-ui";
import {usePagination, useRequest} from "alova/client";
import type {ITableQueryParams} from "/@/api/types.ts";
import {memberApi} from "/@/api/modules/setting/member.ts";
import type {MemberUserItem} from "/@/api/modules/user/types.ts";
import BasePagination from "/@/components/BasePagination.vue";
import {projectMemberApis} from "/@/api/modules/project/project-member.ts";
import {orgAndProjectApi} from "/@/api/modules/setting/org-project";
import BaseModal from '/@/components/base-modal/index.vue'

const {isOrganization = false, projectId = '', organizationId = '', userGroupOptions = []} = defineProps<{
  isOrganization?: boolean; // 组织下的
  userGroupOptions?: Array<SelectOption>;
  organizationId?: string;
  projectId?: string;
}>()
const showModal = defineModel<boolean>('showModal', {type: Boolean, default: false});
const emit = defineEmits<{
  (e: 'submit'): void;
}>();
const keyword = ref('');
const userGroupIds = ref<string[]>([]);
const currentUserGroupOptions = ref<Array<SelectOption>>([]);
const columns: DataTableColumns<MemberUserItem> = [
  {
    type: 'selection', disabled(row) {
      return row.memberFlag
    }
  },
  {title: '用户名', key: 'name', ellipsis: {tooltip: true}},
  {title: '邮箱', key: 'email'}
]
const checkedRowKeys = ref<DataTableRowKey[]>([])
const handleCheck = (rowKeys: DataTableRowKey[]) => {
  checkedRowKeys.value = rowKeys
}
const {page, pageSize, total, data, send: fetchData} = usePagination((page, pageSize) => {
  const param: ITableQueryParams = {
    page, pageSize,
    keyword: keyword.value,
    sourceId: projectId ?? organizationId,
    projectId: projectId,
    organizationId: organizationId,
  }
  return !isOrganization ? memberApi.getSystemMemberListPage(param) : memberApi.getOrganizationMemberListPage(param);
}, {
  initialData: {total: 0, data: []},
  immediate: false,
  data: resp => resp.records,
  total: resp => resp.totalRow,
  watchingStates: [keyword]
})
const handleCancel = () => {
  showModal.value = false;
};
const {send: addUser, loading} = useRequest(param => {
  if (!isOrganization) {
    return orgAndProjectApi.addUserToOrgOrProject(param)
  }
  return orgAndProjectApi.addProjectMemberByOrg(param)
}, {immediate: false})
const handleSubmit = () => {
  let param: {}
  if (!isOrganization) {
    param = {
      userRoleIds: userGroupIds.value,
      userIds: checkedRowKeys.value,
      projectId,
      organizationId
    };
  } else {
    param = {
      userRoleIds: userGroupIds.value,
      userIds: checkedRowKeys.value,
      projectId,
    }
  }
  addUser(param).then(() => {
    window.$message.success('添加成功')
    emit('submit')
    handleCancel()
  });
}
const getUserGroupOptions = async () => {
  try {
    if (organizationId && !isOrganization) {
      // 系统-组织与项目-组织-成员用户组下拉
      currentUserGroupOptions.value = await memberApi.getGlobalUserGroup(organizationId);
    } else if (projectId) {
      // 系统-组织与项目-项目-成员用户组下拉 和 组织-项目-成员用户组下拉
      currentUserGroupOptions.value = await projectMemberApis.getProjectUserGroup(projectId);
    }
  } catch (error) {
    console.log(error);
  }
};
watch(() => showModal.value, (newValue) => {
  if (newValue) {
    fetchData();
    if (userGroupOptions.length === 0) {
      getUserGroupOptions();
    } else {
      currentUserGroupOptions.value = userGroupOptions;
    }
    if (projectId) {
      userGroupIds.value = ['project_member']
    } else if (organizationId) {
      userGroupIds.value = ['org_member']
    }
  }
})
</script>

<template>
  <base-modal v-model:show-modal="showModal" style="width: 680px" :loading="loading"
              ok-text="添加"
              :disable="!userGroupIds.length || !checkedRowKeys.length"
              @cancel="handleCancel"
              @update="handleSubmit">
    <template #title>
      添加成员
    </template>
    <div class="mb-[16px] flex justify-end">
      <div>
        <n-input v-model:value="keyword" class="w-[240px]" placeholder="通过名称/邮箱搜索" clearable/>
      </div>
    </div>
    <div class="mt-[16px]">
      <n-data-table :columns="columns"
                    :data="data"
                    :row-key="(row: MemberUserItem) => row.id"
                    @update:checked-row-keys="handleCheck"/>
      <base-pagination v-model:page="page" v-model:page-size="pageSize" :count="total||0"/>
    </div>
    <template #actionLeft>
      <div class="text-nowrap">用户组</div>
      <n-select v-model:value="userGroupIds" multiple placeholder="请为以上成员选择用户组"
                :options="currentUserGroupOptions" label-field="name" value-field="id"/>
    </template>
  </base-modal>

</template>

<style scoped>

</style>