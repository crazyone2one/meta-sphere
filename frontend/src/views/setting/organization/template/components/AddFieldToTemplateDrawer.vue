<script setup lang="ts">
import type {DefinedFieldItem} from "/@/api/modules/setting/template/types.ts";
import {computed, ref, watch, watchEffect} from "vue";
import EditFieldDrawer from "/@/views/setting/organization/template/components/EditFieldDrawer.vue";

const props = defineProps<{
  mode: 'organization' | 'project';
  totalData: DefinedFieldItem[]; // 所有字段
  tableSelectData: DefinedFieldItem[]; // 表格选择字段
}>();
const emit = defineEmits(['confirm', 'update:visible', 'update-data', 'brash']);
const active = defineModel<boolean>('active', {type: Boolean, default: false});
const totalList = ref<DefinedFieldItem[]>([]);
const selectSystemIds = ref<string[]>([]);
const selectCustomIds = ref<string[]>([]);
const showFieldDrawer = ref<boolean>(false);
const systemField = computed(() => {
  return totalList.value.filter((item: any) => item.internal);
});
const customField = computed(() => {
  return totalList.value.filter((item: any) => !item.internal);
});
// 计算当前系统字段是否全选
const isCheckSystemIdsAll = computed(() => {
  return systemField.value.length === selectSystemIds.value.length;
});
// 计算当前自定义字段是否全选
const isCheckCustomIdsAll = computed(() => {
  return customField.value.length === selectCustomIds.value.length;
});
// 计算是否全选
const isCheckedAll = ref(isCheckSystemIdsAll.value && isCheckCustomIdsAll.value)
// 计算是否半选
const indeterminate = computed(() => {
  return isCheckSystemIdsAll.value && isCheckCustomIdsAll.value;
});
const handleConfirm = () => {
  emit('confirm', selectedList.value);
  active.value = false;
}
const handleClose = () => {
  active.value = false;
  selectCustomIds.value = props.tableSelectData.filter((item) => !item.internal).map((item) => item.id);
  selectSystemIds.value = props.tableSelectData.filter((item) => item.internal).map((item) => item.id);
}
const createField = () => showFieldDrawer.value = true
const selectedList = ref<DefinedFieldItem[]>([]);
const handleCheckedChange = (checked: boolean) => {
  if (checked) {
    selectSystemIds.value = systemField.value.map((item) => item.id);
    selectCustomIds.value = customField.value.map((item) => item.id);
  } else {
    selectCustomIds.value = [];
  }
}
const totalIds = computed(() => {
  return [...new Set([...selectSystemIds.value, ...selectCustomIds.value])];
});
const handleClear = () => selectCustomIds.value = [];
// 移除已选择字段
const removeSelectedField = (id: string) => {
  selectCustomIds.value = selectCustomIds.value.filter((item) => item !== id);
  selectedList.value = selectedList.value.filter((item) => item.id !== id);
}
// 监视选择列表顺序按照选择列表排序
watch(
    () => totalIds.value,
    (val) => {
      const res = totalList.value.filter((item) => val.indexOf(item.id) > -1);
      selectedList.value = res.sort((a, b) => {
        return val.indexOf(a.id) - val.indexOf(b.id);
      });
    }
);
// 监视回显字段
watch(
    () => props.tableSelectData,
    () => {
      const sysField = props.tableSelectData.filter((item) => item.internal);
      const cusField = props.tableSelectData.filter((item) => !item.internal);
      selectSystemIds.value = sysField.map((item) => item.id);
      selectCustomIds.value = cusField.map((item) => item.id);
    }
);
watchEffect(() => {
  selectedList.value = props.tableSelectData;
  const sysField = props.tableSelectData.filter((item) => item.internal);
  const cusField = props.tableSelectData.filter((item) => !item.internal);
  selectSystemIds.value = sysField.map((item) => item.id);
  selectCustomIds.value = cusField.map((item) => item.id);
  selectedList.value = customField.value;
});
watchEffect(() => {
  totalList.value = props.totalData;
});
</script>

<template>
  <n-drawer v-model:show="active" :width="800">
    <n-drawer-content>
      <template #header>
        关联字段
      </template>
      <div class="panel-wrapper">
        <div class="inner-wrapper">
          <div class="optional-field">
            <div class="optional-header">
              <n-checkbox v-model:checked="isCheckedAll" :indeterminate="!indeterminate"
                          @update:checked="handleCheckedChange">
                <span class="font-medium text-#adb0bc">全选</span>
              </n-checkbox>
            </div>
            <div class="optional-panel p-4">
              <div class="mb-2 font-medium text-#adb0bc">系统字段</div>
              <div>
                <div>
                  <n-checkbox-group v-model:value="selectSystemIds" class="checkboxContainer">
                    <div v-for="field in systemField" :key="field.id" class="item checkbox">
                      <n-checkbox :value="field.id" :disabled="field.internal">{{ field.name }}</n-checkbox>
                    </div>
                  </n-checkbox-group>
                </div>
              </div>
              <div class="my-2 mt-8 font-medium text-#adb0bc">
                自定义字段
              </div>
              <div>
                <n-checkbox-group v-model:value="selectCustomIds" class="checkboxContainer">
                  <div v-for="field in customField" :key="field.id" class="item">
                    <n-checkbox :value="field.id" :disabled="field.internal" :label="field.name"/>
                  </div>
                </n-checkbox-group>
              </div>
              <edit-field-drawer :mode="props.mode" :data="totalData"/>
              <div>
                <n-button class="mt-1 px-0" text :disabled="totalData.length >= 20" @click="createField">
                  <template #icon>
                    <div class="i-mdi:plus-circle text-[14px]"/>
                  </template>
                  新增字段
                </n-button>
              </div>
            </div>
          </div>
          <div class="selected-field w-[272px]">
            <div class="optional-header">
              <div class="font-medium">已选字段</div>
              <n-button size="small" type="warning" @click="handleClear">清空</n-button>
            </div>
            <div class="selected-list p-4">
              <div v-for="element in selectedList" :key="element.dateIndex" class="selected-item">
                <span class="one-line-text ml-2 max-w-[180px]">{{ element.name }}</span>
                <div v-if="!element.internal" class="i-mdi:close-circle-outline text-[14px] cursor-pointer"
                     @click="removeSelectedField(element.id)"/>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <slot name="footer">
          <n-flex>
            <slot name="footerLeft"></slot>
            <n-button secondary @click="handleClose">取消</n-button>
            <n-button type="primary" @click="handleConfirm">保存
            </n-button>
          </n-flex>
        </slot>
      </template>
    </n-drawer-content>
  </n-drawer>
</template>

<style scoped>
.panel-wrapper {
  width: 100%;
  height: 100%;

  .inner-wrapper {
    display: flex;
    overflow: hidden;
    width: 100%;
    height: 100%;
    border: 1px solid;
    border-radius: 6px;

    .optional-field {
      flex-grow: 1;
      height: 100%;
      border-right: 1px solid;

      .optional-header {
        padding: 0 16px;
        height: 54px;

        @apply flex items-center justify-between;
      }
    }

    .selected-field {
      width: 272px;

      .optional-header {
        padding: 0 16px;
        height: 54px;

        @apply flex items-center justify-between;
      }

      .selected-list {
        .selected-item {
          height: 36px;

          cursor: move;
          @apply mb-2 flex items-center justify-between rounded px-2;
        }
      }
    }
  }
}

.checkboxContainer {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(116px, 1fr));
  grid-gap: 8px;
}
</style>