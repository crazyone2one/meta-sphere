// 字段类型定义
export type FieldType = 'text' | 'number' | 'select' | 'checkbox' | 'radio' | 'textarea' | 'custom' | 'date';

export type Option = {
    label: string;
    value: string;
}

// 表单字段定义
export type FormField = {
    key: string;
    label: string;
    type?: FieldType;
    options?: Option[];
    required?: boolean;
    disabled?: boolean;
}
// 表单数据类型（动态key）
export type FormData = Record<string, any>;
export const FormFieldOptions: Record<string, FormField> = {
    ycFlag: {
        key: 'ycFlag',
        label: '是否为异常文件',
        type: 'radio',
        options: [{label: '是', value: 'true'}, {label: '否', value: 'false'}]
    },
    tuningFlag: {
        key: 'tuningFlag',
        label: '是否为标校文件',
        type: 'radio',
        options: [{label: '是', value: 'true'}, {label: '否', value: 'false'}]
    },
    range: {
        key: 'range',
        label: '异常开始/结束时间',
        type: 'date',
    },
    customConfig: {
        key: 'customConfig',
        label: '自定义参数',
    }
}