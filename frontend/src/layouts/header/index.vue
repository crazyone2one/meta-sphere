<script setup lang="ts">
import {NFlex} from "naive-ui";

import ProjectSelect from "/@/layouts/header/components/ProjectSelect.vue";
import NavEnd from "/@/layouts/header/components/NavEnd.vue";
import TopMenu from "/@/layouts/header/components/TopMenu.vue";
</script>

<template>
  <n-layout-header style="height: 64px; padding: 12px" bordered>
    <n-flex justify="space-between" class="bg-transparent">
      <div >
        <project-select/>
      </div>
      <div>
        <top-menu/>
      </div>
      <div class="flex mr-[20px]">
        <nav-end/>
      </div>
    </n-flex>
  </n-layout-header>
</template>

<style scoped>

</style>