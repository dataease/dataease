<template>
  <el-dialog
    ref="enlargeDialog"
    :title="viewInfo?.title"
    :append-to-body="true"
    v-model="dialogShow"
    width="70vw"
    trigger="click"
  >
    <div class="export-button">
      <el-button
        class="m-button"
        v-if="optType === 'enlarge'"
        link
        icon="Download"
        size="middle"
        @click="downloadViewImage"
      >
        {{ t('chart.export_img') }}
      </el-button>
      <el-button
        class="m-button"
        v-if="optType === 'details'"
        link
        icon="Download"
        size="middle"
        @click="downloadViewDetails"
      >
        导出Excel
      </el-button>
      <el-divider class="close-divider" direction="vertical" />
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
import { nextTick, ref } from 'vue'
import { toPng } from 'html-to-image'
import { useI18n } from '@/hooks/web/useI18n'
import { deepCopy } from '@/utils/utils'
import ChartComponentS2 from '@/views/chart/components/views/components/ChartComponentS2.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { VIEW_DETAILS_BASH_STYLE } from '@/views/chart/components/editor/util/dataVisualiztion'
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

  .m-button {
    color: #1f2329;
    font-size: 14px;
    font-style: normal;
    font-weight: 400;
  }

  .ed-button.is-link {
    font-size: 12px;
    font-weight: 400;
    padding: 4px;

    &:not(.is-disabled):focus,
    &:not(.is-disabled):hover {
      color: #1f2329;
      border-color: transparent;
      background-color: rgba(31, 35, 41, 0.1);
    }
    &:not(.is-disabled):active {
      color: #1f2329;
      border-color: transparent;
      background-color: rgba(31, 35, 41, 0.2);
    }
  }
}
.close-divider {
  margin: 0 16px 0 12px;
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
