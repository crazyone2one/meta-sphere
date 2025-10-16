export const getClassSimpleName = (fullClassName: string) => {
    if (!fullClassName) return '';
    // 使用split分割字符串并获取最后一个元素
    return fullClassName.split('.').pop() || '';
}