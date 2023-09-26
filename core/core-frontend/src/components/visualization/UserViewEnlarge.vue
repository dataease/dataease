<template>
  <el-dialog
    ref="enlargeDialog"
    :title="titleInfo"
    :append-to-body="true"
    v-model="dialogShow"
    width="70vw"
    trigger="click"
  >
    <div class="export-button">
      <el-button
        v-if="optType === 'enlarge'"
        link
        icon="Download"
        size="middle"
        @click="downloadViewImage"
        >{{ t('chart.export_img') }}</el-button
      >
      <el-button
        v-if="optType === 'details'"
        link
        icon="Download"
        size="middle"
        @click="downloadViewDetails"
        >导出Excel</el-button
      >
      <el-divider direction="vertical" />
    </div>
    <div class="enlarge-outer" ref="viewContainer" v-if="dialogShow">
      <component-wrapper
        v-if="optType === 'enlarge'"
        class="enlarge-wrapper"
        :view-info="viewInfo"
        :config="config"
        :dv-info="dvInfo"
        show-position="viewDialog"
      />
      <chart-component-s2
        v-if="optType === 'details'"
        :view="viewInfo"
        show-position="viewDialog"
        ref="chartComponentDetails"
      />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import ComponentWrapper from '@/components/data-visualization/canvas/ComponentWrapper.vue'
import { computed, nextTick, ref } from 'vue'
import { toPng } from 'html-to-image'
import { useI18n } from '@/hooks/web/useI18n'
import { deepCopy } from '@/utils/utils'
import ChartComponentS2 from '@/views/chart/components/views/components/ChartComponentS2.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { VIEW_DETAILS_BASH_STYLE } from '@/views/chart/components/editor/util/dataVisualiztion'
import { innerExportDetails } from '@/api/chart'
import { exportExcelDownload } from '@/views/chart/components/js/util'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const dialogShow = ref(false)
let viewInfo = ref(null)
const config = ref(null)
const canvasStyleData = ref(null)
const viewContainer = ref(null)
const { t } = useI18n()
const optType = ref(null)
const chartComponentDetails = ref(null)
const { dvInfo } = storeToRefs(dvMainStore)

const titleInfo = computed(() => {
  return optType.value === 'enlarge' ? config?.value?.name : '查看数据'
})

const dialogInit = (canvasStyle, view, item, opt) => {
  optType.value = opt
  dialogShow.value = true
  viewInfo.value = deepCopy(view)
  viewInfo.value.customStyle.text.show = false
  config.value = deepCopy(item)
  canvasStyleData.value = canvasStyle
  if (opt === 'details') {
    dataDetailsOpt()
  }
}

const dataDetailsOpt = () => {
  nextTick(() => {
    const viewDataInfo = dvMainStore.getViewDataDetails(viewInfo.value.id)
    chartComponentDetails.value.renderChartFromDialog(
      JSON.parse(VIEW_DETAILS_BASH_STYLE),
      viewDataInfo
    )
  })
}

const downloadViewImage = () => {
  htmlToImage()
}

const downloadViewDetails = () => {
  const viewDataInfo = dvMainStore.getViewDataDetails(viewInfo.value.id)
  const chartExtRequest = dvMainStore.getLastViewRequestInfo(viewInfo.value.id)
  const chart = { ...viewInfo.value, chartExtRequest, data: viewDataInfo }
  exportExcelDownload(chart)
}
const htmlToImage = () => {
  toPng(viewContainer.value)
    .then(dataUrl => {
      const a = document.createElement('a')
      a.setAttribute('download', viewInfo.value.title)
      a.href = dataUrl
      a.click()
    })
    .catch(error => {
      console.error('oops, something went wrong!', error)
    })
}

defineExpose({
  dialogInit
})
</script>

<style lang="less" scoped>
.export-button {
  position: absolute;
  right: 48px;
  top: 26px;
  z-index: 2;
}
.enlarge-outer {
  position: relative;
  height: 65vh;
  .enlarge-wrapper {
    width: 100%;
    height: 100%;
  }
}
</style>
