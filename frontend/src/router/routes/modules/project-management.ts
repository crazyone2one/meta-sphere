import type {RouteRecordRaw} from "vue-router";
import {ProjectManagementRouteEnum} from "../../../enums/common-enum.ts";

const ProjectManagement: RouteRecordRaw = {
    path: '/project-management',
    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT,
    redirect: '/project-management/permission',
    component: null,
    meta: {
        title: '项目管理', order: 2, hideChildrenInMenu: true,
        icon: 'i-mdi:folder-cog',
        roles: [
            'PROJECT_BASE_INFO:READ',
            'PROJECT_TEMPLATE:READ',
            'PROJECT_FILE_MANAGEMENT:READ',
            'PROJECT_MESSAGE:READ',
            'PROJECT_CUSTOM_FUNCTION:READ',
            'PROJECT_LOG:READ',
            'PROJECT_ENVIRONMENT:READ',
            // 菜单管理
            'PROJECT_APPLICATION_WORKSTATION:READ',
            'PROJECT_APPLICATION_TEST_PLAN:READ',
            'PROJECT_APPLICATION_BUG:READ',
            'PROJECT_APPLICATION_CASE:READ',
            'PROJECT_APPLICATION_API:READ',
            'PROJECT_APPLICATION_UI:READ',
            'PROJECT_APPLICATION_PERFORMANCE_TEST:READ',
            // 菜单管理
            'PROJECT_USER:READ',
            'PROJECT_GROUP:READ',
        ],
    },
    children: [
        {
            path: 'taskCenter', name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TASK_CENTER,
            component: () => import('/@/views/schedule/index.vue'),
            meta: {
                title: '任务中心',
                roles: ['PROJECT_MANAGEMENT_TASK_CENTER:READ'],
                isTopMenu: true,
            },
        },
        {
            path: 'permission',
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION,
            component: () => import('/@/views/project-management/project-permission/index.vue'),
            redirect: '/project-management/permission/basicInfo',
            meta: {
                title: '项目与权限', isTopMenu: true,
                roles: [
                    'PROJECT_BASE_INFO:READ',
                    // 菜单管理
                    'PROJECT_APPLICATION_WORKSTATION:READ',
                    'PROJECT_APPLICATION_TEST_PLAN:READ',
                    'PROJECT_APPLICATION_BUG:READ',
                    'PROJECT_APPLICATION_CASE:READ',
                    'PROJECT_APPLICATION_API:READ',
                    'PROJECT_APPLICATION_UI:READ',
                    'PROJECT_APPLICATION_PERFORMANCE_TEST:READ',
                    // 菜单管理
                    'PROJECT_USER:READ',
                    'PROJECT_GROUP:READ',
                ],
            },
            children: [
                {
                    path: 'basicInfo',
                    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_BASIC_INFO,
                    component: () => import('/@/views/project-management/project-permission/basic-info/index.vue'),
                    meta: {title: "基本信息", roles: ['PROJECT_BASE_INFO:READ']}
                },
                {
                    path: 'menuManagement',
                    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT,
                    component: () => import('/@/views/project-management/project-permission/menu-management/index.vue'),
                    meta: {
                        title: "应用设置", roles: ['PROJECT_APPLICATION_WORKSTATION:READ',
                            'PROJECT_APPLICATION_TEST_PLAN:READ',
                            'PROJECT_APPLICATION_BUG:READ',
                            'PROJECT_APPLICATION_CASE:READ',
                            'PROJECT_APPLICATION_API:READ',
                            'PROJECT_APPLICATION_UI:READ',
                            'PROJECT_APPLICATION_PERFORMANCE_TEST:READ',]
                    }
                },
                // {
                //     path: 'projectVersion',
                //     name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_VERSION,
                //     component: () => import('/@/views/project-management/project-permission/project-version/index.vue'),
                //     meta: {title: "项目版本", roles: ['PROJECT_VERSION:READ']}
                // },
                {
                    path: 'member',
                    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
                    component: () => import('/@/views/project-management/project-permission/member/index.vue'),
                    meta: {title: "项目成员", roles: ['PROJECT_USER:READ']}
                },
                {
                    path: 'projectUserGroup',
                    name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_USER_GROUP,
                    component: () => import('/@/views/project-management/project-permission/user-group/index.vue'),
                    meta: {title: "用户组", roles: ['PROJECT_GROUP:READ']}
                }
            ]
        },
        {
            path: 'projectManagementTemplate',
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE,
            component: () => import('/@/views/project-management/template/index.vue'),
            meta: {
                title: '模板管理', roles: ['PROJECT_TEMPLATE:READ'], isTopMenu: true,
            }
        },
        {
            path: 'projectManagementTemplateField',
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_FIELD_SETTING,
            component: () => import('/@/views/project-management/template/components/projectFieldSetting.vue'),
            meta: {
                title: '字段设置', roles: ['PROJECT_TEMPLATE:READ'],
            }
        },
        {
            path: 'projectManagementTemplateList',
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT,
            component: () => import('/@/views/project-management/template/components/TemplateManagement.vue'),
            meta: {
                title: '模板列表', roles: ['PROJECT_TEMPLATE:READ'],
            }
        },
        {
            path: 'projectManagementTemplateScheduleDetail/:mode?',
            name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_SCHEDULE_DETAIL,
            component: () => import('/@/views/project-management/template/components/TemplateDetail.vue'),
            meta: {
                title: '创建schedule模板', roles: ['PROJECT_TEMPLATE:READ+UPDATE', 'PROJECT_TEMPLATE:READ+ADD'],
            }
        }
    ]
}
export default ProjectManagement;