<script setup lang="ts">
import {NFlex} from "naive-ui";

import ProjectSelect from "/@/layouts/header/components/ProjectSelect.vue";
import NavEnd from "/@/layouts/header/components/NavEnd.vue";
</script>

<template>
  <n-layout-header style="height: 64px; padding: 24px" bordered>
    <n-flex>
      <div class="flex-1 items-center mb-2">
        <project-select/>
      </div>
      <div class="items-center flex mr-[20px]">
        <nav-end/>
      </div>
    </n-flex>
  </n-layout-header>
</template>

<style scoped>

</style>