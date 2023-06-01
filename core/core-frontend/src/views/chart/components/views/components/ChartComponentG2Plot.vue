<script lang="ts" setup>
import { onMounted, reactive, ref, toRefs } from 'vue'
import { getData } from '@/api/chart'
import { G2PlotChartView } from '@/views/chart/components/js/panel/types'
import chartViewManager from '@/views/chart/components/js/panel'

const props = defineProps({
  view: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  }
})

const { view } = toRefs(props)

const state = reactive({
  myChart: null,
  loading: false,
  data: null // 图表数据
})

const containerId = 'container-' + view.value.id

const calcData = view => {
  state.loading = true
  const v = JSON.parse(JSON.stringify(view))
  // console.log(v)
  getData(v)
    .then(res => {
      // console.log(res)
      state.data = res?.data
      renderChart(res)
    })
    .finally(() => {
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
    container: containerId,
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
  calcData(view.value)
  // renderChart({ render: ChartRenderType.ANT_V, type: 'bar' })
})
</script>

<template>
  <div class="canvas-area" v-loading="state.loading">
    <div class="canvas-content" :id="containerId"></div>
  </div>
</template>

<style lang="less" scoped>
.canvas-area {
  width: 100%;
  height: 100%;
  .canvas-content {
    width: 100%;
    height: 100%;
  }
}
</style>
