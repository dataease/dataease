<script lang="ts" setup>
import { ref, onBeforeMount } from 'vue'
import { findById } from '@/api/visualization/dataVisualization'
const config = ref()
const viewInfo = ref()
const canvasStyleData = ref()
const showPosition = ref('preview')
const userViewEnlargeRef = ref()
onBeforeMount(() => {
  findById(window.DataEaseBi.dvId).then(res => {
    canvasStyleData.value = JSON.parse(res?.data?.canvasStyleData) || {}
    viewInfo.value = res?.data?.canvasViewInfo[window.DataEaseBi.chartId]
    config.value = (
      (JSON.parse(res?.data?.componentData) as unknown as Array<{ id: string }>) || []
    ).find(ele => ele.id === window.DataEaseBi.chartId)
  })
})
const userViewEnlargeOpen = () => {
  userViewEnlargeRef.value.dialogInit(canvasStyleData.value, viewInfo.value, config.value)
}
</script>

<template>
  <div class="de-view-wrapper" v-if="!!config">
    <ComponentWrapper
      style="width: 100%; height: 100%"
      :view-info="viewInfo"
      :config="config"
      :show-position="showPosition"
      @userViewEnlargeOpen="userViewEnlargeOpen"
    />
    <user-view-enlarge ref="userViewEnlargeRef"></user-view-enlarge>
  </div>
</template>

<style lang="less" scoped>
.de-view-wrapper {
  width: 400px;
  height: 400px;
  position: relative;
}
</style>
