<script setup lang="ts">
import {computed, inject, ref} from "vue";
import CreateOrUpdateUserGroup from "/@/views/setting/user-group/components/CreateOrUpdateUserGroup.vue";
import {AuthScopeEnum, type AuthScopeEnumType} from "/@/enums/common-enum.ts";
import type {CurrentUserGroupItem, UserGroupItem} from "/@/api/modules/setting/types.ts";
import {userGroupApi} from "/@/api/modules/setting/user-group.ts";
import AddUserModal from "/@/views/setting/user-group/components/AddUserModal.vue";

const systemType = inject<AuthScopeEnumType>('systemType');
const emit = defineEmits<{
  (e: 'handleSelect', element: UserGroupItem): void;
  (e: 'addUserSuccess', id: string): void;
}>();
const {isGlobalDisable = false} = defineProps<{
  addPermission?: string[];
  updatePermission?: string[];
  isGlobalDisable: boolean;
}>();
const systemToggle = ref(true);
const systemUserGroupVisible = ref(false);
const userGroupList = ref<UserGroupItem[]>([]);
const currentItem = ref<CurrentUserGroupItem>({id: '', name: '', internal: false, type: AuthScopeEnum.SYSTEM, code: ''});
const currentId = ref('');
const userModalVisible = ref(false);

const handleCreateUG = (scoped: string) => {
  if (scoped === "SYSTEM") {
    systemUserGroupVisible.value = true;
  }
}
const systemUserGroupList = computed(() => {
  return userGroupList.value.filter((ele) => ele.type === AuthScopeEnum.SYSTEM);
});
const handleListItemClick = (element: UserGroupItem) => {
  const {id, name, type, internal, code} = element;
  currentItem.value = {id, name, type, internal, code};
  currentId.value = id;
  emit('handleSelect', element);
};
const initData = async (id?: string, isSelect = true) => {
  if (systemType === AuthScopeEnum.SYSTEM) {
    userGroupApi.getUserGroupList().then(res => {
      if (res.length > 0) {
        userGroupList.value = res;
        if (isSelect) {
          if (id) {
            const item = res.find((i) => i.id === id);
            if (item) {
              handleListItemClick(item);
            } else {
              window.$message.warning('资源已被删除');
              handleListItemClick(res[0] as UserGroupItem);
            }
          } else {
            handleListItemClick(res[0] as UserGroupItem);
          }
        }
      }
    })
  }
}
const handleCreateUserGroup = (id: string) => {
  initData(id);
};
const handleAddMember = () => {
  userModalVisible.value = true;
};
const handleAddUserCancel = (shouldSearch: boolean) => {
  userModalVisible.value = false;
  if (shouldSearch) {
    emit('addUserSuccess', currentId.value);
  }
};
defineExpose({
  initData,
});
</script>

<template>
  <div class="flex flex-col px-[16px] pb-[16px]">
    <div class="sticky top-0 z-[999] bg-[var(--color-text-fff)] pb-[8px] pt-[16px]">
      <n-input placeholder="请输入用户组名称" clearable/>
    </div>
    <div class="mt-2">
      <div class="flex items-center justify-between px-[4px] py-[7px]">
        <div class="flex flex-row items-center gap-1">
          <div v-if="systemToggle" class="cursor-pointer i-ant-design:down-outlined text-[12px]"
               @click="systemToggle = false"/>
          <div v-else class="cursor-pointer i-ant-design:right-outlined text-[12px]" @click="systemToggle = true"/>
          <div class="text-[14px]">系统用户组</div>
        </div>
        <create-or-update-user-group :visible="systemUserGroupVisible"
                                     :auth-scope="AuthScopeEnum.SYSTEM"
                                     :list="systemUserGroupList"
                                     @cancel="systemUserGroupVisible = false"
                                     @submit="handleCreateUserGroup">
          <n-tooltip trigger="hover">
            <template #trigger>
              <div class="i-ant-design:plus-circle-outlined cursor-pointer text-[20px]"
                   @click="handleCreateUG('SYSTEM')"/>
            </template>
            添加系统用户组
          </n-tooltip>
        </create-or-update-user-group>
      </div>
      <Transition>
        <div v-if="systemToggle">
          <div v-for="element in systemUserGroupList"
               :key="element.id"
               class="list-item"
               :class="{ '!bg-green-50': element.id === currentId }"
               @click="handleListItemClick(element)">
            <create-or-update-user-group :list="systemUserGroupList" :auth-scope="element.type" :visible="false">
              <div class="flex max-w-[100%] grow flex-row items-center justify-between">
                <div
                    class="one-line-text"
                    :class="{ '!text-[#18a058]': element.id === currentId }"
                >
                  {{ element.name }}
                </div>
                <div v-if="
                    element.type === systemType ||
                    (!element.internal &&
                      (element.scopeId !== 'global' || !isGlobalDisable))
                  "
                     class="list-item-action flex flex-row items-center gap-[8px] opacity-0"
                     :class="{ '!opacity-100': element.id === currentId }">
                  <div v-if="element.type === systemType">
                    <n-button text @click="handleAddMember">
                      <div class="i-ant-design:plus-outlined cursor-pointer text-[16px]"/>
                    </n-button>
                  </div>
                </div>
              </div>
            </create-or-update-user-group>
          </div>
        </div>
      </Transition>
    </div>
  </div>
  <add-user-modal v-model:show-modal="userModalVisible" :current-id="currentItem.code" @cancel="handleAddUserCancel"/>
</template>

<style scoped>
.list-item {
  padding: 7px 4px 7px 20px;
  height: 28px;
  border-radius: 2px;
  @apply flex cursor-pointer items-center;

  &:hover .list-item-action {
    opacity: 1;
  }
}
</style>