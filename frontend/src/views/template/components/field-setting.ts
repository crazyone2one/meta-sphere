import type {fieldIconAndNameModal} from "/@/api/modules/setting/template/types.ts";
import type {FormItemType} from "/@/api/types.ts";
import dayjs from "dayjs";
import {computed, ref} from "vue";
import {TemplateCardEnum} from "/@/enums/common-enum.ts";
import useTemplateStore from "/@/store/modules/setting/template.ts";

const templateStore = useTemplateStore();
export const numberTypeOptions: { label: string; value: FormItemType }[] = [
    {label: '整数', value: 'INT',},
    {label: '保留小数', value: 'FLOAT',},
];
export const dateOptions: { label: string; value: FormItemType }[] = [
    {label: dayjs().format('YYYY-MM-DD'), value: 'DATE',},
    {label: dayjs().format('YYYY-MM-DD HH:mm:ss'), value: 'DATETIME',},
];
export const fieldIconAndName: fieldIconAndNameModal[] = [
    {
        value: 'INPUT',
        iconName: 'i-ant-design:edit-outlined',
        label: '输入框',
    },
    {
        value: 'TEXTAREA',
        iconName: 'i-ant-design:edit-outlined',
        label: '文本',
    },
    {
        value: 'SELECT',
        iconName: 'i-ant-design:bars-outlined',
        label: '单选下拉框',
    },
    {
        value: 'MULTIPLE_SELECT',
        iconName: 'i-ant-design:bars-outlined',
        label: '多选下拉框',
    },
    {
        value: 'RADIO',
        iconName: 'i-ant-design:check-circle-outlined',
        label: '单选',
    },
    {
        value: 'CHECKBOX',
        iconName: 'i-ant-design:check-square-outlined',
        label: '复选框',
    },
    {
        value: 'MEMBER',
        iconName: 'i-ant-design:user-outlined',
        label: '成员',
    },
    {
        value: 'MULTIPLE_MEMBER',
        iconName: 'i-ant-design:user-outlined',
        label: '成员多选',
    },
    {
        value: 'DATE',
        iconName: 'i-ant-design:calendar-outlined',
        label: '日期',
    },
    {
        value: 'DATETIME',
        iconName: 'i-ant-design:calendar-outlined',
        label: '日期时间',
    },
    {
        value: 'NUMBER',
        iconName: 'i-ant-design:borderless-table-outlined',
        label: '数字',
    },
    {
        value: 'INT',
        iconName: 'i-ant-design:borderless-table-outlined',
        label: '数字',
    },
    {
        value: 'FLOAT',
        iconName: 'i-ant-design:borderless-table-outlined',
        label: '数字',
    },
    {
        value: 'MULTIPLE_INPUT',
        iconName: 'i-ant-design:tag-outlined',
        label: '多值输入框',
    },
    {
        value: 'SYSTEM',
        iconName: 'i-ant-design:borderless-table-outlined',
        label: '',
    },
];
const organizationState = computed(() => templateStore.orgStatus);
const projectState = computed(() => templateStore.projectStatus);
export const getCardList = (type: string) => {
    const dataList = ref([
        {
            id: 1001,
            key: 'SCHEDULE',
            value: TemplateCardEnum.SCHEDULE,
            name: '定时任务模板',
        },
        {
            id: 1002,
            key: 'FUNCTIONAL',
            value: TemplateCardEnum.FUNCTIONAL,
            name: '用例模板',
        },
    ]);
    if (type === 'organization') {
        return dataList.value.map((item) => {
            return {
                ...item,
                enable: organizationState.value[item.key],
            };
        });
    }

    return dataList.value.map((item) => {
        return {
            ...item,
            enable: projectState.value[item.key],
        };
    });
}
