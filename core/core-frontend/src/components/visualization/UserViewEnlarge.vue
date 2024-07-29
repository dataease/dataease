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
      <el-select
        v-if="optType === 'enlarge' && authShow"
        v-model="pixel"
        class="pixel-select"
        size="small"
      >
        <el-option-group v-for="group in pixelOptions" :key="group.label" :label="group.label">
          <el-option
            v-for="item in group.options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-option-group>
      </el-select>

      <el-button
        class="m-button"
        v-if="optType === 'enlarge' && authShow"
        link
        icon="Download"
        size="middle"
        @click="downloadViewImage"
      >
        {{ t('chart.export_img') }}
      </el-button>
      <el-button
        class="m-button"
        v-if="optType === 'details' && authShow"
        link
        icon="Download"
        size="middle"
        :loading="exportLoading"
        @click="downloadViewDetails"
      >
        导出Excel
      </el-button>
      <el-button
        class="m-button"
        v-if="optType === 'details' && authShow && viewInfo.type === 'table-pivot'"
        link
        icon="Download"
        size="middle"
        :loading="exportLoading"
        :disabled="!enableFormattedExport"
        @click="exportAsFormattedExcel"
      >
        <span :title="enableFormattedExport ? '' : '树形模式暂不支持导出'">导出Excel(带格式)</span>
      </el-button>
      <el-divider class="close-divider" direction="vertical" v-if="authShow" />
    </div>
    <div
      v-loading="downLoading"
      element-loading-text="导出中..."
      element-loading-background="rgba(122, 122, 122, 1)"
      class="enlarge-outer"
      v-if="dialogShow"
    >
      <div
        class="enlarge-inner"
        :class="{
          'enlarge-inner-with-header': optType === 'details' && sourceViewType.includes('chart-mix')
        }"
        ref="viewContainer"
        :style="customExport"
      >
        <component-wrapper
          v-if="optType === 'enlarge'"
          class="enlarge-wrapper"
          :view-info="viewInfo"
          :config="config"
          :dv-info="dvInfo"
          show-position="viewDialog"
        />
        <chart-component-s2
          v-if="optType === 'details' && !sourceViewType.includes('chart-mix')"
          :view="viewInfo"
          show-position="viewDialog"
          ref="chartComponentDetails"
        />
        <template v-else-if="optType === 'details' && sourceViewType.includes('chart-mix')">
          <el-tabs class="tab-header" v-model="activeName" @tab-change="handleClick">
            <el-tab-pane :label="t('chart.drag_block_value_axis_left')" name="left"></el-tab-pane>
            <el-tab-pane :label="t('chart.drag_block_value_axis_right')" name="right"></el-tab-pane>
          </el-tabs>
          <div style="flex: 1">
            <chart-component-s2
              v-if="activeName === 'left'"
              :view="viewInfo"
              show-position="viewDialog"
              ref="chartComponentDetails"
            />
            <chart-component-s2
              v-else-if="activeName === 'right'"
              :view="viewInfo"
              show-position="viewDialog"
              ref="chartComponentDetails2"
            />
          </div>
        </template>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import ComponentWrapper from '@/components/data-visualization/canvas/ComponentWrapper.vue'
