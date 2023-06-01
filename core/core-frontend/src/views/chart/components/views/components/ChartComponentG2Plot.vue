<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { getData } from '@/api/chart'
import { useEmitt } from '@/hooks/web/useEmitt'
import { ChartRenderType, G2PlotChartView } from '@/views/chart/components/js/panel/types'
import chartViewManager from '@/views/chart/components/js/panel'

const state = reactive({
  myChart: null,
  loading: false,
  data: [] // 图表数据
})
const calcData = view => {
  state.loading = true
  const v = JSON.parse(JSON.stringify(view))
  // console.log(v)
  getData(v).then(res => {
    // console.log(res)
    state.data = res?.data
    renderChart(res)
    state.loading = false
  })
}
const action = param => {
  console.log(param)
}
const renderChart = view => {
  view.data = state.data
  state.myChart = (
    chartViewManager.getChartView(view.render, view.type) as G2PlotChartView<any, any>
  ).drawChart({
    chartObj: state.myChart,
    container: 'container',
    chart: view,
    action
  })
  state.myChart?.render()
}

defineExpose({
  calcData,
  renderChart
})

onMounted(() => {
  renderChart({ render: ChartRenderType.ANT_V, type: 'bar' })
})
</script>

<template>
  <div v-loading="state.loading">
    <div id="container"></div>
  </div>
</template>

<style lang="less" scoped></style>
