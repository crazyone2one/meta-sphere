<script setup lang="ts">
import {NTabPane,NTabs} from 'naive-ui'
import Chart from '/@/views/dashboard/components/chart/index.vue'
import EChart from '/@/views/dashboard/components/echart/index.vue'
</script>

<template>
  <n-card>
    <n-tabs type="line" animated>
      <n-tab-pane name="chart" tab="chart">
        <chart/>
      </n-tab-pane>
      <n-tab-pane name="eChart" tab="eChart">
        <e-chart/>
      </n-tab-pane>
    </n-tabs>
  </n-card>
</template>

<style scoped>

</style>