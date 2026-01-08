import {computed, ref} from "vue";
import useTemplateStore from "/@/store/modules/setting/template.ts";
import {TemplateCardEnum} from "/@/enums/common-enum.ts";
import type {FormItemType} from "/@/api/types.ts";
import {fieldIconAndName} from "/@/views/template/components/field-setting.ts";
import type {DefinedFieldItem} from "/@/api/modules/setting/template/types.ts";
import {FieldTypeFormRules} from "/@/utils/form-create.ts";

const templateStore = useTemplateStore()
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
    ])
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
export const getIconType = (iconType: FormItemType) => {
    return fieldIconAndName.find((item) => item.value === iconType);
};

export function getTemplateName(type: string, scene: string) {
    const dataList = getCardList(type);
    return dataList.find((item) => item.key === scene)?.name;
}

/**
 * 处理totalData自定义字段列表格式
 */
export const getTotalFieldOptionList = (totalData: DefinedFieldItem[]) => {
    return totalData.map((item: any) => {
        const currentFormRules = FieldTypeFormRules[item.type];
        let selectOptions: any = [];
        if (item.options && item.options.length) {
            selectOptions = item.options.map((optionItem: any) => {
                return {
                    label: optionItem.text,
                    value: optionItem.value,
                };
            });
            currentFormRules.options = selectOptions;
        }
        return {
            ...item,
            formRules: [
                {
                    ...currentFormRules,
                    title: item.name,
                    field: item.id,
                    effect: {
                        required: false,
                    },
                    props: {
                        ...currentFormRules.props,
                        options: selectOptions,
                        placeholder: '默认值',
                    },
                },
            ],
            fApi: null,
            required: item.internal && item.internalFieldKey === 'functional_priority',
        };
    });
}