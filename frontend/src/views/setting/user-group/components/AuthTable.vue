<script setup lang="ts">
import type {
  AuthTableItem,
  CurrentUserGroupItem,
  SavePermissions,
  UserGroupAuthSetting
} from "/@/api/modules/setting/types.ts";
import {AuthScopeEnum, type AuthScopeEnumType} from "/@/enums/common-enum.ts";
import {computed, h, inject, ref, type VNode, watchEffect} from "vue";
import {useRequest} from "alova/client";
import {userGroupApi} from "/@/api/modules/setting/user-group.ts";
import {type DataTableColumns, NCheckbox, NCheckboxGroup, NFlex} from "naive-ui";

const systemType = inject<AuthScopeEnumType>('systemType');
const {current = {id: ''}, disabled = false, showBottom = true} = defineProps<{
  current: CurrentUserGroupItem;
  disabled?: boolean,
  showBottom?: boolean
}>()
const allChecked = ref(false);
const allIndeterminate = ref(false);
// 是否可以保存
const canSave = ref(false);
const tableData = ref<AuthTableItem[]>();
const {send} = useRequest((id) => userGroupApi.getGlobalUSetting(id), {immediate: false});
const makeData = (item: UserGroupAuthSetting) => {
  const result: AuthTableItem[] = [];
  item.children?.forEach((child, index) => {
    if (!child.license || child.license) {
      const perChecked =
          child?.permissions?.reduce((acc: string[], cur) => {
            if (cur.enable) {
              acc.push(cur.id);
            }
            return acc;
          }, []) || [];
      const perCheckedLength = perChecked.length;
      let indeterminate = false;
      if (child?.permissions) {
        indeterminate = perCheckedLength > 0 && perCheckedLength < child?.permissions?.length;
      }
      result.push({
        id: child?.id,
        license: child?.license,
        enable: child?.enable,
        permissions: child?.permissions,
        indeterminate,
        perChecked,
        ability: index === 0 ? item.name : undefined,
        operationObject: child.name,
        rowSpan: index === 0 ? item.children?.length || 1 : undefined,
      });
    }
  });
  return result;
}
// 转换数据 计算系统，组织，项目的合并行数
const transformData = (data: UserGroupAuthSetting[]) => {
  const result: AuthTableItem[] = [];
  data.forEach((item) => {
    result.push(...makeData(item));
  });
  return result;
};
const handleAllChange = (isInit = false) => {
  if (!tableData.value) return;
  const tmpArr = tableData.value;
  const {length: allLength} = tmpArr;
  const {length} = tmpArr.filter((item) => item.enable);
  if (length === allLength) {
    allChecked.value = true;
    allIndeterminate.value = false;
  } else if (length === 0) {
    allChecked.value = false;
    allIndeterminate.value = false;
  } else {
    allChecked.value = false;
    allIndeterminate.value = true;
  }
  if (!isInit && !canSave.value) canSave.value = true;
}
const initData = (id: string) => {
  tableData.value = []; // 重置数据，可以使表格滚动条重新计算
  if (systemType === AuthScopeEnum.SYSTEM) {
    send(id).then(res => {
      tableData.value = transformData(res);
      handleAllChange(true);
    });
  }

}
const systemAdminDisabled = computed(() => {
  const adminArr = ['admin', 'org_admin', 'project_admin'];
  const {id} = current;
  if (adminArr.includes(id)) {
    // 系统管理员,组织管理员，项目管理员都不可编辑
    return true;
  }

  return disabled;
});
const handleAllAuthChangeByCheckbox = () => {

}
const tableColumns: DataTableColumns<AuthTableItem> = [
  {title: '功能', key: 'ability', width: 100},
  {title: '操作对象', key: 'operationObject', width: 100},
  {
    title: () => {
      return h(NFlex, {justify: 'space-between'}, {
        default: () => {
          const res = [
            h("div", {class: ''}, {default: () => '权限'}),
          ];
          if (tableData.value && tableData.value.length > 0) {
            res.push(h(NCheckbox, {
              checked: allChecked.value,
              indeterminate: allIndeterminate.value,
              disabled: systemAdminDisabled.value || disabled,
              class: 'mr-[7px]', onUpdateChecked: () => handleAllAuthChangeByCheckbox()
            }, {}))
          }
          return res
        }
      })
    }, key: 'operationObject',
    render(row, rowIndex) {
      return h('div', {class: 'flex flex-row items-center justify-between'}, {
        default: () => {
          return [
            h(NCheckboxGroup, {
              value: row.perChecked,
              onUpdateValue: (v, e) => handleCellAuthChange(v, rowIndex, row, e)
            }, {
              default: () => {
                const res: VNode[] = []
                row.permissions?.forEach(item => {
                  res.push(
                      h(NCheckbox, {value: item.id, label: item.name}, {})
                  )
                })
                return res
              }
            }),
            h(NCheckbox, {
              class: 'mr-[7px]', checked: row.enable, indeterminate: row.indeterminate,
              disabled: systemAdminDisabled.value || disabled,
              onUpdateChecked: (value: boolean) => handleRowAuthChange(value, rowIndex)
            }, {})
          ]
        }
      })
    }
  },
]
const handleRowAuthChange = (value: boolean | (string | number | boolean)[], rowIndex: number) => {
  if (!tableData.value) return;
  const tmpArr = tableData.value;
  const row = tmpArr[rowIndex];
  // 检查row是否存在以及permissions是否存在
  if (!row) return;
  row.indeterminate = false;
  if (value) {
    row.enable = true;
    row.perChecked = row.permissions?.map((item) => item.id) ?? [];
  } else {
    row.enable = false;
    row.perChecked = [];
  }
  tableData.value = [...tmpArr];
  handleAllChange();
  if (!canSave.value) canSave.value = true;
}
const handleCellAuthChange = (_value: (string | number)[], rowIndex: number,
                              row: AuthTableItem,
                              e: {
                                actionType: 'check' | 'uncheck';
                                value: string | number;
                              }) => {
  setAutoRead(row, e.value as string);
  if (!tableData.value) return;
  const tmpArr = tableData.value;
  const tmpRow = tmpArr[rowIndex];
  // 检查row是否存在以及permissions是否存在
  if (!tmpRow) return;
  const length = tmpRow.permissions?.length || 0;
  if (row.perChecked?.length === length) {
    tmpRow.enable = true;
    tmpRow.indeterminate = false;
  } else if (row.perChecked?.length === 0) {
    tmpRow.enable = false;
    tmpRow.indeterminate = false;
  } else {
    tmpRow.enable = false;
    tmpRow.indeterminate = true;
  }
  handleAllChange();
}
const setAutoRead = (record: AuthTableItem, currentValue: string) => {
  if (!record.perChecked?.includes(currentValue)) {
    // 如果当前没有选中则执行自动添加查询权限逻辑
    // 添加权限值
    record.perChecked?.push(currentValue);
    const preStr = currentValue.split(':')[0];
    const postStr = currentValue.split(':')[1];
    const lastEditStr = currentValue.split('+')[1]; // 编辑类权限通过+号拼接
    const existRead = record.perChecked?.some(
        (item: string) => item.split(':')[0] === preStr && item.split(':')[1] === 'READ'
    );
    const existCreate = record.perChecked?.some(
        (item: string) => item.split(':')[0] === preStr && item.split(':')[1] === 'ADD'
    );
    if (!existRead && postStr !== 'READ') {
      record.perChecked?.push(`${preStr}:READ`);
    }
    if (!existCreate && lastEditStr === 'IMPORT') {
      // 勾选导入时自动勾选新增和查询
      record.perChecked?.push(`${preStr}:ADD`);
      record.perChecked?.push(`${preStr}:READ+UPDATE`);
    }
  } else {
    // 删除权限值
    const preStr = currentValue.split(':')[0];
    const postStr = currentValue.split(':')[1];
    if (postStr === 'READ') {
      // 当前是查询 那 移除所有相关的
      record.perChecked = record.perChecked.filter((item: string) => !item.includes(preStr as string));
    } else {
      record.perChecked.splice(record.perChecked.indexOf(currentValue), 1);
    }
  }
}
const handleReset = () => {
  if (current.id) {
    initData(current.id);
  }
}
const handleSave = () => {
  if (!tableData.value) return;
  const permissions: SavePermissions[] = [];
  const tmpArr = tableData.value;
  tmpArr.forEach((item) => {
    item.permissions?.forEach((ele) => {
      ele.enable = item.perChecked?.includes(ele.id) || false;
      permissions.push({id: ele.id, enable: ele.enable});
    });
  });
  if (systemType === AuthScopeEnum.SYSTEM) {
    userGroupApi.saveGlobalUSetting({
      userRoleId: current.id,
      permissions,
    }).then(() => {
      canSave.value = false;
      window.$message.success('保存成功');
      initData(current.id);
    })
  }
}
watchEffect(() => {
  if (current.id) {
    initData(current.id);
  }
});
</script>

<template>
  <div class="flex h-full flex-col gap-[16px] overflow-hidden">
    <div class="group-auth-table">
      <n-data-table :data="tableData" :columns="tableColumns"/>
    </div>
    <div v-if="showBottom && current.id !== 'admin' && !systemAdminDisabled"
         class="footer">
      <n-button :disabled="!canSave" @click="handleReset">撤销修改</n-button>
      <n-button type="primary" :disabled="!canSave" @click="handleSave">
        保存
      </n-button>
    </div>
  </div>
</template>

<style scoped>
.group-auth-table {
  @apply flex-1 overflow-hidden;
  padding: 0 16px 16px;

  :deep(.n-data-table-th__title) {
    width: 100%;
  }

  :deep(.n-data-table-th) {
    line-height: normal;
  }
}

.footer {
  display: flex;
  justify-content: flex-end;
  padding: 24px;
  box-shadow: 0 -1px 4px rgb(2 2 2 / 10%);
  gap: 16px;
}
</style>