import type {FormItemRule, SelectGroupOption, SelectOption} from "naive-ui";

export type FormItemType = 'input' | 'select' | 'inputNumber' | 'tagInput' | 'multiple' | 'switch' | 'textarea';
export type FormMode = 'create' | 'edit';

// 自定义检验器，为了传入动态渲染的表单项下标
export interface CustomValidator {
    notRepeat?: boolean;
}

export interface FormItemModel {
    field: string;
    type: FormItemType;
    rules?: (FormItemRule & CustomValidator)[];
    label?: string;
    placeholder?: string;
    min?: number;
    max?: number;
    maxLength?: number;
    hideAsterisk?: boolean;
    hideLabel?: boolean;
    children?: FormItemModel[];
    options?: (SelectOption | SelectGroupOption)[]; // select option 选项
    // filedNames?: SelectFieldNames; // select option 选项字段名
    className?: string; // 自定义样式
    defaultValue?: string | string[] | number | number[] | boolean; // 默认值
    hasRedStar?: boolean; // 是否有红星
    tooltip?: string;
    disabled?: boolean;
    maxKey?: string;
    minKey?: string;
    getPrecisionFun?: (model: FormItemModel, ele: FormItemModel) => number;

    [key: string]: any;
}