<script setup lang="ts">
import {ref} from "vue";
import UserGroupLeft from "/@/views/setting/user-group/components/UserGroupLeft.vue";

const userGroupLeftRef = ref<InstanceType<typeof UserGroupLeft>>()
</script>

<template>
  <n-card>
    <n-split :min="0.2" :max="0.8" :default-size="0.4">
      <template #1>
        <user-group-left ref="userGroupLeftRef"/>
      </template>
      <template #2>
        <div class="flex h-full flex-col overflow-hidden pt-[16px]">
          <div class="flex flex-row items-center justify-between px-[16px]">
            <n-radio-group>
              <n-radio>权限</n-radio>
              <n-radio>成员</n-radio>
            </n-radio-group>
            <div class="flex items-center">
              <n-input placeholder="通过姓名/邮箱/手机搜索" class="w-[240px]" clearable/>
            </div>
          </div>
          <div class="flex-1 overflow-hidden">
            right
          </div>
        </div>
      </template>
    </n-split>
  </n-card>
</template>

<style scoped>

</style>