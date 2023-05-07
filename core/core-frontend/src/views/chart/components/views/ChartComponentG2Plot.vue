<script lang="ts" setup>
import { onMounted, ref, reactive } from 'vue'
import { baseBarOption } from '../js/panel/bar/bar'
import { getData } from '../../../../api/chart'
import { useEmitt } from '../../../../hooks/web/useEmitt'

const state = reactive({
  myChart: null
})

const calcData = view => {
  const v = { ...view }
  v.yaxis.forEach(ele => {
    if (!ele.summary) {
      ele.summary = 'sum'
    }
  })
  v.xaxis = JSON.stringify(v.xaxis)
  v.yaxis = JSON.stringify(v.yaxis)
  v.customAttr = JSON.stringify(v.customAttr)
  console.log(v)
  getData(v).then(res => {
    console.log(res)
    renderChart(res)
  })
}

const renderChart = view => {
  state.myChart = baseBarOption(state.myChart, 'container', view)
  state.myChart.render()
}

onMounted(() => {
  renderChart(null)
  useEmitt({ name: 'calcData', callback: calcData })
})
</script>

<template>
  <div>
    <div id="container"></div>
  </div>
</template>

<style lang="less" scoped></style>
