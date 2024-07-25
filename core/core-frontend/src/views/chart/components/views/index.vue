<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import ChartComponentG2Plot from './components/ChartComponentG2Plot.vue'
import DeIndicator from '@/custom-component/indicator/DeIndicator.vue'
import { useAppStoreWithOut } from '@/store/modules/app'
import router from '@/router'
import { useEmbedded } from '@/store/modules/embedded'
import { XpackComponent } from '@/components/plugin'
import { PluginComponent } from '@/components/plugin'
import {
  computed,
  CSSProperties,
  nextTick,
  onBeforeMount,
  onMounted,
  PropType,
  provide,
  reactive,
  ref,
  shallowRef,
  toRefs,
  watch
} from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { hexColorToRGBA } from '@/views/chart/components/js/util.js'
import {
  CHART_FONT_FAMILY_MAP,
  DEFAULT_TITLE_STYLE
} from '@/views/chart/components/editor/util/chart'
import DrillPath from '@/views/chart/components/views/components/DrillPath.vue'
import { ElIcon, ElInput, ElMessage } from 'element-plus-secondary'
import { useFilter } from '@/hooks/web/useFilter'
import { useCache } from '@/hooks/web/useCache'
import { parseUrl } from '@/utils/ParseUrl'

import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { cloneDeep } from 'lodash-es'
import ChartComponentS2 from '@/views/chart/components/views/components/ChartComponentS2.vue'
import { ChartLibraryType } from '@/views/chart/components/js/panel/types'
import chartViewManager from '@/views/chart/components/js/panel'
import { storeToRefs } from 'pinia'
import { checkAddHttp, setIdValueTrans } from '@/utils/canvasUtils'
import { Base64 } from 'js-base64'
import DeRichTextView from '@/custom-component/rich-text/DeRichTextView.vue'
import ChartEmptyInfo from '@/views/chart/components/views/components/ChartEmptyInfo.vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { viewFieldTimeTrans } from '@/utils/viewUtils'
import { CHART_TYPE_CONFIGS } from '@/views/chart/components/editor/util/chart'
import request from '@/config/axios'
import { store } from '@/store'

const { wsCache } = useCache()
const chartComponent = ref<any>()
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const { emitter } = useEmitt()

let innerRefreshTimer = null
const appStore = useAppStoreWithOut()
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)
const isIframe = computed(() => appStore.getIsIframe)

const emit = defineEmits(['onPointClick'])

const { nowPanelJumpInfo, publicLinkStatus, dvInfo, curComponent, canvasStyleData, mobileInPc } =
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
  themes: {
    type: String,
    required: false,
    default: 'dark'
  },
  showPosition: {
    type: String,
    required: false,
    default: 'preview'
  },
  // 仪表板刷新计时器
  searchCount: {
    type: Number,
    required: false,
    default: 0
  },
  disabled: {
    type: Boolean,
    required: false,
    default: false
  },
  scale: {
    type: Number,
    required: false,
    default: 1
  }
})
const dynamicAreaId = ref('')
const { view, showPosition, element, active, searchCount, scale } = toRefs(props)

const titleShow = computed(
  () =>
    element.value.innerType !== 'rich-text' &&
    state.title_show &&
    showPosition.value !== 'viewDialog'
)
const snapshotStore = snapshotStoreWithOut()

const state = reactive({
  initReady: true, //curComponent 切换期间 不接收外部的calcData 和 renderChart 事件
  title_show: true,
  title_remark: {
    show: false,
    remark: ''
  },
  title_class: {
    fontSize: '18px',
    color: '#303133',
    textAlign: 'left',
    fontStyle: 'normal',
    fontWeight: 'normal',
    background: '',
    fontFamily: '',
    textShadow: 'none',
    letterSpacing: '0px',
    fontSynthesis: 'style weight',
    width: 'fit-content',
    maxWidth: '100%',
    wordBreak: 'break-word',
    whiteSpace: 'pre-wrap'
  } as CSSProperties,
  drillFilters: [],
  viewInfoData: null,
  drillClickDimensionList: []
})

