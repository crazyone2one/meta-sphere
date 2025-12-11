export const getClassSimpleName = (fullClassName: string) => {
    if (!fullClassName) return '';
    // 使用split分割字符串并获取最后一个元素
    return fullClassName.split('.').pop() || '';
}
let lastTimestamp = 0;
let sequence = 0;
export const getGenerateId = () => {
    let timestamp = new Date().getTime();
    if (timestamp === lastTimestamp) {
        sequence++;
        if (sequence >= 100000) {
            // 如果超过999，则重置为0，等待下一秒
            sequence = 0;
            while (timestamp <= lastTimestamp) {
                timestamp = new Date().getTime();
            }
        }
    } else {
        sequence = 0;
    }

    lastTimestamp = timestamp;

    return timestamp.toString() + sequence.toString().padStart(5, '0');
};