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

export interface ScrollToViewOptions {
    behavior?: 'auto' | 'smooth';
    block?: 'start' | 'center' | 'end' | 'nearest';
    inline?: 'start' | 'center' | 'end' | 'nearest';
}

export const scrollIntoView = (targetRef: HTMLElement | Element | null, options: ScrollToViewOptions = {}) => {
    const scrollOptions: ScrollToViewOptions = {
        behavior: options.behavior || 'smooth',
        block: options.block || 'start',
        inline: options.inline || 'nearest',
    };

    targetRef?.scrollIntoView(scrollOptions);
}