// 表单字段使用

export const INPUT = {
    type: 'input',
    title: '',
    field: 'fieldName',
    value: '',
    props: {
        'placeholder': '请输入',
        'max-length': 255,
        'allow-clear': true,
    },
};
export const SELECT = {
    type: 'SearchSelect',
    field: 'fieldName',
    title: '',
    value: '',
    options: [],
    props: {
        'multiple': false,
        // 'placeholder': t('formCreate.PleaseSelect'),
        'options': [],
        'modelValue': '',
        'allow-clear': true,
    },
};

export const MULTIPLE_SELECT = {
    type: 'SearchSelect',
    field: 'fieldName',
    title: '',
    value: [],
    options: [],
    props: {
        'multiple': true,
        // 'placeholder': t('formCreate.PleaseSelect'),
        'options': [],
        'modelValue': [],
        'allow-clear': true,
    },
};

export const RADIO = {
    type: 'radio',
    field: 'fieldName',
    title: '',
    value: '',
    options: [],
};

export const CHECKBOX = {
    type: 'checkbox',
    field: 'fieldName',
    title: '',
    value: [],
    options: [],
};

export const MEMBER = {
    type: 'SearchSelect',
    field: 'fieldName',
    title: '',
    value: '',
    options: [],
    props: {
        'multiple': false,
        // 'placeholder': t('formCreate.PleaseSelect'),
        'modelValue': '',
        'allow-clear': true,
    },
};

export const MULTIPLE_MEMBER = {
    type: 'SearchSelect',
    field: 'fieldName',
    title: '',
    value: [],
    options: [],
    props: {
        'multiple': true,
        // 'placeholder': t('formCreate.PleaseSelect'),
        'options': [],
        'modelValue': [],
        'allow-clear': true,
    },
};

export const DATE = {
    type: 'a-date-picker',
    field: 'fieldName',
    title: '',
    value: '',
    props: {
        // 'placeholder': t('formCreate.PleaseSelect'),
        'format': 'YYYY-MM-DD',
        'show-time': false,
    },
};

export const DATETIME = {
    type: 'a-date-picker',
    field: 'fieldName',
    title: '',
    value: '',
    props: {
        // 'placeholder': t('formCreate.PleaseSelect'),
        'format': 'YYYY-MM-DD HH:mm:ss',
        'show-time': true,
        'show-now-btn': true,
    },
};

export const FLOAT = {
    type: 'InputNumber',
    field: 'fieldName',
    title: '',
    value: 0.0,
    props: {
        precision: 2,
        placeholder: '请输入',
    },
};

export const INT = {
    type: 'InputNumber',
    field: 'fieldName',
    title: '',
    value: 0,
    props: {
        precision: 0,
        max: 999999999999999,
        min: -999999999999999,
        placeholder: '请输入',
    },
};

export const MULTIPLE_INPUT = {
    type: 'MsTagsInput',
    field: 'fieldName',
    title: '',
    value: [],
    props: {
        'placeholder': '请输入',
        'allow-clear': true,
    },
};

export const TEXTAREA = {
    type: 'a-textarea',
    field: 'fieldName',
    title: '',
    value: '',
    props: {
        'placeholder': '请输入',
        'max-length': 1000,
        'auto-size': {minRows: 1},
    },
};
export const JIRAKEY = {
    type: 'JiraKey',
    field: 'jiraKey',
    title: '',
    value: '',
    props: {
        modelValue: '',
        instructionsIcon: '',
    },
};

export const PASSWORD = {
    type: 'PassWord',
    field: 'fieldName',
    title: '',
    value: '',
    props: {
        'modelValue': '',
        'instructionsIcon': '',
        'allow-clear': true,
    },
};

export const CASCADER = {
    type: 'cascader',
    field: 'fieldName',
    title: '',
    value: [],
    props: {
        'allow-clear': true,
    },
};

export const FieldTypeFormRules: Record<string, any> = {
    INPUT,
    SELECT,
    MULTIPLE_SELECT,
    RADIO,
    CHECKBOX,
    MEMBER,
    MULTIPLE_MEMBER,
    DATE,
    DATETIME,
    INT,
    FLOAT,
    MULTIPLE_INPUT,
    TEXTAREA,
    JIRAKEY,
    PASSWORD,
    CASCADER,
};
