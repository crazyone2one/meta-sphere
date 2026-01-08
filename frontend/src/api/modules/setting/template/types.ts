import type {FormItemType, FormRuleItem} from "/@/api/types.ts";
import type {LocationQueryValue} from "vue-router";

export type SceneType =
    'FUNCTIONAL'
    | 'BUG'
    | 'API'
    | 'SCHEDULE'
    | 'TEST_PLAN'
    | LocationQueryValue[]
    | LocationQueryValue;

export interface FieldOptions {
    id?: string;
    fieldId?: string;
    value: any;
    text: string;
    internal?: boolean; // 是否是内置模板
    pos?: number; // 排序字段
}// 新增 || 编辑参数
export interface AddOrUpdateField {
    id?: string;
    name: string;
    used: boolean;
    scene: SceneType; // 使用场景
    type: FormItemType;
    remark: string; // 备注
    scopeId: string; // 组织或项目ID
    options?: FieldOptions[];
    enableOptionKey: boolean;

    [key: string]: any;
}

// 自定义字段
export interface DefinedFieldItem {
    id: string;
    name: string;
    scene: SceneType; // 使用场景
    type: FormItemType; // 表单类型
    remark: string;
    internal: boolean; // 是否是内置字段
    scopeType: string; // 组织或项目级别字段（PROJECT, ORGANIZATION）
    createTime: number;
    updateTime: number;
    createUser: string;
    refId: string | null; // 项目字段所关联的组织字段ID
    enableOptionKey: boolean | null; // 是否需要手动输入选项key
    scopeId: string; // 组织或项目ID
    options: FieldOptions[] | null;
    required?: boolean | undefined;
    fApi?: any; // 表单值
    formRules?: FormRuleItem; // 表单列表
    [key: string]: any;
}

export interface fieldIconAndNameModal {
    value: string;
    iconName: string; // 图标名称
    label: string; // 对应标签
}

export interface CustomField {
    fieldId: string;
    required?: boolean; // 是否必填
    apiFieldId?: string; // api字段名
    defaultValue: string | (string | number)[] | number; // 默认值
    [key: string]: any;
}

export interface ActionTemplateManage {
    id?: string;
    name: string;
    remark: string;
    scopeId: string;
    enableThirdPart?: boolean; // 是否开启api字段名配置
    scene?: SceneType;
    customFields?: CustomField[];
    fieldType?: string;
    systemFields?: Record<string, any>[];
    enablePlatformDefault?: boolean;
    internal?: boolean; // 是否为系统模板
    platForm?: string;
    uploadImgFileIds: string[]; // 模板附件
    [key: string]: any;
}