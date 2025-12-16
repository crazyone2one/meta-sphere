<script setup lang="ts">
import BarChart from "/@/views/dashboard/components/chart/BarChart.vue";
import {useTemplateRef} from "vue";

const barChart = useTemplateRef<InstanceType<typeof BarChart>>('barChart')
</script>

<template>
  <n-grid x-gap="12" :cols="4">
    <n-gi>
      <bar-chart ref="barChart"/>
    </n-gi>
    <n-gi>
      <div ref="secondChart" style="width: 100%; height: 400px;"></div>
    </n-gi>
    <n-gi>
      <div ref="pieChart" style="width: 100%; height: 400px;"></div>
    </n-gi>
    <n-gi>
      <div ref="scatterChart" style="width: 100%; height: 400px;"></div>
    </n-gi>
  </n-grid>
</template>

<style scoped>

</style>