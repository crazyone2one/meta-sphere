<script setup lang="ts">
import {useAppStore} from "/@/store";

const appStore = useAppStore();
</script>

<template>
  <n-h1>project basic info</n-h1>
  <n-h1>{{appStore.currentProjectId}}</n-h1>
  <n-p>Not yet!</n-p>
</template>

<style scoped>

</style>