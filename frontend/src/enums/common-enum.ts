export const AuthScopeEnum = {
    SYSTEM: 'SYSTEM',
    ORGANIZATION: 'ORGANIZATION',
    PROJECT: 'PROJECT',
} as const
export type AuthScopeEnumType = typeof AuthScopeEnum[keyof typeof AuthScopeEnum];
export const TemplateCardEnum = {
    FUNCTIONAL: 'caseTemplate', // 用例模板
    API: 'apiTemplate', // API模板
    UI: 'uiTemplate', // UI模板
    TEST_PLAN: 'testPlanTemplate', // 测试计划模板
    BUG: 'defectTemplate', // 缺陷模板
    SCHEDULE: 'scheduleTemplate', // 缺陷模板
} as const
export type TemplateCardEnumType = typeof TemplateCardEnum[keyof typeof TemplateCardEnum];