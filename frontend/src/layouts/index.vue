<script setup lang="ts">
import Sidebar from '/@/layouts/sidebar/index.vue'
import Header from '/@/layouts/header/index.vue'
import Content from '/@/layouts/content/index.vue'
</script>

<template>
  <n-layout position="absolute" has-sider>
    <sidebar/>
    <n-layout>
      <Header/>
      <Content/>
    </n-layout>
  </n-layout>
</template>

<style scoped>

</style>