import { computed, h, nextTick, ref } from 'vue'
import { toPng } from 'html-to-image'
import { useI18n } from '@/hooks/web/useI18n'
import { deepCopy } from '@/utils/utils'
import ChartComponentS2 from '@/views/chart/components/views/components/ChartComponentS2.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { exportExcelDownload } from '@/views/chart/components/js/util'
import { storeToRefs } from 'pinia'
import { RefreshLeft } from '@element-plus/icons-vue'
import { assign } from 'lodash-es'
import { useEmitt } from '@/hooks/web/useEmitt'
import { ElMessage, ElButton } from 'element-plus-secondary'
import { exportPivotExcel } from '@/views/chart/components/js/panel/common/common_table'
const downLoading = ref(false)
const dvMainStore = dvMainStoreWithOut()
const dialogShow = ref(false)
let viewInfo = ref<DeepPartial<ChartObj>>(null)
const config = ref(null)
const canvasStyleData = ref(null)
const viewContainer = ref(null)
const { t } = useI18n()
const optType = ref(null)
const chartComponentDetails = ref(null)
const chartComponentDetails2 = ref(null)
const { dvInfo, editMode } = storeToRefs(dvMainStore)
const exportLoading = ref(false)
const sourceViewType = ref()
const activeName = ref('left')
const DETAIL_CHART_ATTR: DeepPartial<ChartObj> = {
  render: 'antv',
  type: 'table-info',
  customAttr: {
    basicStyle: {
      tableColumnMode: 'dialog',
      tablePageMode: 'pull'
    },
    tableHeader: {
      tableHeaderBgColor: '#F8F8F9',
      tableHeaderFontColor: '#7C7E81'
    },
    tableCell: {
      tableItemBgColor: '#FFFFFF',
      tableFontColor: '#7C7E81',
      enableTableCrossBG: false
    },
    tooltip: {
      show: false
    }
  },
  senior: {
    scrollCfg: {
      open: false
    }
  }
}
const DETAIL_TABLE_ATTR: DeepPartial<ChartObj> = {
  senior: {
    scrollCfg: {
      open: false
    }
  }
}

const authShow = computed(() => editMode.value === 'edit' || dvInfo.value.weight > 3)

const customExport = computed(() => {
  if (downLoading.value) {
    const bashStyle = pixel.value.split(' * ')
    return {
      width: bashStyle[0] + 'px!important',
      height: bashStyle[1] + 'px!important'
    }
  } else {
    return {}
  }
})

const pixel = ref('1280 * 720')

const pixelOptions = [
  {
    label: 'Windows(16:9)',
    options: [
      {
        value: '1920 * 1080',
        label: '1920 * 1080'
      },
      {
        value: '1600 * 900',
        label: '1600 * 900'
      },
      {
        value: '1280 * 720',
        label: '1280 * 720'
      }
    ]
  },
  {
    label: 'MacOS(16:10)',
    options: [
      {
        value: '2560 * 1600',
        label: '2560 * 1600'
      },
      {
        value: '1920 * 1200',
        label: '1920 * 1200'
      },
      {
        value: '1680 * 1050',
        label: '1680 * 1050'
      }
    ]
  }
]
const dialogInit = (canvasStyle, view, item, opt) => {
  sourceViewType.value = view.type
  optType.value = opt
  dialogShow.value = true
  viewInfo.value = deepCopy(view) as DeepPartial<ChartObj>
  viewInfo.value.customStyle.text.show = false
  config.value = deepCopy(item)
  canvasStyleData.value = canvasStyle
  if (opt === 'details') {
    if (!viewInfo.value.type?.includes('table')) {
      assign(viewInfo.value, DETAIL_CHART_ATTR)
    } else {
      assign(viewInfo.value, DETAIL_TABLE_ATTR)
    }
    dataDetailsOpt()
  }
}

const dataDetailsOpt = () => {
  nextTick(() => {
    const viewDataInfo = dvMainStore.getViewDataDetails(viewInfo.value.id)
    if (sourceViewType.value.includes('chart-mix')) {
      chartComponentDetails.value?.renderChartFromDialog(viewInfo.value, viewDataInfo.left)
      chartComponentDetails2.value?.renderChartFromDialog(viewInfo.value, viewDataInfo.right)
    } else {
      chartComponentDetails.value.renderChartFromDialog(viewInfo.value, viewDataInfo)
    }
  })
}

const handleClick = tab => {
  nextTick(() => {
    const viewDataInfo = dvMainStore.getViewDataDetails(viewInfo.value.id)
    if (tab === 'left') {
      chartComponentDetails.value?.renderChartFromDialog(viewInfo.value, viewDataInfo.left)
    } else if (tab === 'right') {
      chartComponentDetails2.value?.renderChartFromDialog(viewInfo.value, viewDataInfo.right)
    }
  })
}

