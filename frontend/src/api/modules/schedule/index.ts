import {get, post, put} from "/@/api";
import type {IPageResponse, ITableQueryParams} from "/@/api/types.ts";
import type {ICreateTask, IScheduleConfig, IScheduleInfo, IUpdateTask} from "/@/api/modules/schedule/types.ts";
import type {SelectOption} from "naive-ui";

export const scheduleApi = {
    getScheduleList: (data: ITableQueryParams) => post<IPageResponse<IScheduleInfo>>("/system-schedule/page", data),
    saveSchedule: (data: ICreateTask) => post("/system-schedule/save", data),
    updateSchedule: (data: IUpdateTask) => put("/system-schedule/update", data),
    updateScheduleCron: (data: { id: string, cron: string}) => post("/system-schedule/schedule/update-cron", data),
    // 配置定时任务
    runScheduleConfig: (data: IScheduleConfig) => post("/system-schedule/schedule-config", data),
    // 获取后台已创建的任务名称
    getScheduleNameList: (projectId: string) => get<Array<SelectOption>>(`/system-schedule/schedule-name-list/${projectId}`),
    // 修改任务状态
    changeScheduleStatus: (id: string) => get(`/system-schedule/status/switch/${id}`),
}