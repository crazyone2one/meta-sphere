<script setup lang="ts">
import {UserSelector} from "/@/components/user-selector";
import {useForm} from "alova/client";
import {userGroupApi} from "/@/api/modules/setting/user-group.ts";
import {computed, inject, ref} from "vue";
import {AuthScopeEnum, type AuthScopeEnumType} from "/@/enums/common-enum.ts";
import {UserRequestTypeEnum} from "/@/components/user-selector/utils.ts";
import {useAppStore} from "/@/store";
import type {FormInst} from "naive-ui";

const systemType = inject<AuthScopeEnumType>('systemType');
const appStore = useAppStore()
const showModal = defineModel<boolean>('showModal', {type: Boolean, default: false});
const {currentId = ''} = defineProps<{ currentId: string }>()
const formRef = ref<FormInst | null>(null)
const currentOrgId = computed(() => appStore.currentOrgId);
const userSelectorProps = computed(() => {
  if (systemType === AuthScopeEnum.SYSTEM) {
    return {
      type: UserRequestTypeEnum.SYSTEM_USER_GROUP,
      loadOptionParams: {
        roleId: currentId,
      },
      disabledKey: 'exclude',
    };
  }
  return {
    type: UserRequestTypeEnum.ORGANIZATION_USER_GROUP,
    loadOptionParams: {
      roleId: currentId,
      organizationId: currentOrgId.value,
    },
    disabledKey: 'checkRoleFlag',
  };
})
const emit = defineEmits<(e: 'cancel', shouldSearch: boolean) => void>();
const handleCancel = (shouldSearch = false) => {
  emit('cancel', shouldSearch);
}
const {form, send, loading} = useForm(formData => {
  if (systemType === AuthScopeEnum.SYSTEM) {
    return userGroupApi.addUserToUserGroup(formData);
  } else {
    return userGroupApi.addOrgUserToUserGroup({...formData, organizationId: currentOrgId.value});
  }
}, {
  initialForm: {
    roleId: currentId,
    userIds: []
  },
  immediate: false,
  resetAfterSubmiting: true
})
const handleSubmit = () => {
  formRef.value?.validate(errors => {
    if (!errors) {
      send().then(() => {
        handleCancel(true);
        window.$message.success('添加成员成功');
      });
    }
  })
}
const rules = {
  userIds: [
    {
      required: true,
      message: '请选择成员',
    }
  ]
}
</script>

<template>
  <n-modal v-model:show="showModal" preset="dialog" title="Dialog" @close="handleCancel(false)">
    <template #header>
      <div>添加成员</div>
    </template>
    <div class="form">
      <n-form ref="formRef" :model="form" :rules="rules" class="rounded-[4px]">
        <n-form-item label="成员" path="userIds">
          <user-selector v-model:model-value="form.userIds" :type="userSelectorProps.type"
                         :loadOptionParams="userSelectorProps.loadOptionParams"/>
        </n-form-item>
      </n-form>
    </div>
    <template #action>
      <n-flex>
        <n-button secondary :loading="loading" @click="handleCancel(false)">取消</n-button>
        <n-button type="primary" :loading="loading" @click="handleSubmit">确定</n-button>
      </n-flex>
    </template>
  </n-modal>
</template>

<style scoped>

</style>