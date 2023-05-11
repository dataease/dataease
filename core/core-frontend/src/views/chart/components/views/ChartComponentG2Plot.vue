<script lang="ts" setup>
import { onMounted, ref, reactive } from 'vue'
import { baseBarOption } from '../js/panel/bar/bar'
import { getData } from '../../../../api/chart'
import { useEmitt } from '../../../../hooks/web/useEmitt'

const state = reactive({
  myChart: null,
  loading: false,
  data: null // 图表数据
})

const calcData = view => {
  state.loading = true
  const v = JSON.parse(JSON.stringify(view))
  // todo 后续放到其他地方
  v.yaxis.forEach(ele => {
    if (!ele.summary) {
      ele.summary = 'sum'
    }
    if (!ele.sort) {
      ele.sort = 'none'
    }
  })
  // console.log(v)
  getData(v).then(res => {
    // console.log(res)
    state.data = res?.data
    renderChart(res)
    state.loading = false
  })
}

const renderChart = view => {
  if (view && !view.data) {
    view.data = state.data
  }
  state.myChart = baseBarOption(state.myChart, 'container', view)
  state.myChart?.render()
}

onMounted(() => {
  renderChart(null)
  useEmitt({ name: 'calcData', callback: calcData })
  useEmitt({ name: 'renderChart', callback: renderChart })
})
</script>

<template>
  <div v-loading="state.loading" style="border: 1px solid #cccccc">
    <div id="container"></div>
  </div>
</template>

<style lang="less" scoped></style>
