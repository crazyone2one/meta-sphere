import {get, post} from "/@/api";
import type {IPageResponse, ITableQueryParams} from "/@/api/types.ts";
import type {ICreateTask, IScheduleInfo} from "/@/api/modules/schedule/types.ts";
import type {SelectOption} from "naive-ui";

export const scheduleApi = {
    getScheduleList: (data: ITableQueryParams) => post<IPageResponse<IScheduleInfo>>("/system-schedule/page", data),
    saveSchedule: (data: ICreateTask) => post("/system-schedule/save", data),
    getScheduleNameList: () => get<Array<SelectOption>>("/system-schedule/schedule-name-list"),
}