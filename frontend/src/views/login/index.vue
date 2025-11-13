<script setup lang="ts">
import {computed, onMounted, reactive, ref} from "vue";
import type {NForm} from "naive-ui";
import {addGlobalStyles, initParticles} from "/@/views/login/index.ts";
import {useForm} from "alova/client";
import {authApi} from "/@/api/modules/auth";
import {setToken} from "/@/utils/auth.ts";
import {useAppStore, useUserStore} from "/@/store";
import {useRouter} from "vue-router";

const appStore = useAppStore()
const userStore = useUserStore()
const router = useRouter()
const isDarkMode = ref<boolean>(window.matchMedia('(prefers-color-scheme: dark)').matches);
const logoUrl = 'https://picsum.photos/200/200'; // 替换为实际logo
// 表单引用
const formRef = ref<InstanceType<typeof NForm> | null>(null);
const formData = reactive({
  username: '',
  password: '',
  rememberMe: false
});
const formRules = {
  username: [{required: true, message: '请输入用户名', trigger: ['blur', 'input']}],
  password: [
    {required: true, message: '请输入密码', trigger: ['blur', 'input']}
  ]
};
const inputFocused = ref<{ [key: string]: boolean }>({
  username: false,
  password: false
});
// 动态计算输入框样式
const inputClass = computed(() => {
  return {
    'transition-all duration-300': true,
    'shadow-md': Object.values(inputFocused.value).some(v => v),
    'bg-input-dark/50 border-input-dark/30': isDarkMode.value,
    'bg-white/50': !isDarkMode.value
  };
});
const {send, loading} = useForm(() => authApi.login(formData), {immediate: false})
const handleLogin = () => {
  formRef.value?.validate(error => {
    if (!error) {
      send().then(res => {
        const {accessToken, refreshToken} = res
        setToken(accessToken, refreshToken)
        authApi.getUserInfo().then(res => {
          userStore.setInfo(res)
          appStore.setCurrentOrgId(res.lastOrganizationId || '');
          appStore.setCurrentProjectId(res.lastProjectId || '');
        })
        window.$message.success('登录成功');
        const {redirect, ...othersQuery} = router.currentRoute.value.query;
        router.push({
          name: redirect as string || 'home',
          query: {
            ...othersQuery,
            orgId: appStore.currentOrgId,
            pId: appStore.currentProjectId,
          }
        })
      })
    }
  })
}
// 页面挂载时初始化
onMounted(() => {
  addGlobalStyles();
  initParticles(isDarkMode.value);

  // 检查深色模式偏好
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark');
  }
});
</script>

<template>
  <div class="login-page relative min-h-screen flex items-center justify-center overflow-hidden">
    <!-- 粒子背景 -->
    <div id="particles-js" class="absolute inset-0 z-0"/>
    <!-- 背景渐变遮罩 -->
    <div class="absolute inset-0 bg-gradient-to-br from-primary/10 to-primary/5 z-10"/>
    <!-- 登录卡片 -->
    <n-card
        class="login-card relative z-20 w-full max-w-md overflow-hidden transition-all duration-500 hover:shadow-2xl"
        :class="isDarkMode ? 'bg-card-dark/80 border-none' : 'bg-card/80'">
      <!-- 卡片顶部装饰 -->
      <div class="absolute top-0 left-0 right-0 h-1.5 bg-gradient-to-r from-primary to-primary-light"/>
      <!-- 登录标题区域 -->
      <div class="text-center mb-8 pt-6">
        <n-avatar
            class="w-16 h-16 mx-auto mb-2 border-4 border-white/20 shadow-lg"
            :src="logoUrl"
            alt="系统logo"
        />
        <h1 class="text-2xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-primary to-primary-light mb-1">
          登录系统
        </h1>
        <p class="text-text-200 text-sm" :class="isDarkMode ? 'text-text-300-dark' : ''">
          请输入您的账号信息以继续
        </p>
      </div>
      <n-form ref="formRef" :model="formData" :rules="formRules">
        <n-form-item path="username" label="用户名">
          <n-input
              v-model:value="formData.username"
              placeholder="请输入用户名"
              :class="inputClass"
              :bordered="!isDarkMode"
          />
        </n-form-item>
        <n-form-item path="password" label="密码">
          <n-input
              v-model:value="formData.password"
              type="password"
              placeholder="请输入密码"
              :class="inputClass"
              :bordered="!isDarkMode"

          />
        </n-form-item>
        <!-- 登录按钮 -->
        <n-button
            type="primary"
            class="w-full h-11 text-base bg-gradient-to-r from-primary to-primary-light hover:from-primary-light hover:to-primary transition-all duration-300 shadow-md hover:shadow-lg"
            :loading="loading"
            @click="handleLogin"
        >
          登 录
        </n-button>
      </n-form>
    </n-card>
  </div>
</template>

<style scoped>
.login-page {
  --primary: #6366f1; /* 主色调：靛蓝色 */
  --primary-light: #8b5cf6; /* 亮一点的主色调：紫色 */
  --text-200: #64748b; /* 次要文本颜色 */
  --text-300-dark: #cbd5e1; /* 深色模式下的次要文本 */
  --card: #ffffff; /* 卡片背景色 */
  --card-dark: #1e293b; /* 深色模式卡片背景 */
  --input-dark: #334155; /* 深色模式输入框 */
}

/* 响应式调整 */
@media (max-width: 640px) {
  .login-card {
    margin: 0 1rem;
  }
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: var(--primary);
  border-radius: 3px;
  opacity: 0.5;
}

::placeholder {
  color: var(--text-200);
  opacity: 0.7;
}
</style>