const downloadViewImage = () => {
  htmlToImage()
}

const downloadViewDetails = () => {
  const viewDataInfo = dvMainStore.getViewDataDetails(viewInfo.value.id)
  const chartExtRequest = dvMainStore.getLastViewRequestInfo(viewInfo.value.id)
  const chart = {
    ...viewInfo.value,
    chartExtRequest,
    data: viewDataInfo,
    type: sourceViewType.value
  }
  exportLoading.value = true
  exportExcelDownload(chart, () => {
    openMessageLoading(exportData)
  })
  exportLoading.value = false
}

const exportAsFormattedExcel = () => {
  const s2Instance = dvMainStore.getViewInstanceInfo(viewInfo.value.id)
  if (!s2Instance) {
    return
  }
  const chart = dvMainStore.getViewDetails(viewInfo.value.id)
  exportPivotExcel(s2Instance, chart)
}
const enableFormattedExport = computed(() => {
  const chart = dvMainStore.getViewDetails(viewInfo.value.id) as ChartObj
  const mode = chart?.customAttr?.basicStyle?.tableLayoutMode
  return mode === 'grid'
})
const exportData = () => {
  useEmitt().emitter.emit('data-export-center', { activeName: 'IN_PROGRESS' })
}

const openMessageLoading = cb => {
  const iconClass = `el-icon-loading`
  const customClass = `de-message-loading de-message-export`
  ElMessage({
    message: h('p', null, [
      '后台导出中,可前往',
      h(
        ElButton,
        {
          text: true,
          size: 'small',
          class: 'btn-text',
          onClick: () => {
            cb()
          }
        },
        t('data_export.export_center')
      ),
      '查看进度，进行下载'
    ]),
    iconClass,
    icon: h(RefreshLeft),
    showClose: true,
    customClass
  })
}
const htmlToImage = () => {
  downLoading.value = true
  useEmitt().emitter.emit('renderChart-' + viewInfo.value.id)
  setTimeout(() => {
    toPng(viewContainer.value)
      .then(dataUrl => {
        downLoading.value = false
        const a = document.createElement('a')
        a.setAttribute('download', viewInfo.value.title)
        a.href = dataUrl
        a.click()
        useEmitt().emitter.emit('renderChart-' + viewInfo.value.id)
      })
      .catch(error => {
        downLoading.value = false
        useEmitt().emitter.emit('renderChart-' + viewInfo.value.id)
        console.error('oops, something went wrong!', error)
      })
  }, 500)
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

  .pixel-select {
    width: 125px;
    margin-right: 8px;
    margin-top: -1px;
  }

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
  overflow: hidden;
  .enlarge-inner {
    position: relative;
    width: 100%;
    height: 100%;
  }
  .enlarge-inner-with-header {
    display: flex;
    flex-direction: column;
  }
  .enlarge-wrapper {
    width: 100%;
    height: 100%;
  }
}
.tab-header {
  margin-top: -10px;
  margin-bottom: 10px;
  --ed-tabs-header-height: 34px;
  --custom-tab-color: #646a73;

  :deep(.ed-tabs__nav-wrap::after) {
    background-color: unset;
  }

  &.dark {
    --custom-tab-color: #a6a6a6;
  }

  :deep(.ed-tabs__item) {
    font-weight: 400;
    font-size: 12px;
    padding: 0 8px !important;
    margin-right: 12px;
    color: var(--custom-tab-color);
  }
  :deep(.is-active) {
    font-weight: 500;
    color: var(--ed-color-primary, #3370ff);
  }

  :deep(.ed-tabs__nav-scroll) {
    padding-left: 0 !important;
  }

  :deep(.ed-tabs__header) {
    margin: 0 !important;
  }

  :deep(.ed-tabs__content) {
    height: calc(100% - 35px);
    overflow-y: auto;
    overflow-x: hidden;
  }
}
</style>
