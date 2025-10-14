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
}

export interface IRunConfig {
    runMode: 'SERIAL' | 'PARALLEL'

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
    runConfig?: IRunConfig;
}