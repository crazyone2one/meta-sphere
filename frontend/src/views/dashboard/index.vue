<script setup lang="ts">

import * as echarts from 'echarts/core';
import {GridComponent,type GridComponentOption} from 'echarts/components';
import {LineChart,type LineSeriesOption} from 'echarts/charts';
import {UniversalTransition} from 'echarts/features';
import {CanvasRenderer} from 'echarts/renderers';
import {onMounted} from "vue";

echarts.use([GridComponent, LineChart, CanvasRenderer, UniversalTransition]);

type EChartsOption = echarts.ComposeOption<
    GridComponentOption | LineSeriesOption
>;


onMounted(() => {
  const chartDom = document.getElementById('main')!;
  const myChart = echarts.init(chartDom);
  let option: EChartsOption = {
    xAxis: {
      type: 'category',
      data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        data: [150, 230, 224, 218, 135, 147, 260],
        type: 'line'
      }
    ]
  };
  option && myChart.setOption(option);
})
</script>

<template>
  <div>
    <div id="main" style="width: 600px;height:400px;"></div>
  </div>
</template>

<style scoped>

</style>