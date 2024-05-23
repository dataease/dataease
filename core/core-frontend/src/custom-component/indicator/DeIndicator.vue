<script setup lang="ts">
import { getData } from '@/api/chart'
import { ref, reactive, shallowRef, computed, CSSProperties, toRefs, PropType } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { customAttrTrans, customStyleTrans, recursionTransObj } from '@/utils/canvasStyle'
import { deepCopy } from '@/utils/utils'
import { cloneDeep, defaultsDeep, defaultTo } from 'lodash-es'
import {
  BASE_VIEW_CONFIG,
  CHART_FONT_FAMILY_MAP,
  DEFAULT_INDICATOR_NAME_STYLE,
  DEFAULT_INDICATOR_STYLE
} from '@/views/chart/components/editor/util/chart'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { hexColorToRGBA } from '@/views/chart/components/js/util'
import { storeToRefs } from 'pinia'

const props = defineProps({
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
  }
})

const { view, scale, terminal } = toRefs(props)

const dvMainStore = dvMainStoreWithOut()

const { batchOptStatus } = storeToRefs(dvMainStore)

const errMsg = ref('')
const isError = ref(false)
const state = reactive({
  data: null,
  loading: false,
  totalItems: 0
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

const emit = defineEmits(['onChartClick', 'onDrillFilters', 'onJumpClick'])

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
    CHART_FONT_FAMILY_MAP[DEFAULT_INDICATOR_STYLE.fontFamily],
    DEFAULT_INDICATOR_STYLE.fontFamily
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
    CHART_FONT_FAMILY_MAP[DEFAULT_INDICATOR_STYLE.suffixFontFamily],
    DEFAULT_INDICATOR_STYLE.suffixFontFamily
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
    CHART_FONT_FAMILY_MAP[DEFAULT_INDICATOR_NAME_STYLE.fontFamily],
    DEFAULT_INDICATOR_NAME_STYLE.fontFamily
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
    const { indicator, indicatorName, basicStyle } = chart.customAttr

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

      if (basicStyle?.alpha !== undefined && !batchOptStatus.value) {
        indicatorColor.value = hexColorToRGBA(basicStyle.colors[0], basicStyle.alpha)
        suffixColor = hexColorToRGBA(basicStyle.colors[1], basicStyle.alpha)
      }

      indicatorClass.value = {
        color: thresholdColor.value.color,
        'font-size': indicator.fontSize + 'px',
        'font-family': defaultTo(
          CHART_FONT_FAMILY_MAP[indicator.fontFamily],
          DEFAULT_INDICATOR_STYLE.fontFamily
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
          CHART_FONT_FAMILY_MAP[indicator.suffixFontFamily],
          DEFAULT_INDICATOR_STYLE.suffixFontFamily
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

      if (basicStyle?.alpha !== undefined && !batchOptStatus.value) {
        nameColor = hexColorToRGBA(basicStyle.colors[2], basicStyle.alpha)
      }

      indicatorNameShow.value = true
      indicatorNameClass.value = {
        color: nameColor,
        'font-size': indicatorName.fontSize + 'px',
        'font-family': defaultTo(
          CHART_FONT_FAMILY_MAP[indicatorName.fontFamily],
          DEFAULT_INDICATOR_NAME_STYLE.fontFamily
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

          dvMainStore.setViewDataDetails(view.id, chartData.value)
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

defineExpose({
  calcData,
  renderChart
})
</script>

<template>
  <div :style="contentStyle">
    <div>
      <span :style="indicatorClass">{{ formattedResult }}</span>
      <span :style="indicatorSuffixClass" v-if="showSuffix">{{ suffixContent }}</span>
    </div>
    <div :style="indicatorNameWrapperStyle" v-if="indicatorNameShow">
      <span :style="indicatorNameClass">{{ resultName }}</span>
    </div>
  </div>
</template>

<style scoped lang="less"></style>
