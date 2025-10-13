import {instance} from "./request";


export const get =<T> (url: string, params = {}, options = {}) => {
    return instance.Get<T>(url, {
        params,
        ...options
    });
}
// 通用 POST 请求
export const post = <T>(url: string, data = {}, options = {}) => {
    return instance.Post<T>(url, data, {
        ...options
    });
};

// 通用 PUT 请求
export const put = (url: string, data = {}, options = {}) => {
    return instance.Put(url, data, {
        ...options
    });
};

// 通用 DELETE 请求
export const del = (url: string, options = {}) => {
    return instance.Delete(url, {
        ...options
    });
};

// 通用 PATCH 请求
export const patch = (url: string, data = {}, options = {}) => {
    return instance.Patch(url, data, {
        ...options
    });
};