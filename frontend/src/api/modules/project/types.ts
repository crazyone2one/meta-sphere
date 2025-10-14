// 项目列表项
export interface IProjectListItem {
    id: string;
    num: number;
    organizationId: string;
    name: string;
    description: string;
    createTime: number;
    updateTime: number;
    updateUser: string;
    createUser: string;
    deleteTime: number;
    deleted: boolean;
    deleteUser: string;
    enable: boolean;
    moduleSetting: string;
}
