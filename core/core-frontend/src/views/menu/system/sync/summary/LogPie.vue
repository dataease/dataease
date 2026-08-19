<template>
  <div ref="mainEcharts" style="width: auto; height: 300px" id="LogPieChart" v-loading="loading"/>
</template>

<script lang="ts" setup>
import * as echarts from 'echarts'
import {computed, onMounted, onUnmounted, ref} from 'vue'
import {getJobLogLienChartInfo} from '@/api/sync/syncSummary'
import { useI18n } from "@/hooks/web/useI18n";
const {t} = useI18n()
const loading = ref<boolean>(false)
let resizeObserver: ResizeObserver | null = null,
    canResize = true
const data = ref({
  successCount: 0,
  failCount: 0,
  runningCount: 0
})
const getChartData = () => {
  getJobLogLienChartInfo().then(res => {
    data.value = res.data
    echartsInit()
  })
}
const colors = ['#32b74e', '#EE6666', '#0080ff']
const option = computed(() => {
  return {
    title: {
      text: t('sync_summary.sync_status_distribution'),
      textStyle: {
        overflow: 'break'
      },
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      type: 'scroll',
      orient: 'horizontal',
      bottom: 'bottom',
      icon:'circle',
      itemHeight:8,
      itemWidth:8,
    },
    series: [
      {
        type: 'pie',
        radius: '40%',
        label: {
          show: true,
          formatter(param) {
            // correct the percentage
            let color = ''
            if (param.dataIndex === 0) {
              color = '{success|}'
            }
            if (param.dataIndex === 1) {
              color = '{fail|}'
            }
            if (param.dataIndex === 2) {
              color = '{running|}'
            }
            return  color+' '+param.name + '：' + param.percent + '%\n\n'
          },
          rich: {
            success: {
              height: 8,
              width: 8,
              backgroundColor: colors[0],
              borderRadius: 5
            },
            fail: {
              height: 8,
              width: 8,
              backgroundColor: colors[1],
              borderRadius: 5
            },
            running: {
              height: 8,
              width: 8,
              backgroundColor: colors[2],
              borderRadius: 5
            }
          }
        },
        labelLayout: function (params) {
          const isLeft = params.labelRect.x < myChart.getWidth() / 2;
          const points = params.labelLinePoints;
          // Update the end point.
          points[2][0] = isLeft
              ? params.labelRect.x
              : params.labelRect.x + params.labelRect.width;
          return {
            labelLinePoints: points
          };
        },
        data: [
          {value: data.value.successCount, name: t('sync_task.status_success')},
          {value: data.value.failCount, name: t('sync_task.status_failed')},
          {value: data.value.runningCount, name: t('sync_task.status_running')}
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
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
const mainEcharts = ref()
let myChart;
const echartsInit = () => {
  myChart = echarts.init(mainEcharts.value)
  myChart.setOption(option.value)
  const targetElement = document.getElementById('LogPieChart')
  resizeObserver = new ResizeObserver(() => {
    if (!canResize) {
      return
    }
    canResize = false
    setTimeout(() => {
      canResize = true
      myChart.resize()
    }, 500)
  })
  targetElement && resizeObserver.observe(targetElement)
}
</script>

<style scoped></style>
