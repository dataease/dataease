<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import ChartComponentG2Plot from './components/ChartComponentG2Plot.vue'
import { nextTick, onBeforeMount, onMounted, reactive, ref, toRefs, watch } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { hexColorToRGBA } from '@/views/chart/components/js/util.js'
import { DEFAULT_TITLE_STYLE } from '@/views/chart/components/editor/util/chart'
import DrillPath from '@/views/chart/components/views/components/DrillPath.vue'
import { ElMessage } from 'element-plus-secondary'
import { useFilter } from '@/hooks/web/useFilter'
import { useCache } from '@/hooks/web/useCache'

import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { cloneDeep } from 'lodash-es'
import ChartComponentS2 from '@/views/chart/components/views/components/ChartComponentS2.vue'
import { ChartLibraryType } from '@/views/chart/components/js/panel/types'
import chartViewManager from '@/views/chart/components/js/panel'
import { storeToRefs } from 'pinia'
import { checkAddHttp, setIdValueTrans } from '@/utils/canvasUtils'
import { Base64 } from 'js-base64'

const { wsCache } = useCache()

const chartComponent = ref<any>()

const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()

const { nowPanelJumpInfo, publicLinkStatus, dvInfo } = storeToRefs(dvMainStore)

const props = defineProps({
  element: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  },
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
const dynamicAreaId = ref('')

const { view, showPosition, element } = toRefs(props)

const state = reactive({
  title_show: true,
  title_class: {
    margin: '0 0',
    width: '100%',
    fontSize: '18px',
    color: '#303133',
    textAlign: 'left',
    fontStyle: 'normal',
    fontWeight: 'normal',
    background: ''
  },
  drillFilters: [],
  drillClickDimensionList: []
})

watch(
  [() => view.value],
  () => {
    initTitle()
  },
  { deep: true }
)

const initTitle = () => {
  if (view.value.customStyle) {
    const customStyle = view.value.customStyle
    if (customStyle.text) {
      state.title_show = customStyle.text.show
      state.title_class.fontSize = customStyle.text.fontSize + 'px'
      state.title_class.color = customStyle.text.color
      state.title_class.textAlign = customStyle.text.hPosition
      state.title_class.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
      state.title_class.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'

      state.title_class.fontFamily = customStyle.text.fontFamily
        ? customStyle.text.fontFamily
        : DEFAULT_TITLE_STYLE.fontFamily
      state.title_class.letterSpacing =
        (customStyle.text.letterSpace
          ? customStyle.text.letterSpace
          : DEFAULT_TITLE_STYLE.letterSpace) + 'px'
      state.title_class.textShadow = customStyle.text.fontShadow ? '2px 2px 4px' : 'none'
    }
    if (customStyle.background) {
      state.title_class.background = hexColorToRGBA(
        customStyle.background.color,
        customStyle.background.alpha
      )
    }
  }
}

const drillJump = index => {
  // const length = state.drillClickDimensionList.length
  state.drillClickDimensionList = state.drillClickDimensionList.slice(0, index)
  // if (props.view.type === 'map' || props.view.type === 'buddle-map') {
  //   this.backToParent(index, length)
  // }
  view.value.chartExtRequest = filter()
  chartComponent?.value?.calcData(view.value)
}

const chartClick = param => {
  if (state.drillClickDimensionList.length < props.view.drillFields.length - 1) {
    state.drillClickDimensionList.push({ dimensionList: param.data.dimensionList })
    view.value.chartExtRequest = filter()
    chartComponent?.value?.calcData(view.value)
    // this.getData(this.element.propValue.viewId)
  } else if (props.view.drillFields.length > 0) {
    ElMessage.error(t('chart.last_layer'))
  }
}

// 仪表板和大屏所有额外过滤参数都在此处
const filter = (firstLoad?: boolean) => {
  const { filter } = useFilter(view.value.id, firstLoad)
  return {
    user: wsCache.get('user.uid'),
    filter,
    linkageFilters: element.value.linkageFilters,
    // outerParamsFilters: this.element.outerParamsFilters,
    drill: state.drillClickDimensionList
    // resultCount: this.resultCount,
    // resultMode: this.resultMode,
    // queryFrom: 'panel'
  }
}

const onDrillFilters = param => {
  state.drillFilters = param ? param : []
  nextTick(() => {
    chartComponent?.value?.renderChart(view.value)
  })
}

const windowsJump = (url, jumpType) => {
  try {
    window.open(url, jumpType)
  } catch (e) {
    ElMessage.error(t('visualization.url_check_error') + ':' + url)
  }
}

const jumpClick = param => {
  let dimension, jumpInfo, sourceInfo
  // 如果有名称name 获取和name匹配的dimension 否则倒序取最后一个能匹配的
  if (param.name) {
    param.dimensionList.forEach(dimensionItem => {
      if (dimensionItem.id === param.name || dimensionItem.value === param.name) {
        dimension = dimensionItem
        sourceInfo = param.viewId + '#' + dimension.id
        jumpInfo = nowPanelJumpInfo.value[sourceInfo]
      }
    })
  } else {
    for (let i = param.dimensionList.length - 1; i >= 0; i--) {
      dimension = param.dimensionList[i]
      sourceInfo = param.viewId + '#' + dimension.id
      jumpInfo = nowPanelJumpInfo.value[sourceInfo]
      if (jumpInfo) {
        break
      }
    }
  }
  if (jumpInfo) {
    param.sourceDvId = dvInfo.value.id
    param.sourceViewId = param.viewId
    param.sourceFieldId = dimension.id
    // 内部仪表板跳转
    if (jumpInfo.linkType === 'inner') {
      if (jumpInfo.targetDvId) {
        if (publicLinkStatus.value) {
          // 判断是否有公共链接ID
          if (jumpInfo.publicJumpId) {
            const url = '/link/' + jumpInfo.publicJumpId
            const currentUrl = window.location.href
            localStorage.setItem('beforeJumpUrl', currentUrl)
            windowsJump(url, jumpInfo.jumpType)
          } else {
            ElMessage.warning(t('visualization.public_link_tips'))
          }
        } else {
          const url =
            '#/preview/?dvId=' +
            jumpInfo.targetDvId +
            '&jumpInfoParam=' +
            encodeURI(Base64.encode(JSON.stringify(param)))
          windowsJump(url, jumpInfo.jumpType)
        }
      } else {
        ElMessage.warning('未指定跳转仪表板')
      }
    } else {
      const colList = [...param.dimensionList, ...param.quotaList]
      let url = setIdValueTrans('id', 'value', jumpInfo.content, colList)
      url = checkAddHttp(url)
      windowsJump(url, jumpInfo.jumpType)
    }
  } else {
    // if (this.chart.type.indexOf('table') === -1) {
    //   this.$message({
    //     type: 'warn',
    //     message: '未获取跳转信息',
    //     showClose: true
    //   })
    // }
  }
}

const queryData = (firstLoad = false) => {
  const queryFilter = filter(firstLoad)
  let params = cloneDeep(view.value)
  params['chartExtRequest'] = queryFilter
  chartComponent?.value?.calcData(params)
}

const showChartView = (...libs: ChartLibraryType[]) => {
  const chartView = chartViewManager.getChartView(view.value.render, view.value.type)
  return libs?.includes(chartView.library)
}

onBeforeMount(() => {
  useEmitt({
    name: `query-data-${view.value.id}`,
    callback: queryData
  })
})

onMounted(() => {
  queryData(true)
  useEmitt({
    name: 'snapshotChangeToView',
    callback: function (cacheViewInfo) {
      initTitle()
      nextTick(() => {
        if (
          cacheViewInfo.snapshotCacheViewCalc.includes(view.value.id) ||
          cacheViewInfo.snapshotCacheViewCalc.includes('all')
        ) {
          view.value.chartExtRequest = filter(false)
          chartComponent?.value?.calcData(view.value)
        } else if (
          cacheViewInfo.snapshotCacheViewRender.includes(view.value.id) ||
          cacheViewInfo.snapshotCacheViewRender.includes('all')
        ) {
          chartComponent?.value?.renderChart(view.value)
        }
      })
    }
  })
  useEmitt({
    name: 'calcData-' + view.value.id,
    callback: function (val) {
      initTitle()
      nextTick(() => {
        view.value.chartExtRequest = filter(false)
        chartComponent?.value?.calcData(val)
      })
    }
  })
  useEmitt({
    name: 'renderChart-' + view.value.id,
    callback: function (val) {
      initTitle()
      nextTick(() => {
        chartComponent?.value?.renderChart(val)
      })
    }
  })
  useEmitt({
    name: 'resetDrill-' + view.value.id,
    callback: function (val) {
      drillJump(val)
    }
  })
})

initTitle()
</script>

<template>
  <div class="chart-area">
    <p v-if="state.title_show" :style="state.title_class">{{ view.title }}</p>
    <!--这里去渲染不同图库的视图-->
    <chart-component-g2-plot
      style="flex: 1"
      :dynamic-area-id="dynamicAreaId"
      :view="view"
      :show-position="showPosition"
      v-if="showChartView(ChartLibraryType.G2_PLOT, ChartLibraryType.L7_PLOT)"
      ref="chartComponent"
      @onChartClick="chartClick"
      @onDrillFilters="onDrillFilters"
      @onJumpClick="jumpClick"
    />
    <chart-component-s2
      style="flex: 1"
      :view="view"
      :show-position="showPosition"
      v-else-if="showChartView(ChartLibraryType.S2)"
      ref="chartComponent"
      @onChartClick="chartClick"
      @onDrillFilters="onDrillFilters"
      @onJumpClick="jumpClick"
    />
    <drill-path :drill-filters="state.drillFilters" @onDrillJump="drillJump" />
  </div>
</template>

<style lang="less" scoped>
.chart-area {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>
