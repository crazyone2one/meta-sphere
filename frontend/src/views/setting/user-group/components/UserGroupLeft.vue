<script setup lang="ts">
import {ref} from "vue";
import CreateOrUpdateUserGroup from "/@/views/setting/user-group/components/CreateOrUpdateUserGroup.vue";

const systemToggle = ref(true);
const systemUserGroupVisible = ref(false);
const handleCreateUG = (scoped: string) => {
  if (scoped === "SYSTEM") {
    systemUserGroupVisible.value = true;
  }
}
</script>

<template>
  <div class="flex flex-col px-[16px] pb-[16px]">
    <div class="sticky top-0 z-[999] bg-[var(--color-text-fff)] pb-[8px] pt-[16px]">
      <n-input placeholder="请输入用户组名称" clearable/>
    </div>
    <div class="mt-2">
      <div class="flex items-center justify-between px-[4px] py-[7px]">
        <div class="flex flex-row items-center gap-1">
          <div v-if="systemToggle" class="cursor-pointer i-ant-design:down-outlined" @click="systemToggle = false"/>
          <div v-else class="cursor-pointer i-ant-design:right-outlined" @click="systemToggle = true"/>
          <div class="text-[14px]">系统用户组</div>
        </div>
        <create-or-update-user-group :visible="systemUserGroupVisible"
                                     @cancel="systemUserGroupVisible = false">
          <n-tooltip trigger="hover">
            <template #trigger>
              <div class="i-ant-design:plus-circle-outlined cursor-pointer" @click="handleCreateUG('SYSTEM')"/>
            </template>
            添加系统用户组
          </n-tooltip>
        </create-or-update-user-group>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>