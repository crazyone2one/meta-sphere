import {createAlova} from 'alova';
import adapterFetch from 'alova/fetch';
import VueHook from 'alova/vue';
import {clearToken, getToken, setToken} from "/@/utils/auth.ts";
import useAppStore from "/@/store/modules/app";
import {createServerTokenAuthentication} from "alova/client";
import {authApi} from "/@/api/modules/auth/index.ts";
import router from "/@/router";

const RESULT_CODE_MAP: Record<number, { type: 'success' | 'error' | 'warning' | 'info', message: string }> = {
    100200: {type: 'success', message: '操作成功'},
    100500: {type: 'error', message: '操作失败'},
    100400: {type: 'warning', message: '参数校验失败'},
    100401: {type: 'error', message: '用户认证失败'},
    100403: {type: 'error', message: '权限认证失败'},
    100404: {type: 'warning', message: '资源不存在'},
    100405: {type: 'warning', message: '请求方法不允许'},
    100408: {type: 'warning', message: '请求超时'},
    100409: {type: 'warning', message: '资源冲突'},
    100415: {type: 'warning', message: '不支持的媒体类型'},
};
const {onAuthRequired, onResponseRefreshToken} = createServerTokenAuthentication({
    refreshTokenOnSuccess: {
        isExpired: (response, method) => {
            const isExpired = method.meta && method.meta.isExpired;
            return response.status === 401 && !isExpired;
        },
        handler: async (_response, method) => {
            method.meta = method.meta || {};
            method.meta.isExpired = true;
            try {
                const {
                    accessToken,
                    refreshToken
                } = await authApi.refreshToken({"refreshToken": getToken().refreshToken});
                setToken(accessToken, refreshToken)
            } catch (error: any) {
                // 检查是否是refresh token过期的特定错误
                if (error.message === 'REFRESH_TOKEN_EXPIRED') {
                    // 显示明确的过期提示
                    window.$message?.error('登录已过期，请重新登录');
                }
                // 清除token并跳转到登录页
                clearToken();
                // location.href = '/login';
                await router.push('/login')
                // 并抛出错误
                throw error;
            }
        },
    },
    logout: () => {
        // 登出处理
        clearToken()
    },
    assignToken: method => {
        const token = getToken();
        if (token && (!method.meta?.authRole || method.meta?.authRole !== 'refreshToken')) {
            method.config.headers.Authorization = `Bearer ${token.accessToken}`;
        }
    }
});

const handleError = (method: any, data: any) => {
    if (data.code !== 100200 && data.message && !method.meta?.ignoreMessage) {
        showResultMessage(data.code, data.message);
    }
};
// 提取消息显示逻辑到独立函数
const showResultMessage = (code: number, message: string) => {
    if (!window.$message) return;
    const codeInfo = RESULT_CODE_MAP[code];
    if (codeInfo) {
        window.$message[codeInfo.type](codeInfo.message);
    } else {
        // 默认使用 error 类型提示
        window.$message.error(message);
    }
};
export const instance = createAlova({
    baseURL: `${window.location.origin}/${import.meta.env.VITE_API_BASE_URL}`,
    statesHook: VueHook,
    requestAdapter: adapterFetch(),
    timeout: 300 * 1000,
    beforeRequest: onAuthRequired((method) => {
        const appStore = useAppStore();
        method.config.headers = {
            ...method.config.headers,
            'ORGANIZATION': appStore.currentOrgId,
            'PROJECT': appStore.currentProjectId,
        };
        appStore.showLoading()
    }),
    responded: onResponseRefreshToken({
        // 成功响应处理
        onSuccess: async (response, method) => {
            const appStore = useAppStore();
            appStore.hideLoading()
            if (method.meta?.isBlob) {
                return response.blob();
            }
            // 解析 JSON 数据
            let data: any;
            try {
                data = await response.json();
            } catch (parseError) {
                // JSON 解析失败处理
                throw new Error('服务器响应格式错误');
            }

            // 检查 HTTP 状态码
            if (response.status < 200 || response.status >= 300) {
                // HTTP 错误处理
                handleError(method, data);
                throw new Error(`HTTP Error: ${response.status}`);
            }

            // 根据后端返回的状态码显示对应提示
            if (data.code !== 100200 && data.message && !method.meta?.ignoreMessage) {
                showResultMessage(data.code, data.message);
            }

            if (data.code === 100200) {
                // 成功响应，返回数据部分
                return data.data !== undefined ? data.data : data;
            } else {
                // 业务错误处理
                throw new Error(data.message || '请求失败');
            }
        },
        // 错误响应处理
        onError: (error, method) => {
            const appStore = useAppStore();
            appStore.hideLoading()
            // 显示错误提示
            if (!method.meta?.ignoreMessage && window.$message) {
                window.$message.error(error.message || '网络请求失败');
            }
            console.error('API Error:', error);
            throw new Error(error.message || '网络请求失败');
        }
    }),
});