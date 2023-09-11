<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import ChartComponentG2Plot from './components/ChartComponentG2Plot.vue'
import {
  computed,
  nextTick,
  onBeforeMount,
  onMounted,
  PropType,
  provide,
  reactive,
  ref,
  ShallowRef,
  shallowRef,
  toRefs,
  watch
} from 'vue'
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
import DeRichTextView from '@/custom-component/rich-text/DeRichTextView.vue'

const { wsCache } = useCache()

const chartComponent = ref<any>()

const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()

const { nowPanelJumpInfo, publicLinkStatus, dvInfo, curComponent, canvasStyleData } =
  storeToRefs(dvMainStore)

const props = defineProps({
  active: {
    type: Boolean,
    default: false
  },
  element: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  },
  view: {
    type: Object as PropType<ChartObj>,
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
  },
  // 仪表板刷新计时器
  searchCount: {
    type: Number,
    required: false,
    default: 0
  }
})
const dynamicAreaId = ref('')
const { view, showPosition, element, active, searchCount } = toRefs(props)

const titleShow = computed(() => element.value.innerType !== 'rich-text' && state.title_show)

const state = reactive({
  initReady: true, //curComponent 切换期间 不接收外部的calcData 和 renderChart 事件
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

const loading = ref(false)

const resultMode = computed(() => {
  return canvasStyleData.value.dashboard?.resultMode || null
})

const resultCount = computed(() => {
  return canvasStyleData.value.dashboard?.resultCount || null
})

watch(
  [() => view.value],
  () => {
    initTitle()
  },
  { deep: true }
)

watch([() => searchCount.value], () => {
  queryData()
})
// 仪表板的查询结果设置变化 视图数据需要刷新
watch([() => resultCount.value], () => {
  queryData()
})

watch([() => resultMode.value], () => {
  queryData()
})

watch([() => curComponent.value], () => {
  if (curComponent.value && curComponent.value.id === view.value.id) {
    state.initReady = false
    nextTick(() => {
      state.initReady = true
    })
  }
})

const chartExtRequest = shallowRef(null)
provide('chartExtRequest', chartExtRequest)

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
  state.drillClickDimensionList = state.drillClickDimensionList.slice(0, index)
  view.value.chartExtRequest = filter()
  calcData(view.value)
}

const chartClick = param => {
  // 下钻字段第一个没有在维度中不允许下钻
  const xIds = view.value.xAxis.map(ele => ele.id)
  if (xIds.indexOf(props.view.drillFields[0].id) == -1) {
    ElMessage.error(t('chart.drill_field_error'))
    return
  }
  if (state.drillClickDimensionList.length < props.view.drillFields.length - 1) {
    state.drillClickDimensionList.push({ dimensionList: param.data.dimensionList })
    view.value.chartExtRequest = filter()
    calcData(view.value)
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
    drill: state.drillClickDimensionList,
    resultCount: resultCount.value,
    resultMode: resultMode.value
  }
}

const onDrillFilters = param => {
  state.drillFilters = param ? param : []
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
            '#/preview?dvId=' +
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
  }
}

const queryData = (firstLoad = false) => {
  const queryFilter = filter(firstLoad)
  let params = cloneDeep(view.value)
  params['chartExtRequest'] = queryFilter
  chartExtRequest.value = queryFilter
  calcData(params)
}

const calcData = params => {
  loading.value = true
  nextTick(() => {
    chartComponent?.value?.calcData?.(params, () => {
      loading.value = false
    })
  })
}

const showChartView = (...libs: ChartLibraryType[]) => {
  if (view.value?.render && view.value?.type) {
    const chartView = chartViewManager.getChartView(view.value.render, view.value.type)
    return libs?.includes(chartView.library)
  } else {
    return false
  }
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
          calcData(view.value)
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
      if (!state.initReady) {
        return
      }
      initTitle()
      nextTick(() => {
        view.value.chartExtRequest = filter(false)
        calcData(val)
      })
    }
  })
  useEmitt({
    name: 'renderChart-' + view.value.id,
    callback: function (val) {
      if (!state.initReady) {
        return
      }
      initTitle()
      nextTick(() => {
        chartComponent?.value?.renderChart(val)
      })
    }
  })
  useEmitt({
    name: 'resetDrill-' + view.value.id,
    callback: function (val) {
      nextTick(() => {
        drillJump(val)
      })
    }
  })
  useEmitt({
    name: 'tabCanvasChange-' + element.value.canvasId,
    callback: function () {
      if (!state.initReady && !view.value.type.includes('table')) {
        return
      }
      setTimeout(function () {
        chartComponent?.value?.renderChart(view.value)
      }, 200)
    }
  })
})

// 1.开启仪表板刷新 2.首次加载（searchCount =0 ）3.正在请求数据 则显示加载状态
const loadingFlag = computed(() => {
  return (canvasStyleData.value.refreshViewLoading || searchCount.value === 0) && loading.value
})
initTitle()
</script>

<template>
  <div class="chart-area">
    <p v-if="titleShow" :style="state.title_class">{{ view.title }}</p>
    <!--这里去渲染不同图库的视图-->
    <div style="flex: 1; overflow: hidden">
      <de-rich-text-view
        v-if="showChartView(ChartLibraryType.RICH_TEXT)"
        ref="chartComponent"
        :element="element"
        :active="active"
      />
      <chart-component-g2-plot
        :dynamic-area-id="dynamicAreaId"
        :view="view"
        :show-position="showPosition"
        v-else-if="showChartView(ChartLibraryType.G2_PLOT, ChartLibraryType.L7_PLOT)"
        ref="chartComponent"
        @onChartClick="chartClick"
        @onDrillFilters="onDrillFilters"
        @onJumpClick="jumpClick"
      />
      <chart-component-s2
        :view="view"
        :show-position="showPosition"
        v-else-if="showChartView(ChartLibraryType.S2)"
        ref="chartComponent"
        @onChartClick="chartClick"
        @onDrillFilters="onDrillFilters"
        @onJumpClick="jumpClick"
      />
    </div>
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
