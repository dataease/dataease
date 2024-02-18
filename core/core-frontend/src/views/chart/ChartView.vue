<script lang="ts" setup>
import ViewWrapper from '@/pages/panel/ViewWrapper.vue'
import { useEmbedded } from '@/store/modules/embedded'
import { useRoute } from 'vue-router'
import { onBeforeMount } from 'vue'
const route = useRoute()
const embeddedStore = useEmbedded()
const init = () => {
  const busiFlag = route.query.busiFlag as string
  const dvId = route.query.dvId as string
  const chartId = route.query.chartId as string
  const embeddedToken = route.query.embeddedToken as string
  embeddedStore.setBusiFlag(busiFlag)
  embeddedStore.setToken(embeddedToken)
  embeddedStore.setChartId(chartId)
  embeddedStore.setDvId(dvId)
}
onBeforeMount(() => {
  init()
})
</script>

<template>
  <view-wrapper />
</template>
