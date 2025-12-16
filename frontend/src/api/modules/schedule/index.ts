import {del, get, post, put} from "/@/api";
import type {IPageResponse, ITableQueryParams} from "/@/api/types.ts";
import type {ICreateTask, IScheduleConfig, IScheduleInfo, IUpdateTask} from "/@/api/modules/schedule/types.ts";
import type {SelectOption} from "naive-ui";

export const scheduleApi = {
    getScheduleList: (data: ITableQueryParams) => post<IPageResponse<IScheduleInfo>>("/system-schedule/page", data),
    saveSchedule: (data: ICreateTask) => post("/system-schedule/save", data),
    updateSchedule: (data: IUpdateTask) => put("/system-schedule/update", data),
    updateScheduleCron: (data: { id: string, cron: string }) => post("/system-schedule/schedule/update-cron", data),
    // 配置定时任务
    runScheduleConfig: (data: IScheduleConfig) => post("/system-schedule/schedule-config", data),
    // 获取后台已创建的任务名称
    getScheduleNameList: (projectId: string) => get<Array<SelectOption>>(`/system-schedule/schedule-name-list/${projectId}`),
    // 获取传感器列表
    getSensorList: (data: {
        projectId: string,
        sensorType?: string
        sensorGroup?: string
    }) => post<Array<SelectOption>>(`/system-schedule/sensor/option`, data),
    // 删除任务
    removeSchedule: (id: string) => del(`/system-schedule/remove/${id}`),
    resumeScheduleTask: (id: string) => get(`/system-schedule/resume/schedule/task/${id}`),
    executeScheduleTask: (id: string) => get(`/system-schedule/execute/schedule/task/${id}`),
    pauseScheduleTask: (id: string) => get(`/system-schedule/pause/schedule/task/${id}`),
    // 修改任务状态
    changeScheduleStatus: (id: string) => get(`/system-schedule/status/switch/${id}`),
}