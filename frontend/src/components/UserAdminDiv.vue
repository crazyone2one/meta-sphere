<script setup lang="ts">
const props = withDefaults(defineProps<{ name?: string; isAdmin?: boolean }>(), { isAdmin: false });
</script>

<template>
  <div class="flex w-full flex-row flex-nowrap">
    <div class="one-line-text ">{{ props.name }}</div>
    <div v-if="props.isAdmin" class="ml-1 flex flex-row flex-nowrap text-[#9597a4]">
      {{ `(管理员)` }}
    </div>
  </div>
</template>

<style scoped>

</style>