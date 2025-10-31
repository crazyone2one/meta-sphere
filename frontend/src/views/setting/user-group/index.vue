<script setup lang="ts">
import {computed, onMounted, provide, ref, watchEffect} from "vue";
import UserGroupLeft from "/@/views/setting/user-group/components/UserGroupLeft.vue";
import {AuthScopeEnum} from "/@/enums/common-enum.ts";
import type {CurrentUserGroupItem} from "/@/api/modules/setting/types.ts";
import {useRouter} from "vue-router";
import AuthTable from "/@/views/setting/user-group/components/AuthTable.vue";

const router = useRouter();
const userGroupLeftRef = ref<InstanceType<typeof UserGroupLeft>>()
provide('systemType', AuthScopeEnum.SYSTEM);
const currentTable = ref('auth');
const currentUserGroupItem = ref<CurrentUserGroupItem>({
  id: '',
  name: '',
  type: AuthScopeEnum.SYSTEM,
  internal: true,
});
const handleSelect = (item: CurrentUserGroupItem) => {
  currentUserGroupItem.value = item;
};
const couldShowUser = computed(() => currentUserGroupItem.value.type === AuthScopeEnum.SYSTEM);
watchEffect(() => {
  if (!couldShowUser.value) {
    currentTable.value = 'auth';
  } else {
    currentTable.value = 'auth';
  }
});
onMounted(() => {
  userGroupLeftRef.value?.initData(router.currentRoute.value.query.id as string, true);
});
</script>

<template>
  <n-card>
    <n-split :min="0.2" :max="0.8" :default-size="0.2">
      <template #1>
        <user-group-left ref="userGroupLeftRef" :is-global-disable="false" @handle-select="handleSelect"/>
      </template>
      <template #2>
        <div>
          <div class="flex h-full flex-col overflow-hidden pt-[16px]">
            <div class="flex flex-row items-center justify-between px-[16px]">
              <n-radio-group v-if="couldShowUser" v-model:value="currentTable" name="systemUserGroup" class="mb-[16px]">
                <n-radio value="auth" class="p-[2px]">权限</n-radio>
                <n-radio value="user" class="p-[2px]">成员</n-radio>
              </n-radio-group>
              <div class="flex items-center">
                <n-input v-if="currentTable === 'user'" class="w-[240px]" placeholder="通过姓名/邮箱/手机搜索" clearable/>
              </div>
            </div>
          </div>
          <div class="flex-1 overflow-hidden">
            <auth-table v-if="currentTable === 'auth'"
                        :current="currentUserGroupItem"/>
          </div>
        </div>
      </template>
    </n-split>
  </n-card>
</template>

<style scoped>

</style>