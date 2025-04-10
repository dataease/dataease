<script setup lang="ts">
import { getData } from '@/api/chart'
import { ref, reactive, shallowRef, computed, CSSProperties, toRefs, PropType } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { customAttrTrans, customStyleTrans, recursionTransObj } from '@/utils/canvasStyle'
import { deepCopy, isMobile } from '@/utils/utils'
import { cloneDeep, defaultsDeep, defaultTo } from 'lodash-es'
import {
  BASE_VIEW_CONFIG,
  CHART_FONT_FAMILY_MAP,
  DEFAULT_INDICATOR_NAME_STYLE,
  DEFAULT_INDICATOR_STYLE
} from '@/views/chart/components/editor/util/chart'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { storeToRefs } from 'pinia'
import { isDashboard, trackBarStyleCheck } from '@/utils/canvasUtils'
import ViewTrackBar from '@/components/visualization/ViewTrackBar.vue'

const props = defineProps({
  // 公共参数集
  commonParams: {
    type: Object,
    required: false
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
  scale: {
    type: Number,
    required: false,
    default: 1
  },
  terminal: {
    type: String,
    default: 'pc'
  },
  //图表渲染id后缀
  suffixId: {
    type: String,
    required: false,
    default: 'common'
  },
  fontFamily: {
    type: String,
    required: false,
    default: 'inherit'
  }
})

const { view, scale, terminal, showPosition, commonParams } = toRefs(props)

const dvMainStore = dvMainStoreWithOut()
const dataVMobile = !isDashboard() && isMobile()
const { embeddedCallBack, nowPanelTrackInfo, nowPanelJumpInfo, mobileInPc, inMobile } =
  storeToRefs(dvMainStore)
const viewTrack = ref(null)
const indicatorRef = ref(null)
const errMsg = ref('')
const isError = ref(false)
const state = reactive({
  pointParam: null,
  data: null,
  loading: false,
  totalItems: 0,
  trackBarStyle: {
    position: 'absolute',
    left: '50%',
    top: '50%'
  }
})

const chartData = shallowRef<Partial<Chart['data']>>({
  fields: []
})

const resultObject = computed(() => {
  const list = chartData.value?.series
  if (list && list.length > 0) {
    return list[0]
  }
  return undefined
})

const resultName = computed(() => {
  if (view.value?.yAxis?.length) {
    const axis = view.value.yAxis[0]
    return axis.chartShowName ? axis.chartShowName : axis.name
  }
  return undefined
})

const result = computed(() => {
  const list = resultObject.value?.data
  let _result = undefined
  if (list && list.length > 0) {
    _result = list[0]
  }
  if (_result === null || _result === undefined) {
    if (view.value.senior && view.value.senior?.functionCfg?.emptyDataStrategy === 'setZero') {
      _result = 0
    } else {
      return '-'
    }
  }
  return _result
})

const indicatorColor = ref(DEFAULT_INDICATOR_STYLE.color)
const thresholdColor = computed(() => {
  let color: string = indicatorColor.value
  let backgroundColor: string = DEFAULT_INDICATOR_STYLE.backgroundColor
  if (result.value === '-') {
    return { color, backgroundColor }
  }
  const value = result.value
  if (
    view.value.senior &&
    view.value.senior.threshold?.enable &&
    view.value.senior.threshold?.labelThreshold?.length > 0
  ) {
    const senior = view.value.senior
    for (let i = 0; i < senior.threshold.labelThreshold.length; i++) {
      let flag = false
      const t = senior.threshold.labelThreshold[i]
      const tv = t.value
      if (t.term === 'eq') {
        if (value === tv) {
          color = t.color
          backgroundColor = t.backgroundColor
          flag = true
        }
      } else if (t.term === 'not_eq') {
        if (value !== tv) {
          color = t.color
          backgroundColor = t.backgroundColor
          flag = true
        }
      } else if (t.term === 'lt') {
        if (value < tv) {
          color = t.color
          backgroundColor = t.backgroundColor
          flag = true
        }
      } else if (t.term === 'gt') {
        if (value > tv) {
          color = t.color
          backgroundColor = t.backgroundColor
          flag = true
        }
      } else if (t.term === 'le') {
        if (value <= tv) {
          color = t.color
          backgroundColor = t.backgroundColor
          flag = true
        }
      } else if (t.term === 'ge') {
        if (value >= tv) {
          color = t.color
          backgroundColor = t.backgroundColor
          flag = true
        }
      } else if (t.term === 'between') {
        if (t.min <= value && value <= t.max) {
          color = t.color
          backgroundColor = t.backgroundColor
          flag = true
        }
      }
      if (flag) {
        break
      }
    }
  }
  return { color, backgroundColor }
})

const formattedResult = computed(() => {
  let _result = result.value

  if (_result === '-') {
    return _result
  }

  // 格式化
  if (view.value.yAxis && view.value.yAxis.length > 0 && view.value.yAxis[0].formatterCfg) {
    return valueFormatter(_result, view.value.yAxis[0].formatterCfg)
  }
  return _result
})

const emit = defineEmits([
  'onPointClick',
  'onChartClick',
  'onDrillFilters',
  'onJumpClick',
  'onComponentEvent'
])
const contentStyle = ref<CSSProperties>({
  display: 'flex',
  'flex-direction': 'column',
  'align-items': 'center',
  'justify-content': 'center',
  height: '100%',
  'background-color': thresholdColor.value.backgroundColor
})

const indicatorClass = ref<CSSProperties>({
  color: thresholdColor.value.color,
  'font-size': DEFAULT_INDICATOR_STYLE.fontSize + 'px',
  'font-family': defaultTo(
    props.fontFamily,
    CHART_FONT_FAMILY_MAP[DEFAULT_INDICATOR_STYLE.fontFamily]
  ),
  'font-weight': DEFAULT_INDICATOR_STYLE.isBolder ? 'bold' : 'normal',
  'font-style': DEFAULT_INDICATOR_STYLE.isItalic ? 'italic' : 'normal',
  'letter-spacing': DEFAULT_INDICATOR_STYLE.letterSpace + 'px',
  'text-shadow': DEFAULT_INDICATOR_STYLE.fontShadow ? '2px 2px 4px' : 'none',
  'font-synthesis': 'weight style'
})

const indicatorSuffixClass = ref<CSSProperties>({
  color: DEFAULT_INDICATOR_STYLE.suffixColor,
  'font-size': DEFAULT_INDICATOR_STYLE.suffixFontSize + 'px',
  'font-family': defaultTo(
    props.fontFamily,
    CHART_FONT_FAMILY_MAP[DEFAULT_INDICATOR_STYLE.fontFamily]
  ),
  'font-weight': DEFAULT_INDICATOR_STYLE.suffixIsBolder ? 'bold' : 'normal',
  'font-style': DEFAULT_INDICATOR_STYLE.suffixIsItalic ? 'italic' : 'normal',
  'letter-spacing': DEFAULT_INDICATOR_STYLE.suffixLetterSpace + 'px',
  'text-shadow': DEFAULT_INDICATOR_STYLE.suffixFontShadow ? '2px 2px 4px' : 'none',
  'font-synthesis': 'weight style'
})

const showSuffix = ref<boolean>(DEFAULT_INDICATOR_STYLE.suffixEnable)

const suffixContent = ref('')

const indicatorNameShow = ref(false)

const indicatorNameWrapperStyle = reactive<CSSProperties>({
  'margin-top': DEFAULT_INDICATOR_NAME_STYLE.nameValueSpacing + 'px'
})

const indicatorNameClass = ref<CSSProperties>({
  color: DEFAULT_INDICATOR_NAME_STYLE.color,
  'font-size': DEFAULT_INDICATOR_NAME_STYLE.fontSize + 'px',
  'font-family': defaultTo(
    props.fontFamily,
    CHART_FONT_FAMILY_MAP[DEFAULT_INDICATOR_STYLE.fontFamily]
  ),
  'font-weight': DEFAULT_INDICATOR_NAME_STYLE.isBolder ? 'bold' : 'normal',
  'font-style': DEFAULT_INDICATOR_NAME_STYLE.isItalic ? 'italic' : 'normal',
  'letter-spacing': DEFAULT_INDICATOR_NAME_STYLE.letterSpace + 'px',
  'text-shadow': DEFAULT_INDICATOR_NAME_STYLE.fontShadow ? '2px 2px 4px' : 'none',
  'font-synthesis': 'weight style'
})

const renderChart = async view => {
  if (!view) {
    return
  }

  const TEMP_DEFAULT_CHART = cloneDeep(BASE_VIEW_CONFIG)
  delete TEMP_DEFAULT_CHART.customAttr.basicStyle.alpha

  const chart = deepCopy({
    ...defaultsDeep(view, TEMP_DEFAULT_CHART),
    data: chartData.value
  }) as ChartObj

  recursionTransObj(customAttrTrans, chart.customAttr, scale.value, terminal.value)
  recursionTransObj(customStyleTrans, chart.customStyle, scale.value, terminal.value)

  if (chart.customAttr) {
    const { indicator, indicatorName } = chart.customAttr

    if (indicator) {
      switch (indicator.hPosition) {
        case 'left':
          contentStyle.value['align-items'] = 'flex-start'
          break
        case 'right':
          contentStyle.value['align-items'] = 'flex-end'
          break
        default:
          contentStyle.value['align-items'] = 'center'
      }
      switch (indicator.vPosition) {
        case 'top':
          contentStyle.value['justify-content'] = 'flex-start'
          break
        case 'bottom':
          contentStyle.value['justify-content'] = 'flex-end'
          break
        default:
          contentStyle.value['justify-content'] = 'center'
      }

      indicatorColor.value = indicator.color
      let suffixColor = indicator.suffixColor

      indicatorClass.value = {
        color: thresholdColor.value.color,
        'font-size': indicator.fontSize + 'px',
        'font-family': defaultTo(
          indicator.fontFamily,
          CHART_FONT_FAMILY_MAP[DEFAULT_INDICATOR_STYLE.fontFamily]
        ),
        'font-weight': indicator.isBolder ? 'bold' : 'normal',
        'font-style': indicator.isItalic ? 'italic' : 'normal',
        'letter-spacing': indicator.letterSpace + 'px',
        'text-shadow': indicator.fontShadow ? '2px 2px 4px' : 'none',
        'font-synthesis': 'weight style'
      }
      contentStyle.value['background-color'] = thresholdColor.value.backgroundColor

      indicatorSuffixClass.value = {
        color: suffixColor,
        'font-size': indicator.suffixFontSize + 'px',
        'font-family': defaultTo(
          indicator.suffixFontFamily,
          CHART_FONT_FAMILY_MAP[DEFAULT_INDICATOR_STYLE.suffixFontFamily]
        ),
        'font-weight': indicator.suffixIsBolder ? 'bold' : 'normal',
        'font-style': indicator.suffixIsItalic ? 'italic' : 'normal',
        'letter-spacing': indicator.suffixLetterSpace + 'px',
        'text-shadow': indicator.suffixFontShadow ? '2px 2px 4px' : 'none',
        'font-synthesis': 'weight style'
      }

      showSuffix.value = indicator.suffixEnable
      suffixContent.value = defaultTo(indicator.suffix, '')
    }
    if (indicatorName?.show) {
      let nameColor = indicatorName.color

      indicatorNameShow.value = true
      indicatorNameClass.value = {
        color: nameColor,
        'font-size': indicatorName.fontSize + 'px',
        'font-family': defaultTo(
          indicatorName.fontFamily,
          CHART_FONT_FAMILY_MAP[DEFAULT_INDICATOR_NAME_STYLE.fontFamily]
        ),
        'font-weight': indicatorName.isBolder ? 'bold' : 'normal',
        'font-style': indicatorName.isItalic ? 'italic' : 'normal',
        'letter-spacing': indicatorName.letterSpace + 'px',
        'text-shadow': indicatorName.fontShadow ? '2px 2px 4px' : 'none',
        'font-synthesis': 'weight style'
      }
      indicatorNameWrapperStyle['margin-top'] =
        (indicatorName.nameValueSpacing ?? DEFAULT_INDICATOR_NAME_STYLE.nameValueSpacing) + 'px'
    } else {
      indicatorNameShow.value = false
    }
  }
}

const calcData = (view, callback) => {
  if (view.tableId || view['dataFrom'] === 'template') {
    state.loading = true
    isError.value = false
    const v = JSON.parse(JSON.stringify(view))
    getData(v)
      .then(res => {
        if (res.code && res.code !== 0) {
          isError.value = true
          errMsg.value = res.msg
        } else {
          chartData.value = res?.data as Partial<Chart['data']>
          emit('onDrillFilters', res?.drillFilters)

          dvMainStore.setViewDataDetails(view.id, res)
          renderChart(res)
        }
        callback?.()
      })
      .catch(() => {
        callback?.()
      })
  } else {
    callback?.()
  }
}

const trackClick = trackAction => {
  const param = state.pointParam
  if (!param?.data?.dimensionList && !param?.data?.quotaList) {
    return
  }
  const linkageParam = {
    option: 'linkage',
    innerType: 'indicator',
    name: state.pointParam.data.name,
    viewId: view.value.id,
    dimensionList: state.pointParam.data.dimensionList,
    quotaList: state.pointParam.data.quotaList,
    customFilter: state.pointParam.data.customFilter
  }
  const jumpParam = {
    option: 'jump',
    innerType: 'indicator',
    name: state.pointParam.data.name,
    viewId: view.value.id,
    dimensionList: state.pointParam.data.dimensionList,
    quotaList: state.pointParam.data.quotaList,
    sourceType: state.pointParam.data.sourceType
  }

  const clickParams = {
    option: 'pointClick',
    innerType: 'indicator',
    name: state.pointParam.data.name,
    viewId: view.value.id,
    dimensionList: state.pointParam.data.dimensionList,
    quotaList: state.pointParam.data.quotaList,
    customFilter: state.pointParam.data.customFilter
  }

  switch (trackAction) {
    case 'pointClick':
      emit('onPointClick', clickParams)
      break
    case 'linkageAndDrill':
      dvMainStore.addViewTrackFilter(linkageParam)
      emit('onChartClick', param)
      break
    case 'drill':
      emit('onChartClick', param)
      break
    case 'linkage':
      dvMainStore.addViewTrackFilter(linkageParam)
      break
    case 'jump':
      if (mobileInPc.value && !inMobile.value) return
      emit('onJumpClick', jumpParam)
      break
    case 'event_jump':
    case 'event_download':
    case 'event_share':
    case 'event_fullScreen':
    case 'event_showHidden':
    case 'event_refreshDataV':
    case 'event_refreshView':
      emit('onComponentEvent', jumpParam)
      break
    default:
      break
  }
}

const trackMenu = computed(() => {
  let trackMenuInfo = []
  if (showPosition.value === 'viewDialog') {
    return trackMenuInfo
  }
  let linkageCount = 0
  let jumpCount = 0
  chartData.value?.fields?.forEach(item => {
    const sourceInfo = view.value.id + '#' + item.id
    if (nowPanelTrackInfo.value[sourceInfo]) {
      linkageCount++
    }
    if (nowPanelJumpInfo.value[sourceInfo]) {
      jumpCount++
    }
  })
  jumpCount &&
    view.value?.jumpActive &&
    (!mobileInPc.value || inMobile.value) &&
    trackMenuInfo.push('jump')
  linkageCount && view.value?.linkageActive && trackMenuInfo.push('linkage')
  view.value.drillFields.length && trackMenuInfo.push('drill')
  // 如果同时配置jump linkage drill 切配置联动时同时下钻 在实际只显示两个 '跳转' '联动和下钻'
  if (trackMenuInfo.length === 3 && props.element.actionSelection.linkageActive === 'auto') {
    trackMenuInfo = ['jump', 'linkageAndDrill']
  } else if (
    trackMenuInfo.length === 2 &&
    props.element.actionSelection.linkageActive === 'auto' &&
    !trackMenuInfo.includes('jump')
  ) {
    trackMenuInfo = ['linkageAndDrill']
  }
  if (commonParams.value?.eventEnable) {
    trackMenuInfo.push('event_' + commonParams.value?.eventType)
  }
  return trackMenuInfo
})

const showCursor = computed(() => {
  return trackMenu.value.length || embeddedCallBack.value === 'yes'
})

const pointClickTrans = () => {
  if (embeddedCallBack.value === 'yes') {
    trackClick('pointClick')
  }
}

const action = param => {
  state.pointParam = param
  // 点击
  pointClickTrans()
  // 联动 跳转
  if (trackMenu.value.length < 2) {
    // 只有一个事件直接调用
    trackClick(trackMenu.value[0])
  } else {
    setTimeout(() => {
      const barStyleTemp = {
        left: param.x - 50,
        top: param.y + 10
      }
      trackBarStyleCheck(props.element, barStyleTemp, props.scale, trackMenu.value.length)
      if (dataVMobile) {
        state.trackBarStyle.left = barStyleTemp.left + 40 + 'px'
        state.trackBarStyle.top = barStyleTemp.top + 70 + 'px'
      } else {
        state.trackBarStyle.left = barStyleTemp.left + 'px'
        state.trackBarStyle.top = barStyleTemp.top + 'px'
      }
      viewTrack.value.trackButtonClick()
    }, 200)
  }
}

const onPointClick = event => {
  if (view.value?.yAxis?.length) {
    const axis = view.value.yAxis[0]
    // 获取鼠标的全局坐标
    const mouseX = event.clientX
    const mouseY = event.clientY

    // 获取最外层 div 的偏移量
    const rect = indicatorRef.value.getBoundingClientRect()
    const offsetX = rect.left
    const offsetY = rect.top

    // 计算鼠标相对于最外层 div 的坐标
    const left = mouseX - offsetX
    let top = mouseY - offsetY
    // 模拟点击
    const params = {
      x: left,
      y: top,
      data: {
        name: axis.name,
        dimensionList: view.value.xAxis,
        quotaList: view.value.yAxis,
        customFilter: view.value.customFilter
      }
    }
    action(params)
  }
}

defineExpose({
  calcData,
  renderChart
})
</script>

<template>
  <div
    ref="indicatorRef"
    :class="{ 'menu-point': showCursor }"
    :style="contentStyle"
    @mousedown="onPointClick"
  >
    <view-track-bar
      ref="viewTrack"
      :track-menu="trackMenu"
      :font-family="fontFamily"
      class="track-bar"
      :style="state.trackBarStyle"
      @trackClick="trackClick"
      :is-data-v-mobile="dataVMobile"
    />
    <div>
      <span :style="indicatorClass">{{ formattedResult }}</span>
      <span :style="indicatorSuffixClass" v-if="showSuffix">{{ suffixContent }}</span>
    </div>
    <div :style="indicatorNameWrapperStyle" v-if="indicatorNameShow">
      <span :style="indicatorNameClass">{{ resultName }}</span>
    </div>
  </div>
</template>

<style scoped lang="less">
.menu-point {
  cursor: pointer;
}
</style>
