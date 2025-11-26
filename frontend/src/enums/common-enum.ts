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
export const ProjectManagementRouteEnum = {
    PROJECT_MANAGEMENT: 'projectManagement',
    PROJECT_MANAGEMENT_FILE_MANAGEMENT: 'projectManagementFileManageMent',
    PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT: 'projectManagementMessageManagement',
    PROJECT_MANAGEMENT_COMMON_SCRIPT: 'projectManagementCommonScript',
    PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT_EDIT: 'projectManagementMessageManagementEdit',
    PROJECT_MANAGEMENT_TASK_CENTER: 'projectManagementTaskCenter',
    PROJECT_MANAGEMENT_LOG: 'projectManagementLog',
    PROJECT_MANAGEMENT_PERMISSION: 'projectManagementPermission',
    PROJECT_MANAGEMENT_PERMISSION_BASIC_INFO: 'projectManagementPermissionBasicInfo',
    PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT: 'projectManagementPermissionMenuManagement',
    PROJECT_MANAGEMENT_TEMPLATE: 'projectManagementTemplate',
    PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT: 'projectManagementTemplateList',
    PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_CASE_DETAIL: 'projectManagementTemplateManagementCaseDetail',
    PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_API_DETAIL: 'projectManagementTemplateManagementApiDetail',
    PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_BUG_DETAIL: 'projectManagementTemplateManagementBugDetail',
    PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_WORKFLOW: 'projectManagementTemplateManagementWorkFlow',
    PROJECT_MANAGEMENT_TEMPLATE_FIELD_SETTING: 'projectManagementTemplateFiledSetting',
    PROJECT_MANAGEMENT_PERMISSION_VERSION: 'projectManagementPermissionVersion',
    PROJECT_MANAGEMENT_PERMISSION_USER_GROUP: 'projectManagementPermissionUserGroup',
    PROJECT_MANAGEMENT_PERMISSION_MEMBER: 'projectManagementPermissionMember',
    PROJECT_MANAGEMENT_MENU_MANAGEMENT_ERROR_REPORT_RULE: 'projectManagementMenuManagementErrorReportRule',
    PROJECT_MANAGEMENT_ENVIRONMENT_MANAGEMENT: 'projectManagementEnvironmentManagement',
} as const
export const SettingRouteEnum = {
    SETTING: 'setting',
    SETTING_SYSTEM: 'settingSystem',
    SETTING_SYSTEM_USER_SINGLE: 'settingSystemUser',
    SETTING_SYSTEM_USER_GROUP: 'settingSystemUserGroup',
    SETTING_SYSTEM_ORGANIZATION: 'settingSystemOrganization',
    SETTING_SYSTEM_PARAMETER: 'settingSystemParameter',
    SETTING_SYSTEM_RESOURCE_POOL: 'settingSystemResourcePool',
    SETTING_SYSTEM_RESOURCE_POOL_DETAIL: 'settingSystemResourcePoolDetail',
    SETTING_SYSTEM_AUTHORIZED_MANAGEMENT: 'settingSystemAuthorizedManagement',
    SETTING_SYSTEM_LOG: 'settingSystemLog',
    SETTING_SYSTEM_TASK_CENTER: 'settingSystemTaskCenter',
    SETTING_SYSTEM_PLUGIN_MANAGEMENT: 'settingSystemPluginManagement',
    SETTING_ORGANIZATION: 'settingOrganization',
    SETTING_ORGANIZATION_MEMBER: 'settingOrganizationMember',
    SETTING_ORGANIZATION_USER_GROUP: 'settingOrganizationUserGroup',
    SETTING_ORGANIZATION_PROJECT: 'settingOrganizationProject',
    SETTING_ORGANIZATION_TEMPLATE: 'settingOrganizationTemplate',
    SETTING_ORGANIZATION_TEMPLATE_FILED_SETTING: 'settingOrganizationTemplateFiledSetting',
    SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT: 'settingOrganizationTemplateManagement',
    SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_CASE_DETAIL: 'settingOrganizationTemplateManagementCaseDetail',
    SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_API_DETAIL: 'settingOrganizationTemplateManagementApiDetail',
    SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_BUG_DETAIL: 'settingOrganizationTemplateManagementBugDetail',
    SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_WORKFLOW: 'settingOrganizationTemplateWorkFlow',
    SETTING_ORGANIZATION_SERVICE: 'settingOrganizationService',
    SETTING_ORGANIZATION_LOG: 'settingOrganizationLog',
    SETTING_ORGANIZATION_TASK_CENTER: 'settingOrganizationTaskCenter',
} as const