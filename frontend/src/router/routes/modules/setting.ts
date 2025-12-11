import type {RouteRecordRaw} from "vue-router";
import {SettingRouteEnum} from "../../../enums/common-enum.ts";

const Setting: RouteRecordRaw = {
    path: '/setting',
    name: SettingRouteEnum.SETTING,
    component: null,
    meta: {
        title: '系统设置', icon: 'i-ant-design:setting-outlined', order: 8,
        roles: [
            'SYSTEM_USER:READ',
            'SYSTEM_USER_ROLE:READ',
            'SYSTEM_ORGANIZATION_PROJECT:READ',
            'SYSTEM_PARAMETER_SETTING_BASE:READ',
            'SYSTEM_PARAMETER_SETTING_DISPLAY:READ',
            'SYSTEM_PARAMETER_SETTING_AUTH:READ',
            'SYSTEM_PARAMETER_SETTING_MEMORY_CLEAN:READ',
            'SYSTEM_PARAMETER_SETTING_QRCODE:READ',
            'SYSTEM_TEST_RESOURCE_POOL:READ',
            'SYSTEM_AUTH:READ',
            'SYSTEM_PLUGIN:READ',
            'SYSTEM_LOG:READ',
            'SYSTEM_CASE_TASK_CENTER:READ',
            'SYSTEM_SCHEDULE_TASK_CENTER:READ',
            'ORGANIZATION_MEMBER:READ',
            'ORGANIZATION_USER_ROLE:READ',
            'ORGANIZATION_PROJECT:READ',
            'SYSTEM_SERVICE_INTEGRATION:READ',
            'ORGANIZATION_TEMPLATE:READ',
            'ORGANIZATION_LOG:READ',
            'ORGANIZATION_CASE_TASK_CENTER:READ',
            'ORGANIZATION_SCHEDULE_TASK_CENTER:READ',
        ],
    },
    children: [
        {
            path: 'system',
            name: SettingRouteEnum.SETTING_SYSTEM,
            component: null,
            meta: {
                title: '系统',
                roles: [
                    'SYSTEM_USER:READ',
                    'SYSTEM_USER_ROLE:READ',
                    'SYSTEM_ORGANIZATION_PROJECT:READ',
                    'SYSTEM_PARAMETER_SETTING_BASE:READ',
                    'SYSTEM_PARAMETER_SETTING_DISPLAY:READ',
                    'SYSTEM_PARAMETER_SETTING_AUTH:READ',
                    'SYSTEM_PARAMETER_SETTING_MEMORY_CLEAN:READ',
                    'SYSTEM_PARAMETER_SETTING_QRCODE:READ',
                    'SYSTEM_TEST_RESOURCE_POOL:READ',
                    'SYSTEM_AUTH:READ',
                    'SYSTEM_PLUGIN:READ',
                    'SYSTEM_LOG:READ',
                    'SYSTEM_CASE_TASK_CENTER:READ',
                    'SYSTEM_SCHEDULE_TASK_CENTER:READ',
                ],
                hideChildrenInMenu: true,
            },
            children: [
                {
                    path: 'user', name: SettingRouteEnum.SETTING_SYSTEM_USER_SINGLE,
                    component: () => import('/@/views/setting/user/index.vue'),
                    meta: {
                        title: '用户', roles: ['SYSTEM_USER_ROLE:READ'], isTopMenu: true,
                    },
                },
                {
                    path: 'user-group', name: SettingRouteEnum.SETTING_SYSTEM_USER_GROUP,
                    component: () => import('/@/views/setting/user-group/index.vue'),
                    meta: {
                        title: '用户组', roles: ['SYSTEM_USER_ROLE:READ'], isTopMenu: true,
                    },
                },
                {
                    path: 'organization-and-project', name: SettingRouteEnum.SETTING_SYSTEM_ORGANIZATION,
                    component: () => import('/@/views/setting/org-project/index.vue'),
                    meta: {
                        title: '组织与项目', roles: ['SYSTEM_USER_ROLE:READ'], isTopMenu: true,
                    },
                },
                {
                    path: 'taskCenter', name: SettingRouteEnum.SETTING_SYSTEM_TASK_CENTER,
                    component: () => import('/@/views/schedule/index.vue'),
                    meta: {
                        title: '任务中心',
                        roles: ['SYSTEM_CASE_TASK_CENTER:READ', 'SYSTEM_SCHEDULE_TASK_CENTER:READ'],
                        isTopMenu: true,
                    },
                },
            ]
        },
        {
            path: 'organization',
            name: SettingRouteEnum.SETTING_ORGANIZATION,
            redirect: '/setting/organization/member',
            component: null,
            meta: {
                title: '组织',
                roles: [
                    'ORGANIZATION_MEMBER:READ',
                    'ORGANIZATION_USER_ROLE:READ',
                    'ORGANIZATION_PROJECT:READ',
                    'SYSTEM_SERVICE_INTEGRATION:READ',
                    'ORGANIZATION_TEMPLATE:READ',
                    'ORGANIZATION_LOG:READ',
                    'ORGANIZATION_CASE_TASK_CENTER:READ',
                    'ORGANIZATION_SCHEDULE_TASK_CENTER:READ',
                ],
                hideChildrenInMenu: true,
            },
            children:[
                {
                    path: 'member',
                    name: SettingRouteEnum.SETTING_ORGANIZATION_MEMBER,
                    component: () => import('/@/views/setting/organization/member/index.vue'),
                    meta: {
                        title: '成员',
                        roles: ['ORGANIZATION_MEMBER:READ'],
                        isTopMenu: true,
                    },
                },
                {
                    path: 'template',
                    name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE,
                    component: () => import('/@/views/setting/organization/template/index.vue'),
                    meta: {
                        title: '模板',
                        roles: ['ORGANIZATION_TEMPLATE:READ'],
                        isTopMenu: true,
                    },
                },
                {
                    path: 'templateFiledSetting',
                    name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_FILED_SETTING,
                    component: () => import('/@/views/setting/organization/template/components/OrgFieldSetting.vue'),
                    meta: {
                        locale: '字段设置',
                        roles: ['ORGANIZATION_TEMPLATE:READ'],
                        breadcrumbs: [
                            {
                                name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE,
                                locale: '模板',
                            },
                            {
                                name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_FILED_SETTING,
                                locale: '字段设置',
                                editLocale: '字段设置',
                                query: ['type'],
                            },
                        ],
                    },
                },
            ]
        }
    ]
}
export default Setting;