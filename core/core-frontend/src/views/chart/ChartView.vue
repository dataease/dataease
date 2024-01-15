<script lang="ts" setup>
import ViewWrapper from '@/pages/panel/ViewWrapper.vue'
import { useRoute } from 'vue-router'
import { onBeforeMount } from 'vue'
const route = useRoute()
const init = () => {
  const busiFlag = route.query.busiFlag
  const dvId = route.query.dvId
  const chartId = route.query.chartId
  const embeddedToken = route.query.embeddedToken
  console.log(busiFlag)
  console.log(dvId)
  console.log(chartId)
  console.log(embeddedToken)
  window.DataEaseBi = {
    token: embeddedToken,
    chartId: chartId,
    dvId: dvId,
    busiFlag: busiFlag
  }
}
onBeforeMount(() => {
  init()
})
</script>

<template>
  <view-wrapper />
</template>