const drillClickLength = computed(() => state.drillClickDimensionList.length)

const titleAlign = computed<string>(() => {
  if (!titleShow.value) {
    return 'flex-start'
  }

  if (state.title_class.textAlign === 'center') {
    return 'center'
  } else if (state.title_class.textAlign === 'right') {
    return 'flex-end'
  }

  return 'flex-start'
})

const trackMenu = computed<Array<string>>(() => {
  return chartComponent?.value?.trackMenu ?? []
})

const hasLinkIcon = computed(() => {
  return trackMenu.value.indexOf('linkage') > -1 || trackMenu.value.indexOf('linkageAndDrill') > -1
})
const hasJumpIcon = computed(() => {
  return trackMenu.value.indexOf('jump') > -1 && !mobileInPc.value
})
const hasDrillIcon = computed(() => {
  return trackMenu.value.indexOf('drill') > -1 || trackMenu.value.indexOf('linkageAndDrill') > -1
})

const loading = ref(false)

const resultMode = computed(() => {
  return canvasStyleData.value.dashboard?.resultMode || null
})

const resultCount = computed(() => {
  return canvasStyleData.value.dashboard?.resultCount || null
})

const embeddedStore = useEmbedded()
// 编辑状态下 不启动刷新
const buildInnerRefreshTimer = (
  refreshViewEnable = false,
  refreshUnit = 'minute',
  refreshTime = 5
) => {
  if (showPosition.value === 'preview' && !innerRefreshTimer && refreshViewEnable) {
    innerRefreshTimer && clearInterval(innerRefreshTimer)
    const timerRefreshTime = refreshUnit === 'second' ? refreshTime * 1000 : refreshTime * 60000
    innerRefreshTimer = setInterval(() => {
      clearViewLinkage()
      queryData()
    }, timerRefreshTime)
  }
}

// 清除相同sourceViewId 的 联动条件
const clearViewLinkage = () => {
  dvMainStore.clearViewLinkage(element.value.id)
  useEmitt().emitter.emit('clearPanelLinkage', { viewId: element.value.id })
}

watch(
  [() => view.value],
  () => {
    initTitle()
  },
  { deep: true }
)

watch([() => scale.value], () => {
  initTitle()
})

watch([() => searchCount.value], () => {
  // 内部计时器启动 忽略外部计时器
  if (!innerRefreshTimer) {
    queryData()
  }
})
// 仪表板的查询结果设置变化 图表数据需要刷新
watch([() => resultCount.value], () => {
  queryData()
})

watch([() => resultMode.value], () => {
  queryData()
})

watch([() => scale.value], () => {
  nextTick(() => {
    chartComponent?.value?.renderChart?.(view.value)
  })
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
      state.title_class.fontSize = customStyle.text.fontSize * scale.value + 'px'
      state.title_class.color = customStyle.text.color
      state.title_class.textAlign = customStyle.text.hPosition as CSSProperties['textAlign']
      state.title_class.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
      state.title_class.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'

      state.title_class.fontFamily = customStyle.text.fontFamily
        ? CHART_FONT_FAMILY_MAP[customStyle.text.fontFamily]
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

    state.title_remark.show = customStyle.text.show && customStyle.text.remarkShow
    state.title_remark.remark = customStyle.text.remark
  }
}

const drillJump = (index: number) => {
  state.drillClickDimensionList.splice(index)
  view.value.chartExtRequest = filter()
  calcData(view.value)
}

const onPointClick = param => {
  try {
    const msg = {
      sourceDvId: dvInfo.value.id,
      sourceViewId: view.value.id,
      message: Base64.encode(JSON.stringify(param))
    }
    emit('onPointClick', msg)
  } catch (e) {
    console.warn('de_inner_params send error')
  }
}

