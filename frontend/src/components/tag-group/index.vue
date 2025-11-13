<script setup lang="ts" xmlns="http://www.w3.org/1999/html">
import {NTag} from "naive-ui";

defineProps<{ tagList: Array<any> }>()
const emit = defineEmits<(e: 'click') => void>();

</script>

<template>
  <n-space>
    <div v-if="tagList.length>0" @click="emit('click')">
      <n-tag v-for="tag in tagList" :key="tag.id" :bordered="false" type="info" size="small">
        {{ tag.name ?? '-' }}
      </n-tag>
    </div>
    <div v-else @click="emit('click')">
      -
    </div>
  </n-space>
</template>

<style scoped>

</style>