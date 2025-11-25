export interface ITableQueryParams {
    // 当前页
    page?: number;
    // 每页条数
    pageSize?: number;
    // 排序仅针对单个字段
    sort?: object;
    // 排序仅针对单个字段
    sortString?: string;
    // 表头筛选
    filter?: object;
    // 查询条件
    keyword?: string;

    [key: string]: string | number | object | undefined;
}

export interface IPageResponse<T> {
    [x: string]: any;

    pageSize: number;
    totalPage: number;
    pageNumber: number;
    totalRow: number;
    records: T[];
}

export interface IResponse<T> {
    code: number;
    message: string;
    messageDetail: string;
    data: T;
}

export interface BatchActionQueryParams {
    excludeIds?: string[]; // 排除的id
    selectedIds?: string[];
    selectAll: boolean; // 是否跨页全选
    params?: ITableQueryParams; // 查询参数
    currentSelectCount?: number; // 当前选中的数量
    condition?: any; // 查询条件
    [key: string]: any;
}

export type FormItemType =
    | 'INPUT'
    | 'TEXTAREA'
    | 'SELECT'
    | 'MULTIPLE_SELECT'
    | 'RADIO'
    | 'CHECKBOX'
    | 'MEMBER'
    | 'MULTIPLE_MEMBER'
    | 'DATE'
    | 'DATETIME'
    | 'INT'
    | 'FLOAT'
    | 'MULTIPLE_INPUT'
    | 'NUMBER'
    | 'PassWord'
    | 'CASCADER'
    | undefined;


export interface iLoadOptionParams {
    keyword?: string;
    roleId?: string;
    organizationId?: string;
}