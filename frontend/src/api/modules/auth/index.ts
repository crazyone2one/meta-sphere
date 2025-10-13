import {get, post} from "/@/api";
import type {AuthenticationResponse, UserState} from "/@/api/modules/auth/types.ts";

export const authApi = {
    login: (data: { username: string, password: string }) => {
        const method = post<AuthenticationResponse>('/auth/login', data)
        method.meta = {authRole: null,};
        return method;
    },
    refreshToken: (data: { refreshToken: string }) => {
        const method = post<AuthenticationResponse>('/auth/refreshToken', data,);
        method.meta = {authRole: 'refreshToken'};
        return method
    },
    logout: () => {
        const method = post('/auth/logout')
        method.meta = {authRole: 'logout'};
        return method;
    },
    getUserInfo: () => get<UserState>('/system-user/get-user-info'),
}