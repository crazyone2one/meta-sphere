<script setup lang="ts">
import type {UserModalMode} from "/@/views/setting/user/index.vue";
import {type FormInst, type FormItemRule, type SelectOption} from "naive-ui";
import {computed, ref, unref, watch, watchEffect} from "vue";
import type {CreateUserResult, SimpleUserInfo, UpdateUserInfoParams, UserForm} from "/@/api/modules/user/types.ts";
import {validateEmail, validatePhone} from "/@/utils/validate.ts";
import {userApi} from "/@/api/modules/user";
import {useForm} from "alova/client";

const {mode = 'create', userGroupOptions = []} = defineProps<{
  mode?: UserModalMode,
  userGroupOptions: SelectOption[]
}>()
const emit = defineEmits<(e: 'close', shouldSearch: boolean) => void>();
const formRef = ref<FormInst | null>(null)
const showModal = defineModel<boolean>('showModal', {type: Boolean, default: false});
const defaultUserForm = {
  list: [
    {
      name: '',
      email: '',
      phone: '',
    },
  ],
  userGroup: [],
};
const dynamicUserForm = defineModel<UserForm>('userForm', {
  default: {
    list: [
      {
        name: '',
        email: '',
        phone: '',
      },
    ],
    userGroup: [],
  }
});

const handleClose = (shouldSearch: boolean) => {
  emit('close', shouldSearch);
  formRef.value?.restoreValidation()
  reset()
};
const {form, loading, send, reset} = useForm(formData => {
  if (mode === 'create') {
    const params = {
      userInfoList: formData.list,
      userRoleIdList: formData.userGroup,
    };
    return userApi.batchCreateUser(params);
  } else {
    const activeUser = form.value.list[0];
    const params: UpdateUserInfoParams = {
      id: activeUser.id,
      name: activeUser.name,
      email: activeUser.email,
      phone: activeUser.phone,
      userRoleIdList: form.value.userGroup,
    };
    return userApi.updateUserInfo(params);
  }
}, {
  initialForm: defaultUserForm,
  resetAfterSubmiting: true,
  immediate: false
})
const handleSubmit = () => {
  formRef.value?.validate(err => {
    if (!err) {
      send().then(res => {
        if (mode === 'create') {
          const r = res as CreateUserResult;
          if (r.errorEmails !== null) {
            window.$message.error('邮箱重复');
          } else {
            window.$message.success('创建成功');
            handleClose(true);
          }
        } else {
          window.$message.success('更新成功');
          handleClose(true);
        }
      })
    }
  })
};
const getFormResult = () => {
  return unref<SimpleUserInfo[]>(form.value.list);
}
const formValidate = (cb: (res?: Record<string, any>[]) => void, isSubmit = true) => {
  formRef.value?.validate(err => {
    if (!err) {
      if (typeof cb === 'function') {
        if (isSubmit) {
          cb(getFormResult());
          return;
        }
        cb();
      }
    }
  })
}
const addField = () => {
  const item = {name: '', email: '', phone: ''};
  formValidate(() => {
    form.value.list.push(item); // 序号自增，不会因为删除而重复
  }, false);
}
const removeField = (i: number) => {
  form.value.list.splice(i, 1);
}