const chartClick = param => {
  // 下钻字段第一个没有在维度中不允许下钻
  const xIds = view.value.xAxis.map(ele => ele.id)
  if (xIds.indexOf(props.view.drillFields[0].id) == -1) {
    ElMessage.error(t('chart.drill_field_error'))
    return
  }
  if (state.drillClickDimensionList.length < props.view.drillFields.length - 1) {
    state.drillClickDimensionList.push({
      dimensionList: param.data.dimensionList,
      extra: param.extra
    })
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
    outerParamsFilters: element.value.outerParamsFilters,
    drill: state.drillClickDimensionList,
    resultCount: resultCount.value,
    resultMode: resultMode.value
  }
}

const onDrillFilters = param => {
  state.drillFilters = param ? param : []
}
const openHandler = ref(null)
const initOpenHandler = newWindow => {
  if (openHandler?.value) {
    const pm = {
      methodName: 'initOpenHandler',
      args: newWindow
    }
    openHandler.value.invokeMethod(pm)
  }
}

const divEmbedded = type => {
  useEmitt().emitter.emit('changeCurrentComponent', type)
}

const windowsJump = (url, jumpType) => {
  try {
    const newWindow = window.open(url, jumpType)
    initOpenHandler(newWindow)
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
    // 维度日期类型转换
    viewFieldTimeTrans(dvMainStore.getViewDataDetails(param.viewId), param)
    param.sourceDvId = dvInfo.value.id
    param.sourceViewId = param.viewId
    param.sourceFieldId = dimension.id
    let embeddedBaseUrl = ''
    const divSelf = isDataEaseBi.value && jumpInfo.jumpType === '_self'
    const iframeSelf = isIframe.value && jumpInfo.jumpType === '_self'
    if (isDataEaseBi.value) {
      embeddedBaseUrl = embeddedStore.baseUrl
    }
    // 内部仪表板跳转
    if (jumpInfo.linkType === 'inner') {
      if (jumpInfo.targetDvId) {
        if (publicLinkStatus.value) {
          // 判断是否有公共链接ID
          if (jumpInfo.publicJumpId) {
            const url = `${embeddedBaseUrl}#/de-link/${
              jumpInfo.publicJumpId
            }?jumpInfoParam=${encodeURIComponent(Base64.encode(JSON.stringify(param)))}`
            const currentUrl = window.location.href
            localStorage.setItem('beforeJumpUrl', currentUrl)
            windowsJump(url, jumpInfo.jumpType)
          } else {
            ElMessage.warning(t('visualization.public_link_tips'))
          }
        } else {
          const url = `${embeddedBaseUrl}#/preview?dvId=${
            jumpInfo.targetDvId
          }&jumpInfoParam=${encodeURIComponent(Base64.encode(JSON.stringify(param)))}`

          if (isIframe.value || isDataEaseBi.value) {
            embeddedStore.clearState()
          }
          if (divSelf) {
            embeddedStore.setDvId(jumpInfo.targetDvId)
            embeddedStore.setJumpInfoParam(encodeURIComponent(Base64.encode(JSON.stringify(param))))
            divEmbedded('Preview')
            return
          }

          if (iframeSelf) {
            router.push(parseUrl(url))
            return
          }
          windowsJump(url, jumpInfo.jumpType)
        }
      } else {
        ElMessage.warning('未指定跳转仪表板')
      }
    } else {
      const colList = [...param.dimensionList, ...param.quotaList]
      let url = setIdValueTrans('id', 'value', jumpInfo.content, colList)
      url = checkAddHttp(url)

      if (isIframe.value || isDataEaseBi.value) {
        embeddedStore.clearState()
      }
      if (divSelf) {
        embeddedStore.setOuterUrl(url)
        divEmbedded('Iframe')
        return
      }

      windowsJump(url, jumpInfo.jumpType)
    }
  } else {
  }
}

const queryData = (firstLoad = false) => {
  if (loading.value) {
    return
  }
  const searched = dvMainStore.firstLoadMap.includes(element.value.id)
  const queryFilter = filter(searched ? false : firstLoad)
  let params = cloneDeep(view.value)
  params['chartExtRequest'] = queryFilter
  chartExtRequest.value = queryFilter
  calcData(params)
}

