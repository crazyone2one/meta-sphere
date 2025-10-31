<script setup lang="ts">
import {inject, ref, watchEffect} from "vue";
import type {FormInst, FormItemRule} from "naive-ui";
import type {UserGroupItem} from "/@/api/modules/setting/types.ts";
import {useForm} from "alova/client";
import {userGroupApi} from "/@/api/modules/setting/user-group.ts";
import {AuthScopeEnum, type AuthScopeEnumType} from "/@/enums/common-enum.ts";

const systemType = inject<AuthScopeEnumType>('systemType');
const {visible = false, id = undefined, authScope = AuthScopeEnum.SYSTEM, defaultName = '', list = []} = defineProps<{
  visible: boolean,
  id?: string,
  list: UserGroupItem[],
  defaultName?: string
  // 权限范围
  authScope: AuthScopeEnumType;
}>()
const emit = defineEmits<{
  (e: 'cancel', value: boolean): void;
  (e: 'submit', currentId: string): void;
}>();
const formRef = ref<FormInst | null>(null)
const rules = {
  name: [
    {
      required: true,
      validator: (_rule: FormItemRule, value: string) => {
        if (!value) {
          return new Error('用户组名称不能为空')
        } else {
          if (value === defaultName) {
            return true
          } else {
            const isExist = list.some((item) => item.name === value);
            if (isExist) {
              return new Error(`已有 ${value} ，请更改`)
            }
          }
          if (value.length > 255) {
            return new Error(`名称不能超过 255 个字符`)
          }
          return true
        }
      }
    }
  ]
}
const handleCancel = () => {
  form.value.name = '';
  form.value.code = '';
  emit('cancel', false);
};
const {form, send, loading} = useForm(formData => userGroupApi.updateOrAddUserGroup(formData), {
  immediate: false,
  initialForm: {
    name: '',
    code: '',
    type: authScope,
    id: id
  }
})
const handleSubmit = () => {
  formRef.value?.validate(errors => {
    if (!errors) {
      if (systemType === AuthScopeEnum.SYSTEM) {
        send().then(res => {
          window.$message.success(id ? '更新用户组成功' : '添加用户组成功');
          emit('submit', res.id);
          handleCancel();
        })
      }
    }
  })
};
const handleOutsideClick = () => {
  if (visible) {
    handleCancel();
  }
}
watchEffect(() => {
  form.value.name = defaultName || '';
});
</script>

<template>
  <n-popover :show="visible" trigger="click" placement="bottom-end" class="w-[350px]"
             :content-class="id?'move-left':''">
    <template #trigger>
      <slot></slot>
    </template>
    <div v-outer="handleOutsideClick">
      <div class="form">
        <div class="mb-[8px] text-[14px] font-medium">
          {{ id ? '重命名' : '创建用户组' }}
        </div>
        <span v-if="!id" class="mt-[8px] text-[13px] font-medium">该用户组将在整个系统范围内可用</span>
        <n-form ref="formRef" :model="form" :rules="rules" inline>
          <n-form-item path="name">
            <n-input v-model:value="form.name" placeholder="请输入用户组名称" clearable :maxlength="255"
                     @keyup.esc="handleCancel"/>
          </n-form-item>
          <n-form-item path="code">
            <n-input v-model:value="form.code" placeholder="请输入用户组code" clearable :maxlength="255"
                     @keyup.esc="handleCancel"/>
          </n-form-item>
        </n-form>
      </div>
      <div class="flex flex-row flex-nowrap justify-end gap-2">
        <n-button secondary size="tiny" :disabled="loading" @click="handleCancel">取消</n-button>
        <n-button type="primary" size="tiny" :disabled="form.name.length === 0" :loading="loading"
                  @click="handleSubmit">
          {{ id ? '确认' : '创建' }}
        </n-button>
      </div>
    </div>
  </n-popover>
</template>

<style scoped>
.move-left {
  position: relative;
  right: 22px;
}
</style>