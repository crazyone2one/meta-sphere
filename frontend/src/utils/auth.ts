const ACCESS_TOKEN = 'accessToken';
const REFRESH_TOKEN = 'refreshToken';

const getToken = () => {
    return {
        [ACCESS_TOKEN]: localStorage.getItem(ACCESS_TOKEN),
        [REFRESH_TOKEN]: localStorage.getItem(REFRESH_TOKEN) || ''
    };
};

const setToken = (accessToken: string, refreshToken: string) => {
    localStorage.setItem(ACCESS_TOKEN, accessToken);
    localStorage.setItem(REFRESH_TOKEN, refreshToken);
};

const clearToken = () => {
    localStorage.removeItem(ACCESS_TOKEN);
    localStorage.removeItem(REFRESH_TOKEN);
};

const hasToken = () => {
    return !!localStorage.getItem(ACCESS_TOKEN) && !!localStorage.getItem(REFRESH_TOKEN);
};
const setLoginExpires = () => {
    localStorage.setItem('loginExpires', Date.now().toString());
};

const isLoginExpires = () => {
    const lastLoginTime = Number(localStorage.getItem('loginExpires'));
    const now = Date.now();
    const diff = now - lastLoginTime;
    const thirtyDay = 24 * 60 * 60 * 1000 * 30;
    return diff > thirtyDay;
};
export {clearToken, getToken, hasToken, setToken, isLoginExpires, setLoginExpires,};