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