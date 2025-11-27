<script setup lang="ts">
import {useECharts} from "/@/hook/use-charts.ts";
import {ref} from "vue";
import type {EChartsOption} from "echarts";
import {barOption, lineOption, pieOption, scatterOption} from "/@/views/dashboard/utils.ts";

const mainChart = ref<HTMLElement | null>(null);
const secondChart = ref<HTMLElement | null>(null);
const pieChart = ref<HTMLElement | null>(null);
const scatterChart = ref<HTMLElement | null>(null);

const mainChartOption = ref(barOption)
const secondChartOption = ref(lineOption)
const pieChartOption = ref(pieOption)
const scatterChartOption = ref(scatterOption)
useECharts(mainChart, mainChartOption.value as EChartsOption);
useECharts(secondChart, secondChartOption.value as EChartsOption);
useECharts(pieChart, pieChartOption.value as EChartsOption);
useECharts(scatterChart, scatterChartOption.value as EChartsOption);
</script>

<template>
  <n-grid x-gap="12" :cols="4">
    <n-gi>
      <div ref="mainChart" style="width: 100%; height: 400px;"></div>
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