<script setup lang="ts">
import {computed, h, ref, watch} from "vue";
import type {DataTableColumns, SelectOption} from "naive-ui";
import type {UserListItem} from "/@/api/modules/user/types.ts";
import {usePagination} from "alova/client";
import {orgAndProjectApi, type SystemGetUserByOrgOrProjectIdParams} from "/@/api/modules/setting/org-project";
import BasePagination from "/@/components/BasePagination.vue";
import UserAdminDiv from "/@/components/UserAdminDiv.vue";
import RemoveButton from "/@/components/RemoveButton.vue";
import AddUserModal from "/@/views/setting/org-project/components/AddUserModal.vue";
import {memberApi} from "/@/api/modules/setting/member.ts";
import {projectMemberApis} from "/@/api/modules/project/project-member.ts";
import {hasAnyPermission} from "/@/utils/permission.ts";

export interface projectDrawerProps {
  organizationId?: string;
  projectId?: string;
  currentName: string;
}

const active = defineModel<boolean>('active', {type: Boolean, default: false});
const props = defineProps<projectDrawerProps>();
const keyword = ref('');
const userVisible = ref(false);
const userGroupOptions = ref<Array<SelectOption>>([]);
const emit = defineEmits<{
  (e: 'cancel'): void;
  (e: 'requestFetchData'): void;
}>();
const hasOperationPermission = computed(() =>
    hasAnyPermission([
      'SYSTEM_ORGANIZATION_PROJECT:READ+RECOVER',
      'SYSTEM_ORGANIZATION_PROJECT:READ+UPDATE',
      'SYSTEM_ORGANIZATION_PROJECT:READ+DELETE',
    ])
);
const columns: DataTableColumns<UserListItem> = [
  {
    title: '用户名', key: 'name', ellipsis: {tooltip: true}, width: 200, render(record) {
      return h(UserAdminDiv, {isAdmin: record.adminFlag, name: record.name})
    }
  },
  {title: '用户组', key: 'userGroup', ellipsis: {tooltip: true}},
  {title: '邮箱', key: 'email', ellipsis: {tooltip: true}},
  {title: '手机', key: 'phone', ellipsis: {tooltip: true}},
  {
    title: hasOperationPermission.value ? '操作' : '', key: 'actions', fixed: 'right', width: 60,
    render(record) {
      if (hasAnyPermission(['SYSTEM_ORGANIZATION_PROJECT:READ+DELETE_MEMBER'])) {
        return h(RemoveButton, {
          title: `确认移除 ${record.name} 这个用户吗？`,
          subTitleTip: props.organizationId ? '移除后，将失去组织权限' : '移除后，将失去项目权限',
          onConfirm: () => handleRemove(record)
        }, {})
      }
    }
  }
]
const {page, pageSize, total, data, send: fetchData} = usePagination((page, pageSize) => {
  const param: SystemGetUserByOrgOrProjectIdParams = {
    page, pageSize,
    keyword: keyword.value,
  }
  if (props.organizationId) {
    param.organizationId = props.organizationId;
  }
  if (props.projectId) {
    param.projectId = props.projectId;
  }
  return orgAndProjectApi.postUserPageByOrgIdOrProjectId(param);
}, {
  initialData: {total: 0, data: []},
  immediate: false,
  data: resp => resp.records,
  total: resp => resp.totalRow,
  watchingStates: [keyword]
})
const handleCancel = () => {
  emit('cancel');
  keyword.value = '';
};

const handleAddMemberSubmit = () => {
  fetchData();
  emit('requestFetchData');
};
const handleAddMember = () => userVisible.value = true;
const handleRemove = async (record: UserListItem) => {
  try {
    if (props.organizationId) {
      await orgAndProjectApi.deleteUserFromOrgOrProject(props.organizationId, record.id);
    }
    if (props.projectId) {
      await orgAndProjectApi.deleteUserFromOrgOrProject(props.projectId, record.id, false);
    }
    window.$message.success('移除成功');
    await fetchData();
    emit('requestFetchData');
  } catch (error) {
    console.error(error);
  }
};
const getUserGroupOptions = async () => {
  try {
    if (props.organizationId) {
      // 系统-组织与项目-组织-成员用户组下拉
      userGroupOptions.value = await memberApi.getGlobalUserGroup(props.organizationId);
    } else if (props.projectId) {
      // 系统-组织与项目-项目-成员用户组下拉 和 组织-项目-成员用户组下拉
      userGroupOptions.value = await projectMemberApis.getProjectUserGroup(props.projectId);
    }
  } catch (error) {
    console.log(error);
  }
};
watch([() => active.value], () => {
  if (active.value) {
    fetchData()
    getUserGroupOptions();
  }
})
</script>

<template>
  <n-drawer v-model:show="active" :width="800" :mask-closable="false" @close="handleCancel">
    <n-drawer-content closable>
      <template #header>
        <div class="flex flex-1 items-center justify-between overflow-hidden">
          <div class="flex flex-1 items-center overflow-hidden">
            成员列表
            <div class="ml-[8px] flex flex-1 overflow-hidden text-[#9597a4]">
              ( {{ props.currentName }} )
            </div>
          </div>
        </div>
      </template>
      <div>
        <n-flex justify="space-between">
          <n-button v-permission="['SYSTEM_ORGANIZATION_PROJECT:READ+ADD_MEMBER']" @click="handleAddMember">添加成员</n-button>
          <div>
            <n-input v-model:value="keyword" placeholder="通过名称/邮箱/手机搜索" class="w-[230px]" clearable/>
          </div>
        </n-flex>
        <div class="mt-[16px]">
          <n-data-table :columns="columns"
                        :data="data"
                        :row-key="(row: UserListItem) => row.id"/>
          <base-pagination v-model:page="page" v-model:page-size="pageSize" :count="total||0"/>
        </div>
      </div>
    </n-drawer-content>
  </n-drawer>
  <add-user-modal v-model:show-modal="userVisible" :project-id="props.projectId"
                  :organization-id="props.organizationId"
                  :user-group-options="userGroupOptions"
                  @submit="handleAddMemberSubmit"/>
</template>

<style scoped>

</style>