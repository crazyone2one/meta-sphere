import {computed} from "vue";

export const sensorTypeOptions = computed(() => {
    return [
        {label: '一氧化碳', value: '0004'},
        {label: '环境瓦斯', value: '0001'},
        {label: '烟雾', value: '1008'},
        {label: '主通风机', value: '1010'},
        {label: '甲烷', value: '0043'},
        {label: '风筒', value: '1003'},
        {label: '水位', value: '0502'},
    ]
})