<template>
  <div ref="mainEcharts" style="width: auto; height: 300px" id="logLineChart" v-loading="loading"/>
</template>

<script lang="ts" setup>
import * as echarts from 'echarts'
import {computed, onMounted, onUnmounted, ref} from 'vue'
import {getJobLogLienChartInfo} from '@/api/sync/syncSummary'
import { useI18n } from "@/hooks/web/useI18n";
const {t} = useI18n()
const mainEcharts = ref()
const loading = ref<boolean>(false)
let resizeObserver: ResizeObserver | null = null,
    canResize = true
const data = ref({
  executeDateList: [],
  executeDateRunningList: [],
  executeDateSuccessList: [],
  executeDateFailList: []
})
const getChartData = () => {
  getJobLogLienChartInfo(loading).then(res => {
    data.value = res.data
    echartsInit()
  })
}
const colors = ['#32b74e', '#EE6666']
const option = computed(() => {
  return {
    title: {
      text: t('sync_summary.execution_results_in_the_past_7_days')
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    legend: {
      type: 'scroll',
      orient: 'horizontal',
      bottom: 'bottom',
      icon:'circle',
      data: [t('sync_task.status_success'), t('sync_task.status_failed')],
      itemHeight:8,
      itemWidth:8,
    },
    toolbox: {
      feature: {
        /*saveAsImage: {}*/
      }
    },
    grid: {
      left: '3%',
      right: '6%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: data.value.executeDateList
      }
    ],
    yAxis: [
      {
        type: 'value'
      }
    ],
    series: [
      {
        name: t('sync_task.status_success'),
        type: 'line',
        //stack: 'Total', 这个stack会导致跟上一条线重合
        symbolSize:0,
        data: data.value.executeDateSuccessList
      },
      {
        name: t('sync_task.status_failed'),
        type: 'line',
        //stack: 'Total',
        symbolSize:0,
        data: data.value.executeDateFailList
      }
    ],
    color: colors
  }
})
onMounted(() => {
  getChartData()
})
onUnmounted(() => {
  resizeObserver?.disconnect()
})
const echartsInit = () => {
  // 在生命周期中挂载
  const echartsInit = echarts.init(mainEcharts.value)
  echartsInit.setOption(option.value)
  const targetElement = document.getElementById('logLineChart')
  resizeObserver = new ResizeObserver(() => {
    if (!canResize) {
      return
    }
    canResize = false
    setTimeout(() => {
      canResize = true
      echartsInit.resize()
    }, 500)
  })
  targetElement && resizeObserver.observe(targetElement)
}
</script>

<style scoped></style>
