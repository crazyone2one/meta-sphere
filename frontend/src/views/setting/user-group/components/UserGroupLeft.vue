<script setup lang="ts">
import {computed, inject, ref} from "vue";
import CreateOrUpdateUserGroup from "/@/views/setting/user-group/components/CreateOrUpdateUserGroup.vue";
import {AuthScopeEnum, type AuthScopeEnumType} from "/@/enums/common-enum.ts";
import type {CurrentUserGroupItem, PopVisible, UserGroupItem} from "/@/api/modules/setting/types.ts";
import {userGroupApi} from "/@/api/modules/setting/user-group.ts";
import AddUserModal from "/@/views/setting/user-group/components/AddUserModal.vue";
import TableMoreAction from '/@/components/table-more-action/index.vue'
import {hasAnyPermission} from "/@/utils/permission.ts";

const systemType = inject<AuthScopeEnumType>('systemType');
const emit = defineEmits<{
  (e: 'handleSelect', element: UserGroupItem): void;
  (e: 'addUserSuccess', id: string): void;
}>();
const {isGlobalDisable = false, updatePermission = []} = defineProps<{
  addPermission?: string[];
  updatePermission?: string[];
  isGlobalDisable: boolean;
}>();
const systemToggle = ref(true);
const systemUserGroupVisible = ref(false);
const userGroupList = ref<UserGroupItem[]>([]);
const currentItem = ref<CurrentUserGroupItem>({
  id: '',
  name: '',
  internal: false,
  type: AuthScopeEnum.SYSTEM,
  code: ''
});
const currentId = ref('');
const userModalVisible = ref(false);
// 气泡弹窗
const popVisible = ref<PopVisible>({});
const handleCreateUG = (scoped: string) => {
  if (scoped === "SYSTEM") {
    systemUserGroupVisible.value = true;
  }
}
const showSystem = computed(() => systemType === AuthScopeEnum.SYSTEM);
const systemUserGroupList = computed(() => {
  return userGroupList.value.filter((ele) => ele.type === AuthScopeEnum.SYSTEM);
});
const isSystemShowAll = computed(() => {
  return hasAnyPermission([...updatePermission, 'SYSTEM_USER_ROLE:READ+DELETE']);
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
        // 弹窗赋值
        const tmpObj: PopVisible = {};
        res.forEach((element) => {
          tmpObj[element.id] = {
            visible: false,
            authScope: element.type,
            defaultName: '',
            id: element.id,
            defaultCode: ''
          };
        });
        popVisible.value = tmpObj;
      }
    })
  }
}
const handleCreateUserGroup = (id: string) => {
  initData(id);
};
const handleAddMember = (element: UserGroupItem) => {
  handleListItemClick(element)
  userModalVisible.value = true;
};
const handleAddUserCancel = (shouldSearch: boolean) => {
  userModalVisible.value = false;
  if (shouldSearch) {
    emit('addUserSuccess', currentId.value);
  }
};
const tableActions = [{label: '重命名', key: 'rename'}, {type: 'divider', key: 'd1'}, {label: '删除', key: 'delete'},]
const handleMoreAction = (v: string, id: string, authScope: AuthScopeEnumType) => {
  const tmpObj = userGroupList.value.filter((ele) => ele.id === id)[0];
  if (v === 'rename') {
    popVisible.value[id] = {
      visible: true,
      authScope,
      defaultName: tmpObj?.name ?? '',
      id,
      defaultCode: tmpObj?.code ?? ''
    };
  }
};
const handleRenameCancel = (element: UserGroupItem, id?: string) => {
  if (id) {
    initData(id, true);
  }
  const tmp = popVisible.value[element.id];
  if (tmp) {
    tmp.visible = false;
  }
}
defineExpose({
  initData,
});
</script>

<template>
  <div class="flex flex-col px-[16px] pb-[16px]">
    <div class="sticky top-0 z-[999] bg-[var(--color-text-fff)] pb-[8px] pt-[16px]">
      <n-input placeholder="请输入用户组名称" clearable/>
    </div>
    <div v-if="showSystem" v-permission="['SYSTEM_USER_ROLE:READ']" class="mt-2">
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
          <n-tooltip trigger="hover" placement="right">
            <template #trigger>
              <div class="i-ant-design:plus-circle-outlined cursor-pointer text-[20px]"
                   @click="handleCreateUG('SYSTEM')"/>
            </template>
            创建系统用户组
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
            <create-or-update-user-group :list="systemUserGroupList"
                                         :visible="popVisible[element.id]?.visible || false"
                                         :id="popVisible[element.id]?.id"
                                         :auth-scope="popVisible[element.id]?.authScope || AuthScopeEnum.SYSTEM"
                                         :default-name="popVisible[element.id]?.defaultName"
                                         :default-code="popVisible[element.id]?.defaultCode"
                                         @cancel="handleRenameCancel(element)"
                                         @submit="handleRenameCancel(element, element.id)"
            >
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
                    <n-button text @click="handleAddMember(element)">
                      <div class="i-ant-design:plus-outlined cursor-pointer text-[18px]"/>
                    </n-button>
                  </div>
                  <table-more-action v-if="isSystemShowAll &&
                      !element.internal &&  (element.scopeId !== 'global' || !isGlobalDisable)" :options="tableActions"
                                     :class-name="'text-green'" :size="20"
                                     @select="(v)=>handleMoreAction(v, element.id,AuthScopeEnum.SYSTEM)"/>
                </div>
              </div>
            </create-or-update-user-group>
          </div>
        </div>
      </Transition>
    </div>
  </div>
  <add-user-modal v-model:show-modal="userModalVisible" :current-code="currentItem.code" @cancel="handleAddUserCancel"/>
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