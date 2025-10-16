import {createAlova} from 'alova';
import adapterFetch from 'alova/fetch';
import VueHook from 'alova/vue';
import {clearToken, getToken, setToken} from "/@/utils/auth.ts";
import useAppStore from "/@/store/modules/app";
import {createServerTokenAuthentication} from "alova/client";
import {authApi} from "/@/api/modules/auth/index.ts";
import router from "/@/router";
import type {LocationQueryRaw} from "vue-router";

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
        isExpired: async (response, method) => {
            const res = await response.clone().json()
            const isExpired = method.meta && method.meta.isExpired;
            return (response.status === 401 || res.code === 100401) && !isExpired;
        },
        handler: async (_response, method) => {
            method.meta = method.meta || {};
            method.meta.isExpired = true;
            const {accessToken, refreshToken} = await authApi.refreshToken({"refreshToken": getToken().refreshToken});
            setToken(accessToken, refreshToken)
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

// 提取消息显示逻辑到独立函数
const showResultMessage = (code: number, message: string) => {
    if (code === 100411) {
        window.$dialog.error({
            title: '登录已过期，请重新登录',
            closable: false,
            maskClosable: false,
            positiveText: 'ok',
            onPositiveClick: async () => {
                // 清除token并跳转到登录页
                clearToken();
                // location.href = '/login';
                await router.replace({
                    name: 'login',
                    query: {
                        redirect: router.currentRoute.value.name,
                        ...router.currentRoute.value.query,
                    } as LocationQueryRaw,
                });
            }
        })
    }
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
            const {status} = response
            const appStore = useAppStore();
            appStore.hideLoading()
            if (status === 200) {
                // 返回blob数据
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
                if (data.code === 100200) {
                    // 成功响应，返回数据部分
                    return data.data !== undefined ? data.data : data;
                } else {
                    showResultMessage(data.code, data.message);
                }
            } else {
                // HTTP 错误处理
                const data = await response.json();
                showResultMessage(data.code, data.message);
            }
        },
        // 错误响应处理
        onError: (error, method) => {
            const appStore = useAppStore();
            appStore.hideLoading()
            const tip = `[${method.type}] - [${method.url}] - ${error.message}`
            window.$message?.warning(tip)
        }
    }),
});