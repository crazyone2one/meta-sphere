/**
 * 判断是否为服务端渲染
 */
export const isServerRendering = (() => {
    try {
        return !(typeof window !== 'undefined' && document !== undefined);
    } catch (e) {
        return true;
    }
})();