const calcData = params => {
  dvMainStore.setLastViewRequestInfo(params.id, params.chartExtRequest)
  if (chartComponent?.value) {
    loading.value = true
    if (view.value.isPlugin) {
      chartComponent?.value?.invokeMethod({
        methodName: 'calcData',
        args: [
          params,
          res => {
            loading.value = false
          }
        ]
      })
    } else {
      chartComponent?.value?.calcData?.(params, res => {
        loading.value = false
      })
    }
  }
}

const showChartView = (...libs: ChartLibraryType[]) => {
  if (view.value?.render && view.value?.type) {
    const chartView = chartViewManager.getChartView(view.value.render, view.value.type)
    return chartView && libs?.includes(chartView.library)
  } else {
    return false
  }
}

onBeforeMount(() => {
  if (!showPosition.value.includes('viewDialog')) {
    nextTick(() => {
      useEmitt({
        name: `query-data-${view.value.id}`,
        callback: queryData
      })
    })
  }
})
// 部分场景不需要更新图表，例如放大页面
const listenerEnable = computed(() => {
  return !showPosition.value.includes('viewDialog')
})
onMounted(() => {
  if (!view.value.isPlugin) {
    queryData(true && !showPosition.value.includes('viewDialog'))
  }
  if (!listenerEnable.value) {
    return
  }
  useEmitt({
    name: 'clearPanelLinkage',
    callback: function (param) {
      if (param.viewId === 'all' || param.viewId === element.value.id) {
        chartComponent?.value?.clearLinkage?.()
      }
    }
  })
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
          chartComponent?.value?.renderChart?.(view.value)
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
    name: 'calcData-all',
    callback: function () {
      if (!state.initReady) {
        return
      }
      initTitle()
      nextTick(() => {
        view.value.chartExtRequest = filter(false)
        calcData(view.value)
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
      const viewInfo = val ? val : view.value
      nextTick(() => {
        chartComponent?.value?.renderChart?.(viewInfo)
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
        chartComponent?.value?.renderChart?.(view.value)
      }, 200)
    }
  })
  useEmitt({
    name: 'updateTitle-' + view.value.id,
    callback: () => {
      initTitle()
    }
  })

  const { refreshViewEnable, refreshUnit, refreshTime } = view.value
  buildInnerRefreshTimer(refreshViewEnable, refreshUnit, refreshTime)

  initTitle()
})

// 1.开启仪表板刷新 2.首次加载（searchCount =0 ）3.正在请求数据 则显示加载状态
const loadingFlag = computed(() => {
  return (canvasStyleData.value.refreshViewLoading || searchCount.value === 0) && loading.value
})

const chartAreaShow = computed(() => {
  if (view.value.tableId) {
    if (element.value['state'] === undefined || element.value['state'] === 'ready') {
      return true
    }
  }
  if (view.value.type === 'rich-text') {
    return true
  }
  if (view.value?.isPlugin) {
    return true
  }
  if (view.value['dataFrom'] === 'template') {
    return true
  }
  if (view.value.customAttr?.map?.id) {
    const MAP_CHARTS = ['map', 'bubble-map', 'flow-map']
    if (MAP_CHARTS.includes(view.value.type)) {
      return true
    }
  }
  return false
})

const titleInputRef = ref()
const titleEditStatus = ref(false)
function changeEditTitle() {
  if (!props.active) {
    return
  }
  if (!titleEditStatus.value) {
    titleEditStatus.value = true
    nextTick(() => {
      titleInputRef.value?.focus()
      element.value['editing'] = true
    })
  }
}

function onLeaveTitleInput() {
  element.value['editing'] = false
  titleEditStatus.value = false
}

//v-click-outside 指令
const vClickOutside = {
  beforeMount(el, binding) {
    // 在元素上绑定一个事件监听器
    el.clickOutsideEvent = function (event) {
      // 判断点击事件是否发生在元素外部
      if (!(el === event.target || el.contains(event.target))) {
        // 如果是外部点击，则执行绑定的函数
        binding.value(event)
      }
    }
    // 在全局添加点击事件监听器
    document.addEventListener('click', el.clickOutsideEvent)
  },
  unmounted(el) {
    // 在组件销毁前，移除事件监听器以避免内存泄漏
    document.removeEventListener('click', el.clickOutsideEvent)
  }
}