const nameValidator = computed(() => {
  return {
    required: true, trigger: 'blur', validator: (_rule: FormItemRule, value: string) => {
      if (value === '' || value === undefined) {
        return new Error('姓名不能为空') // reject with error message
      } else if (value.length > 255) {
        return new Error('姓名长度不能超过 50')
      } else {
        return true
      }
    }
  }
})
const emailValidator = computed(() => (index: number) => {
  return [
    {
      required: true, trigger: 'blur', validator: (_rule: FormItemRule, value: string) => {
        if (value === '' || value === undefined) {
          return new Error('邮箱不能为空') // reject with error message
        } else if (validateEmail(value)) {
          return true
        } else {
          return new Error('请输入正确的邮箱')
        }
      }
    },
    {
      validator: (_rule: FormItemRule, value: string) => {
        for (let i = 0; i < form.value.list.length; i++) {
          if (i !== index && form.value.list[i].email === value) {
            return new Error('邮箱不能重复')
          }
        }
      },
      message: '邮箱不能重复',
      trigger: 'blur'
    }
  ];
})
const phoneValidator = computed(() => {
  return {
    trigger: 'blur', validator: (_rule: FormItemRule, value: string) => {
      if (value !== null && value !== '' && value !== undefined && !validatePhone(value)) {
        return new Error('请输入 11 位手机号')
      } else {
        return true
      }
    }
  }
})

watch(() => userGroupOptions, (newValue) => {
  if (newValue.length > 0) {
    form.value.userGroup = newValue.filter((e) => e.selected).map(e => e.id);
  }
})
watchEffect(() => {
  if (showModal.value) {
    form.value = dynamicUserForm.value;
  } else {
    dynamicUserForm.value = {
      list: [
        {
          name: '',
          email: '',
          phone: '',
        },
      ],
      userGroup: [],
    }
  }
})
</script>

<template>
  <n-modal v-model:show="showModal" preset="dialog" title="Dialog"
           :style="{ width:  '30%' }"
           :closable="false" :mask-closable="false">
    <template #header>
      <div>{{ mode === 'create' ? '创建用户' : '编辑用户' }}</div>
    </template>
    <div>
      <n-form ref="formRef" :model="form" label-placement="top" size="small">
        <n-scrollbar class="overflow-y-auto" :style="{ 'max-height': '30vh' }" trigger="none">
          <div v-for="(element, index) in form.list" :key="`${index}`"
               class="flex w-full items-start justify-between rounded gap-[8px] py-[3px] pr-[8px] ">
            <n-form-item label="name" :path="`list[${index}].name`" :show-label="index===0" class="flex-1"
                         :rule="nameValidator">
              <n-input v-model:value="element.name" placeholder="请输入用户姓名"/>
            </n-form-item>
            <n-form-item label="email" :path="`list[${index}].email`" :show-label="index===0" class="flex-1"
                         :rule="emailValidator(index)">
              <n-input v-model:value="element.email" placeholder="请输入邮箱地址"/>
            </n-form-item>
            <n-form-item label="phone" :path="`list[${index}].phone`" :show-label="index===0" class="flex-1"
                         :rule="phoneValidator">
              <n-input v-model:value="element.phone" placeholder="请输入 11 位手机号"/>
            </n-form-item>
            <div class="flex items-center h-[34px]" :class="index===0?'mt-[25px]':''">
              <n-switch v-model:value="element.enable" size="small"/>
            </div>
            <div v-show="form.list.length > 1"
                 class="minus flex h-[32px] w-[32px] cursor-pointer items-center justify-center rounded"
                 @click="removeField(index)">
              <div class="i-ant-design:minus-circle-outlined" :class="index===0?'mt-[50px]':''"/>
            </div>
          </div>
        </n-scrollbar>
        <div v-if="mode==='create'" class="w-full mt-3">
          <n-button text type="info" class="px-0" @click="addField"> 添加</n-button>
        </div>
        <n-form-item path="userGroup">
          <n-select v-model:value="form.userGroup" :options="userGroupOptions" multiple
                    placeholder="请选择用户组" filterable value-field="id" label-field="name"/>
        </n-form-item>
      </n-form>
    </div>

    <template #action>
      <n-button secondary size="small" :disabled="loading" @click="handleClose(false)">取消</n-button>
      <n-button size="small" type="primary" :loading="loading" @click="handleSubmit">
        {{ mode === 'create' ? '创建' : '保存' }}
      </n-button>
    </template>
  </n-modal>
</template>

<style scoped>
.minus {
  margin: 0 !important;
}
</style>