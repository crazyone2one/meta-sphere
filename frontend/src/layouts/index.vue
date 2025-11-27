<script setup lang="ts">
import Sidebar from '/@/layouts/sidebar/index.vue'
import Header from '/@/layouts/header/index.vue'
import Content from '/@/layouts/content/index.vue'
</script>

<template>
  <n-layout position="absolute" >
    <Header/>
    <n-layout has-sider>
      <sidebar/>
      <Content/>
    </n-layout>
  </n-layout>
</template>

<style scoped>

</style>