function onTitleChange() {
  snapshotStore.recordSnapshotCache()
}

const toolTip = computed(() => {
  return props.themes === 'dark' ? 'ndark' : 'dark'
})

const marginBottom = computed<string | 0>(() => {
  if (titleShow.value || trackMenu.value.length > 0 || state.title_remark.show) {
    return 12 * scale.value + 'px'
  }
  return 0
})

const iconSize = computed<string>(() => {
  return 16 * scale.value + 'px'
})

const titleIconStyle = computed(() => {
  return {
    color: canvasStyleData.value.component.seniorStyleSetting.linkageIconColor
  }
})
const chartHover = ref(false)
const showActionIcons = computed(() => {
  if (!chartHover.value) {
    return false
  }
  return trackMenu.value.length > 0 || state.title_remark.show
})
const chartConfigs = ref(CHART_TYPE_CONFIGS)
const pluginLoaded = computed(() => {
  let result = false
  chartConfigs.value.forEach(cat => {
    result = cat.details.find(chart => view.value?.type === chart.value) !== undefined
  })
  return result
})
// TODO 统一加载
const loadPluginCategory = data => {
  data.forEach(item => {
    const { category, title, render, chartValue, chartTitle, icon, staticMap } = item
    const node = {
      render,
      category,
      icon,
      value: chartValue,
      title: chartTitle,
      isPlugin: true,
      staticMap
    }
    if (view.value?.type === node.value) {
      view.value.plugin = {
        isPlugin: true,
        staticMap
      }
    }
    const stack = [...chartConfigs.value]
    let findParent = false
    while (stack?.length) {
      const parent = stack.pop()
      if (parent.category === category) {
        const chart = parent.details.find(chart => chart.value === node.value)
        if (!chart) {
          parent.details.push(node)
        }
        findParent = true
      }
    }
    if (!findParent) {
      stack.push({
        category,
        title,
        display: 'show',
        details: [node]
      })
    }
  })
}
</script>

