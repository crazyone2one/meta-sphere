<script setup lang="ts">
import {NPagination} from "naive-ui";

const props = withDefaults(defineProps<{
  count: number,
  pageSlot?: number,
  size?: 'small' | 'medium' | 'large'
}>(), {
  pageSlot: 10,
  size: 'medium'
})

const page = defineModel<number>('page', {type: Number, default: 1});
const pageSize = defineModel<number>('pageSize', {type: Number, default: 10});
</script>

<template>
  <div class="mt-2 flex justify-end">
    <n-pagination v-model:page="page"
                  v-model:page-size="pageSize"
                  :item-count="props.count"
                  :size="size"
                  show-size-picker
                  :page-sizes="[10, 20, 30, 40,100]"
                  :page-slot="pageSlot"/>
  </div>

</template>

<style scoped>

</style>