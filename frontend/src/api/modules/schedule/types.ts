export interface IScheduleInfo {
    id: string;
    name: string;
    resourceId: string;
    projectName: string;
    num: number;
    enable: boolean;
    value: string;
    lastTime?: number;
    nextTime?: number;
    createUser?: string
    runConfig?: IScheduleConfig;
}

export interface IScheduleConfig {
    resourceId: string
    cron: string
    enable: boolean
    runConfig: IRunConfig
}

export interface IRunConfig {
    [key: string]: any;
}

export interface ICreateTask {
    name: string;
    resourceId: string;
    enable: boolean;
    value: string;
    key: string;
    projectId: string;
    job: string;
    type: string
    runConfig?: IScheduleConfig;
}
export interface IUpdateTask {
    value: string;
    id: string;
}