<template>
  <div
    class="chart-area report-load"
    :class="{ 'report-load-finish': !loadingFlag }"
    v-loading="loadingFlag"
    element-loading-background="rgba(0,0,0,0)"
    @mouseover="chartHover = true"
    @mouseleave="chartHover = false"
  >
    <div
      class="title-container"
      :style="{ 'justify-content': titleAlign, 'margin-bottom': marginBottom }"
    >
      <template v-if="!titleEditStatus">
        <p v-if="titleShow" :style="state.title_class" @dblclick="changeEditTitle">
          {{ view.title }}
        </p>
      </template>
      <template v-else>
        <el-input
          style="flex: 1"
          :effect="canvasStyleData.dashboard.themeColor"
          ref="titleInputRef"
          v-model="view.title"
          @keydown.stop
          @keydown.enter="onLeaveTitleInput"
          v-click-outside="onLeaveTitleInput"
          @change="onTitleChange"
        />
      </template>
      <transition name="fade">
        <div
          class="icons-container"
          :class="{ 'is-editing': titleEditStatus }"
          :style="titleIconStyle"
          v-show="showActionIcons"
        >
          <el-tooltip :effect="toolTip" placement="top" v-if="state.title_remark.show">
            <template #content>
              <div style="white-space: pre-wrap" v-html="state.title_remark.remark"></div>
            </template>
            <el-icon :size="iconSize" class="inner-icon">
              <Icon name="icon_info_outlined" />
            </el-icon>
          </el-tooltip>
          <el-tooltip :effect="toolTip" placement="top" content="已设置联动" v-if="hasLinkIcon">
            <el-icon :size="iconSize" class="inner-icon">
              <Icon name="icon_link-record_outlined" />
            </el-icon>
          </el-tooltip>
          <el-tooltip :effect="toolTip" placement="top" content="已设置跳转" v-if="hasJumpIcon">
            <el-icon :size="iconSize" class="inner-icon">
              <Icon name="icon_viewinchat_outlined" />
            </el-icon>
          </el-tooltip>
          <el-tooltip :effect="toolTip" placement="top" content="已设置下钻" v-if="hasDrillIcon">
            <el-icon :size="iconSize" class="inner-icon">
              <Icon name="icon_drilling_outlined" />
            </el-icon>
          </el-tooltip>
        </div>
      </transition>
    </div>
    <!--这里去渲染不同图库的图表-->
    <div v-if="chartAreaShow" style="flex: 1; overflow: hidden">
      <plugin-component
        v-if="view.plugin?.isPlugin"
        :jsname="view.plugin.staticMap['index']"
        :scale="scale"
        :dynamic-area-id="dynamicAreaId"
        :view="view"
        :show-position="showPosition"
        :element="element"
        :request="request"
        :emitter="emitter"
        :store="store"
        ref="chartComponent"
        @onChartClick="chartClick"
        @onPointClick="onPointClick"
        @onDrillFilters="onDrillFilters"
        @onJumpClick="jumpClick"
        @resetLoading="() => (loading = false)"
      />
      <de-rich-text-view
        v-else-if="showChartView(ChartLibraryType.RICH_TEXT)"
        :themes="canvasStyleData.dashboard.themeColor"
        ref="chartComponent"
        :element="element"
        :disabled="!['canvas', 'canvasDataV'].includes(showPosition) || disabled"
        :active="active"
        :show-position="showPosition"
      />
      <de-indicator
        :scale="scale"
        v-else-if="showChartView(ChartLibraryType.INDICATOR)"
        :themes="canvasStyleData.dashboard.themeColor"
        ref="chartComponent"
        :view="view"
        :show-position="showPosition"
      />
      <chart-component-g2-plot
        :scale="scale"
        :dynamic-area-id="dynamicAreaId"
        :view="view"
        :show-position="showPosition"
        :element="element"
        v-else-if="
          showChartView(ChartLibraryType.G2_PLOT, ChartLibraryType.L7_PLOT, ChartLibraryType.L7)
        "
        ref="chartComponent"
        @onChartClick="chartClick"
        @onPointClick="onPointClick"
        @onDrillFilters="onDrillFilters"
        @onJumpClick="jumpClick"
        @resetLoading="() => (loading = false)"
      />
      <chart-component-s2
        :view="view"
        :scale="scale"
        :show-position="showPosition"
        :element="element"
        :drill-length="drillClickLength"
        v-else-if="showChartView(ChartLibraryType.S2)"
        ref="chartComponent"
        @onPointClick="onPointClick"
        @onChartClick="chartClick"
        @onDrillFilters="onDrillFilters"
        @onJumpClick="jumpClick"
      />
    </div>
    <chart-empty-info
      v-if="!chartAreaShow"
      :themes="canvasStyleData.dashboard.themeColor"
      :view-icon="view.type"
    ></chart-empty-info>
    <drill-path :drill-filters="state.drillFilters" @onDrillJump="drillJump" />
    <XpackComponent
      ref="openHandler"
      jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvT3BlbkhhbmRsZXI="
    />
    <XpackComponent
      v-if="!pluginLoaded && view.isPlugin"
      jsname="L2NvbXBvbmVudC9wbHVnaW5zLWhhbmRsZXIvVmlld0NhdGVnb3J5SGFuZGxlcg=="
      @load-plugin-category="loadPluginCategory"
    />
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
.title-container {
  margin: 0;
  width: 100%;

  display: inline-flex;
  flex-wrap: nowrap;
  justify-content: center;

  gap: 8px;

  .icons-container {
    display: inline-flex;
    flex-direction: row;
    align-items: center;
    flex-wrap: nowrap;
    gap: 8px;

    color: #646a73;

    &.icons-container__dark {
      color: #a6a6a6;
    }

    &.is-editing {
      gap: 6px;
    }

    .inner-icon {
      cursor: pointer;
    }
  }
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
