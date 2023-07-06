<script lang="ts" setup>
import { onMounted, reactive, toRefs } from 'vue'
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
  },
  showPosition: {
    type: String,
    required: false,
    default: 'canvas'
  }
})

const emit = defineEmits(['onChartClick', 'onDrillFilters'])

const { view, showPosition } = toRefs(props)

const state = reactive({
  myChart: null,
  loading: false,
  data: [] // 图表数据
})

const containerId = 'container-' + showPosition.value + '-' + view.value.id

const calcData = view => {
  state.loading = true
  const v = JSON.parse(JSON.stringify(view))
  // console.log(v)
  getData(v)
    .then(res => {
      // console.log(res)
      state.data = res?.data
      emit('onDrillFilters', res.drillFilters)
      renderChart(res)
    })
    .finally(() => {
      state.loading = false
    })
}

const renderChart = view => {
  // view 为引用对象 需要存库 view.data 直接赋值会导致保存不必要的数据
  const chart = { ...view, data: state.data }
  state.myChart = (
    chartViewManager.getChartView(view.render, view.type) as G2PlotChartView<any, any>
  ).drawChart({
    chartObj: state.myChart,
    container: containerId,
    chart: chart,
    action
  })
  state.myChart?.render()
}

const action = param => {
  console.log(param)
  // 当前只有下钻
  const pointParam = param.data
  emit('onChartClick', pointParam)
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
