<script lang="ts" setup>
import { computed, onMounted, PropType, reactive, ref, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_LABEL } from '@/views/chart/components/editor/util/chart'
import { ElIcon, ElSpace } from 'element-plus-secondary'
import { formatterType, unitType } from '../../../js/formatter'
import { defaultsDeep, cloneDeep, intersection, union, defaultTo, map } from 'lodash-es'
import { includesAny } from '../../util/StringUtils'
import { fieldType } from '@/utils/attr'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import Icon from '../../../../../../components/icon-custom/src/Icon.vue'
import { useEmitt } from '@/hooks/web/useEmitt'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object as PropType<ChartObj>,
    required: true
  },
  dimensionData: {
    type: Array<any>,
    required: false
  },
  quotaData: {
    type: Array<any>,
    required: false
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  },
  allFields: {
    type: Array<any>,
    required: false
  },
  propertyInner: {
    type: Array<string>
  }
})
const dvMainStore = dvMainStoreWithOut()
const toolTip = computed(() => {
  return props.themes === 'dark' ? 'ndark' : 'dark'
})
const changeDataset = () => {
  if (showProperty('showFields')) {
    state.labelForm.showFields = []
    emit('onLabelChange', { data: state.labelForm }, 'showFields')
  }
}
const { batchOptStatus } = storeToRefs(dvMainStore)
watch(
  [() => props.chart.customAttr.label, () => props.chart.customAttr.label.show],
  () => {
    init()
  },
  { deep: false }
)
const yAxis = computed(() => {
  if (props.chart.type.includes('chart-mix')) {
    return union(
      defaultTo(
        map(props.chart.yAxis, y => {
          return { ...y, axisType: 'yAxis', seriesId: y.id + '-yAxis' }
        }),
        []
      ),
      defaultTo(
        map(props.chart.yAxisExt, y => {
          return { ...y, axisType: 'yAxisExt', seriesId: y.id + '-yAxisExt' }
        }),
        []
      )
    )
  } else {
    return defaultTo(
      map(props.chart.yAxis, y => {
        return { ...y, axisType: 'yAxis', seriesId: y.id + '-yAxis' }
      }),
      []
    )
  }
})

const yAxisIds = computed(() => {
  return map(yAxis.value, y => y.seriesId)
})

watch(
  [() => yAxisIds.value, () => props.chart.type],
  () => {
    initSeriesLabel()
  },
  { deep: true }
)

const computedIdKey = computed(() => {
  if (props.chart.type.includes('chart-mix')) {
    return 'seriesId'
  }
  return 'id'
})

const curSeriesFormatter = ref<Partial<SeriesFormatter>>({})
const formatterEditable = computed(() => {
  return showProperty('seriesLabelFormatter') && yAxis.value?.length
})
const formatterSelector = ref()
// 初始化系列标签
const initSeriesLabel = () => {
  // 批量设置阶段 没有此标签
  if (!showProperty('seriesLabelFormatter') || batchOptStatus.value) {
    return
  }
  const formatter = state.labelForm.seriesLabelFormatter

  const seriesAxisMap = formatter.reduce((pre, next) => {
    const id = next.seriesId ?? next.id
    pre[next[computedIdKey.value]] = { ...next, seriesId: id }
    return pre
  }, {})
  formatter.splice(0, formatter.length)
  if (!yAxis.value.length) {
    curSeriesFormatter.value = {}
    return
  }
  let initFlag = false
  const themeColor = dvMainStore.canvasStyleData.dashboard.themeColor
  const axisMap = yAxis.value.reduce((pre, next) => {
    const optionLabel: string = `${next.chartShowName ?? next.name}${
      next.summary !== '' ? '(' + t('chart.' + next.summary) + ')' : ''
    }${
      props.chart.type.includes('chart-mix')
        ? next.axisType === 'yAxis'
          ? '(左轴)'
          : '(右轴)'
        : ''
    }` as string
    const optionShowName: string = `${next.chartShowName ?? next.name}${
      next.summary !== '' ? '(' + t('chart.' + next.summary) + ')' : ''
    }${
      props.chart.type.includes('chart-mix')
        ? next.axisType === 'yAxis'
          ? '(左轴)'
          : '(右轴)'
        : ''
    }` as string
    let tmp = {
      ...next,
      optionLabel: optionLabel,
      optionShowName: optionShowName,
      show: true,
      color: themeColor === 'dark' ? '#fff' : '#000',
      fontSize: COMPUTED_DEFAULT_LABEL.value.fontSize,
      showExtremum: false
    } as SeriesFormatter
    if (seriesAxisMap[next[computedIdKey.value]]) {
      tmp = {
        ...tmp,
        formatterCfg: seriesAxisMap[next[computedIdKey.value]].formatterCfg,
        show: seriesAxisMap[next[computedIdKey.value]].show,
        color: seriesAxisMap[next[computedIdKey.value]].color,
        fontSize: seriesAxisMap[next[computedIdKey.value]].fontSize,
        showExtremum: seriesAxisMap[next[computedIdKey.value]].showExtremum
      }
    } else {
      initFlag = true
    }
    formatter.push(tmp)
    next.seriesId = next.seriesId ?? next.id
    pre[next[computedIdKey.value]] = tmp
    return pre
  }, {})
  // 初始化一下序列数组，用于主题适配
  if (initFlag) {
    changeLabelAttr('seriesLabelFormatter', false)
  }
  if (!curSeriesFormatter.value || !axisMap[curSeriesFormatter.value[computedIdKey.value]]) {
    curSeriesFormatter.value = axisMap[formatter[0][computedIdKey.value]]
    return
  }
  curSeriesFormatter.value = axisMap[curSeriesFormatter.value[computedIdKey.value]]
}

const labelPositionR = [
  { name: t('chart.inside'), value: 'inner' },
  { name: t('chart.outside'), value: 'outer' }
]
const labelPositionH = [
  { name: t('chart.text_pos_left'), value: 'left' },
  { name: t('chart.center'), value: 'middle' },
  { name: t('chart.text_pos_right'), value: 'right' }
]
const labelPositionV = [
  { name: t('chart.text_pos_top'), value: 'top' },
  { name: t('chart.center'), value: 'middle' },
  { name: t('chart.text_pos_bottom'), value: 'bottom' }
]

const chartType = computed(() => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  return chart?.type
})

const fontSizeList = computed(() => {
  const arr = []
  for (let i = 10; i <= 40; i = i + 2) {
    if (i === 10 && chartType.value === 'liquid') {
      continue
    }
    arr.push({
      name: i + '',
      value: i
    })
  }
  return arr
})

const COMPUTED_DEFAULT_LABEL = computed(() => {
  if (chartType.value === 'liquid') {
    return {
      ...DEFAULT_LABEL,
      fontSize: fontSizeList.value[0].value
    }
  }
  return DEFAULT_LABEL
})

const state = reactive<{ labelForm: ChartLabelAttr | any }>({
  labelForm: {
    quotaLabelFormatter: DEFAULT_LABEL.quotaLabelFormatter,
    seriesLabelFormatter: [],
    labelFormatter: DEFAULT_LABEL.labelFormatter
  }
})

const emit = defineEmits(['onLabelChange'])
const changeLabelAttr = (prop: string, render = true) => {
  emit('onLabelChange', { data: state.labelForm, render }, prop)
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customAttr) {
    const customAttr = chart.customAttr
    if (customAttr.label) {
      state.labelForm = defaultsDeep(customAttr.label, cloneDeep(COMPUTED_DEFAULT_LABEL.value))
      if (chartType.value === 'liquid' && state.labelForm.fontSize < fontSizeList.value[0].value) {
        state.labelForm.fontSize = fontSizeList.value[0].value
      }
      initSeriesLabel()
      formatterSelector.value?.blur()
    }
  }
}
const checkLabelContent = contentProp => {
  const propIntersection = intersection(props.propertyInner, [
    'showDimension',
    'showQuota',
    'showProportion'
  ])
  if (!propIntersection?.includes(contentProp)) {
    return false
  }
  let trueCount = 0
  propIntersection?.forEach(prop => {
    state.labelForm?.[prop] && trueCount++
  })
  return trueCount === 1 && state.labelForm?.[contentProp]
}
const showProperty = prop => {
  return props.propertyInner?.includes(prop)
}

const showEmpty = computed(() => {
  return (
    props.propertyInner.length === 0 ||
    (batchOptStatus.value && showProperty('seriesLabelFormatter'))
  )
})
const showSeriesLabelFormatter = computed(() => {
  return !batchOptStatus.value && showProperty('seriesLabelFormatter')
})
const showDivider = computed(() => {
  const DIVIDER_PROPS = ['labelFormatter', 'showDimension', 'showQuota', 'showProportion']
  return (
    includesAny(props.propertyInner, ...DIVIDER_PROPS) && !isBarRangeTime.value && !isGroupBar.value
  )
})

const isBarRangeTime = computed<boolean>(() => {
  if (props.chart.type === 'bar-range') {
    const tempYAxis = props.chart.yAxis[0]
    const tempYAxisExt = props.chart.yAxisExt[0]
    if (
      (tempYAxis && tempYAxis.groupType === 'd') ||
      (tempYAxisExt && tempYAxisExt.groupType === 'd')
    ) {
      return true
    }
  }
  return false
})
const showPositionH = computed(() => {
  if (showProperty('hPosition')) {
    if (props.chart.type !== 'bidirectional-bar') {
      return true
    }
    return props.chart.customAttr.basicStyle.layout === 'horizontal'
  }
  return false
})
const showPositionV = computed(() => {
  if (showProperty('vPosition')) {
    if (props.chart.type !== 'bidirectional-bar' && props.chart.type !== 'bar-group') {
      return true
    }
    return props.chart.customAttr.basicStyle.layout === 'vertical'
  }
  return false
})
watch(
  () => props.chart.customAttr.basicStyle.layout,
  () => {
    const layout = props.chart.customAttr.basicStyle.layout
    if (chartType.value === 'bidirectional-bar') {
      if (layout === 'horizontal') {
        if (state?.labelForm?.position === 'top') {
          state.labelForm.position = 'right'
        }
        if (state?.labelForm?.position === 'bottom') {
          state.labelForm.position = 'left'
        }
      }
      if (layout === 'vertical') {
        if (state?.labelForm?.position === 'left') {
          state.labelForm.position = 'bottom'
        }
        if (state?.labelForm?.position === 'right') {
          state.labelForm.position = 'top'
        }
      }
      changeLabelAttr('position')
    }
  },
  { deep: true }
)

const allFields = computed(() => {
  return defaultTo(props.allFields, []).map(item => ({
    key: item.dataeaseName,
    name: item.name,
    value: `${item.dataeaseName}@${item.name}`,
    disabled: false
  }))
})

const defaultPlaceholder = computed(() => {
  if (state.labelForm.showFields && state.labelForm.showFields.length > 0) {
    return state.labelForm.showFields
      .map(field => {
        return '${' + field.split('@')[1] + '}'
      })
      .join(',')
  }
  return ''
})
onMounted(() => {
  init()
  useEmitt({ name: 'dataset-change', callback: changeDataset })
})
const isGroupBar = computed(() => {
  return props.chart.type === 'bar-group'
})
</script>

<template>
  <el-form
    ref="labelForm"
    :disabled="!state.labelForm.show"
    :model="state.labelForm"
    label-position="top"
  >
    <el-row v-show="showEmpty" style="margin-bottom: 12px"> 无其他可设置的属性</el-row>
    <div v-if="!isGroupBar">
      <el-space>
        <el-form-item
          class="form-item"
          :class="'form-item-' + themes"
          v-if="showProperty('color')"
          :label="t('chart.text')"
        >
          <el-color-picker
            :effect="themes"
            v-model="state.labelForm.color"
            class="color-picker-style"
            :predefine="COLOR_PANEL"
            @change="changeLabelAttr('color')"
            is-custom
          />
        </el-form-item>
        <el-form-item
          class="form-item"
          :class="'form-item-' + themes"
          v-if="showProperty('fontSize')"
        >
          <template #label>&nbsp;</template>
          <el-tooltip content="字号" :effect="toolTip" placement="top">
            <el-select
              size="small"
              style="width: 108px"
              :effect="themes"
              v-model.number="state.labelForm.fontSize"
              :placeholder="t('chart.text_fontsize')"
              @change="changeLabelAttr('fontSize')"
            >
              <el-option
                v-for="option in fontSizeList"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-tooltip>
        </el-form-item>
      </el-space>
    </div>
    <div v-if="showProperty('showFields') && !batchOptStatus">
      <el-form-item :label="t('chart.label')" class="form-item" :class="'form-item-' + themes">
        <el-select
          size="small"
          :effect="themes"
          filterable
          multiple
          collapse-tags
          collapse-tags-tooltip
          v-model="state.labelForm.showFields"
          @change="changeLabelAttr('showFields')"
        >
          <el-option
            v-for="option in allFields"
            :key="option.key"
            :label="option.name"
            :value="option.value"
            :disabled="option.disabled"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="showProperty('customContent')" :class="'form-item-' + themes">
        <template #label>
          <span class="data-area-label">
            <span style="margin-right: 4px">
              {{ t('chart.content_formatter') }}
            </span>
            <el-tooltip class="item" :effect="toolTip" placement="bottom">
              <template #content>
                <div>可以${fieldName}的形式读取字段值（不支持换行）</div>
              </template>
              <el-icon class="hint-icon" :class="{ 'hint-icon--dark': themes === 'dark' }">
                <Icon name="icon_info_outlined" />
              </el-icon>
            </el-tooltip>
          </span>
        </template>
        <el-input
          style="font-size: smaller; font-weight: normal"
          v-model="state.labelForm.customContent"
          :effect="themes"
          type="textarea"
          :autosize="{ minRows: 4, maxRows: 4 }"
          :placeholder="defaultPlaceholder"
          @blur="changeLabelAttr('customContent')"
        />
      </el-form-item>
    </div>
    <el-form-item
      v-if="showProperty('rPosition')"
      :label="t('chart.label')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-select
        size="small"
        :effect="themes"
        v-model="state.labelForm.position"
        :placeholder="t('chart.label_position')"
        @change="changeLabelAttr('position')"
      >
        <el-option
          v-for="option in labelPositionR"
          :key="option.value"
          :label="option.name"
          :value="option.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item
      v-if="showPositionH"
      :label="t('chart.label_position')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-select
        size="small"
        :effect="themes"
        v-model="state.labelForm.position"
        :placeholder="t('chart.label_position')"
        @change="changeLabelAttr('position')"
      >
        <el-option
          v-for="option in labelPositionH"
          :key="option.value"
          :label="option.name"
          :value="option.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item v-if="showPositionV" class="form-item" :class="'form-item-' + themes">
      <template #label>
        {{ t('chart.label_position') }}
        <el-tooltip
          class="item"
          :effect="toolTip"
          placement="top"
          v-if="chart.type.includes('chart-mix')"
        >
          <template #content>
            <span v-html="t('chart.chart_mix_label_only_left')"></span>
          </template>
          <span style="vertical-align: middle">
            <el-icon style="cursor: pointer">
              <Icon name="icon_info_outlined" />
            </el-icon>
          </span>
        </el-tooltip>
      </template>
      <el-select
        size="small"
        :effect="themes"
        v-model="state.labelForm.position"
        :placeholder="t('chart.label_position')"
        @change="changeLabelAttr('position')"
      >
        <el-option
          v-for="option in labelPositionV"
          :key="option.value"
          :label="option.name"
          :value="option.value"
        />
      </el-select>
    </el-form-item>
    <el-divider
      class="m-divider"
      :class="{ 'divider-dark': themes === 'dark' }"
      v-if="showDivider"
    />
    <template v-if="showProperty('labelFormatter') && !isBarRangeTime && !isGroupBar">
      <el-form-item
        :label="$t('chart.value_formatter_type')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-select
          size="small"
          :effect="themes"
          v-model="state.labelForm.labelFormatter.type"
          @change="changeLabelAttr('labelFormatter.type')"
        >
          <el-option
            v-for="type in formatterType"
            :key="type.value"
            :label="$t('chart.' + type.name)"
            :value="type.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="state.labelForm.labelFormatter && state.labelForm.labelFormatter.type !== 'auto'"
        :label="$t('chart.value_formatter_decimal_count')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-input-number
          controls-position="right"
          :effect="themes"
          v-model="state.labelForm.labelFormatter.decimalCount"
          :precision="0"
          :min="0"
          :max="10"
          @change="changeLabelAttr('labelFormatter.decimalCount')"
        />
      </el-form-item>

      <el-row
        :gutter="8"
        v-if="state.labelForm.labelFormatter && state.labelForm.labelFormatter.type !== 'percent'"
      >
        <el-col :span="12">
          <el-form-item
            :label="$t('chart.value_formatter_unit')"
            class="form-item"
            :class="'form-item-' + themes"
          >
            <el-select
              size="small"
              :effect="themes"
              v-model="state.labelForm.labelFormatter.unit"
              :placeholder="$t('chart.pls_select_field')"
              @change="changeLabelAttr('labelFormatter.unit')"
            >
              <el-option
                v-for="item in unitType"
                :key="item.value"
                :label="$t('chart.' + item.name)"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
            :label="$t('chart.value_formatter_suffix')"
            class="form-item"
            :class="'form-item-' + themes"
          >
            <el-input
              :effect="themes"
              v-model="state.labelForm.labelFormatter.suffix"
              clearable
              :placeholder="$t('commons.input_content')"
              @change="changeLabelAttr('labelFormatter.suffix')"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item class="form-item" :class="'form-item-' + themes">
        <el-checkbox
          size="small"
          :effect="themes"
          v-model="state.labelForm.labelFormatter.thousandSeparator"
          @change="changeLabelAttr('labelFormatter.thousandSeparator')"
          :label="t('chart.value_formatter_thousand_separator')"
        />
      </el-form-item>
    </template>

    <el-form-item
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('showDimension')"
    >
      <el-checkbox
        v-model="state.labelForm.showDimension"
        :effect="themes"
        :disabled="checkLabelContent('showDimension')"
        size="small"
        label="dimension"
        @change="changeLabelAttr('showDimension')"
      >
        {{ t('chart.dimension') }}
      </el-checkbox>
    </el-form-item>
    <template v-if="showProperty('showQuota')">
      <el-form-item class="form-item form-item-checkbox" :class="'form-item-' + themes">
        <el-checkbox
          v-model="state.labelForm.showQuota"
          :effect="themes"
          :disabled="checkLabelContent('showQuota')"
          size="small"
          label="quota"
          @change="changeLabelAttr('showQuota')"
        >
          {{ t('chart.quota') }}
        </el-checkbox>
      </el-form-item>

      <div style="padding-left: 22px">
        <el-form-item
          :label="t('chart.value_formatter_type')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-select
            size="small"
            :disabled="!state.labelForm.showQuota"
            style="width: 100%"
            :effect="themes"
            v-model="state.labelForm.quotaLabelFormatter.type"
            @change="changeLabelAttr('quotaLabelFormatter.type')"
          >
            <el-option
              v-for="type in formatterType"
              :key="type.value"
              :label="t('chart.' + type.name)"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="
            state.labelForm.quotaLabelFormatter &&
            state.labelForm.quotaLabelFormatter.type !== 'auto'
          "
          :label="t('chart.value_formatter_decimal_count')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-input-number
            controls-position="right"
            :disabled="!state.labelForm.showQuota"
            style="width: 100%"
            :effect="themes"
            v-model="state.labelForm.quotaLabelFormatter.decimalCount"
            :precision="0"
            :min="0"
            :max="10"
            size="small"
            @change="changeLabelAttr('quotaLabelFormatter.decimalCount')"
          />
        </el-form-item>

        <el-row
          :gutter="8"
          v-if="
            state.labelForm.quotaLabelFormatter &&
            state.labelForm.quotaLabelFormatter.type !== 'percent'
          "
        >
          <el-col :span="12">
            <el-form-item
              :label="t('chart.value_formatter_unit')"
              class="form-item"
              :class="'form-item-' + themes"
            >
              <el-select
                :disabled="!state.labelForm.showQuota"
                :effect="themes"
                v-model="state.labelForm.quotaLabelFormatter.unit"
                :placeholder="t('chart.pls_select_field')"
                size="small"
                @change="changeLabelAttr('quotaLabelFormatter.unit')"
              >
                <el-option
                  v-for="item in unitType"
                  :key="item.value"
                  :label="t('chart.' + item.name)"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              :label="t('chart.value_formatter_suffix')"
              class="form-item"
              :class="'form-item-' + themes"
            >
              <el-input
                :disabled="!state.labelForm.showQuota"
                :effect="themes"
                v-model="state.labelForm.quotaLabelFormatter.suffix"
                size="small"
                clearable
                :placeholder="t('commons.input_content')"
                @change="changeLabelAttr('quotaLabelFormatter.suffix')"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-checkbox
            :disabled="!state.labelForm.showQuota"
            size="small"
            :effect="themes"
            v-model="state.labelForm.quotaLabelFormatter.thousandSeparator"
            @change="changeLabelAttr('quotaLabelFormatter.thousandSeparator')"
            :label="t('chart.value_formatter_thousand_separator')"
          />
        </el-form-item>
      </div>
    </template>
    <template v-if="showProperty('showProportion')">
      <el-form-item class="form-item form-item-checkbox" :class="'form-item-' + themes">
        <el-checkbox
          v-model="state.labelForm.showProportion"
          :effect="themes"
          :disabled="checkLabelContent('showProportion')"
          size="small"
          label="proportion"
          @change="changeLabelAttr('showProportion')"
        >
          {{ t('chart.proportion') }}
        </el-checkbox>
      </el-form-item>
      <div style="padding-left: 22px">
        <el-form-item
          :label="t('chart.label_reserve_decimal_count')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-select
            size="small"
            :effect="themes"
            :disabled="!state.labelForm.showProportion"
            v-model="state.labelForm.reserveDecimalCount"
            @change="changeLabelAttr('reserveDecimalCount')"
          >
            <el-option :label="t('chart.reserve_zero')" :value="0" />
            <el-option :label="t('chart.reserve_one')" :value="1" />
            <el-option :label="t('chart.reserve_two')" :value="2" />
          </el-select>
        </el-form-item>
      </div>
    </template>
    <el-form-item
      v-if="showProperty('reserveDecimalCount')"
      :label="t('chart.label_reserve_decimal_count')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-select
        size="small"
        :effect="themes"
        v-model="state.labelForm.reserveDecimalCount"
        @change="changeLabelAttr('reserveDecimalCount')"
      >
        <el-option :label="t('chart.reserve_zero')" :value="0" />
        <el-option :label="t('chart.reserve_one')" :value="1" />
        <el-option :label="t('chart.reserve_two')" :value="2" />
      </el-select>
    </el-form-item>
    <div v-if="showSeriesLabelFormatter">
      <el-form-item class="form-item" :class="'form-item-' + themes">
        <el-select
          v-model="curSeriesFormatter"
          :effect="themes"
          :teleported="false"
          :disabled="!formatterEditable"
          ref="formatterSelector"
          :value-key="computedIdKey"
          class="series-select"
          size="small"
        >
          <template #prefix>
            <el-icon v-if="curSeriesFormatter[computedIdKey]" style="font-size: 14px">
              <Icon
                :className="`field-icon-${fieldType[curSeriesFormatter.deType]}`"
                :name="`field_${fieldType[curSeriesFormatter.deType]}`"
              />
            </el-icon>
          </template>
          <template v-for="item in state.labelForm.seriesLabelFormatter" :key="item[computedIdKey]">
            <el-option class="series-select-option" :value="item" :label="item.optionLabel">
              <el-icon style="margin-right: 8px">
                <Icon
                  :className="`field-icon-${fieldType[item.deType]}`"
                  :name="`field_${fieldType[item.deType]}`"
                />
              </el-icon>
              {{ item.optionShowName }}
            </el-option>
          </template>
        </el-select>
      </el-form-item>
      <template v-if="curSeriesFormatter?.id">
        <el-form-item class="form-item form-item-checkbox" :class="'form-item-' + themes">
          <el-checkbox
            :effect="themes"
            size="small"
            @change="changeLabelAttr('seriesLabelFormatter')"
            v-model="curSeriesFormatter.show"
            label="quota"
          >
            {{ t('chart.label') + t('chart.show') }}
          </el-checkbox>
        </el-form-item>

        <div style="padding-left: 22px">
          <el-space>
            <el-form-item class="form-item" :class="'form-item-' + themes" :label="t('chart.text')">
              <el-color-picker
                :disabled="!curSeriesFormatter.show"
                style="width: 100%"
                :effect="themes"
                v-model="curSeriesFormatter.color"
                class="color-picker-style"
                :predefine="COLOR_PANEL"
                @change="changeLabelAttr('seriesLabelFormatter')"
                is-custom
              />
            </el-form-item>
            <el-form-item class="form-item" :class="'form-item-' + themes">
              <template #label>&nbsp;</template>
              <el-tooltip content="字号" :effect="toolTip" placement="top">
                <el-select
                  size="small"
                  :disabled="!curSeriesFormatter.show"
                  style="width: 108px"
                  :effect="themes"
                  v-model.number="curSeriesFormatter.fontSize"
                  :placeholder="t('chart.text_fontsize')"
                  @change="changeLabelAttr('seriesLabelFormatter')"
                >
                  <el-option
                    v-for="option in fontSizeList"
                    :key="option.value"
                    :label="option.name"
                    :value="option.value"
                  />
                </el-select>
              </el-tooltip>
            </el-form-item>
          </el-space>

          <el-form-item
            :label="t('chart.value_formatter_type')"
            class="form-item"
            :class="'form-item-' + themes"
            v-if="curSeriesFormatter.formatterCfg"
          >
            <el-select
              size="small"
              :disabled="!curSeriesFormatter.show"
              style="width: 100%"
              :effect="props.themes"
              v-model="curSeriesFormatter.formatterCfg.type"
              @change="changeLabelAttr('seriesLabelFormatter')"
            >
              <el-option
                v-for="type in formatterType"
                :key="type.value"
                :label="t('chart.' + type.name)"
                :value="type.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            v-if="
              curSeriesFormatter.formatterCfg && curSeriesFormatter.formatterCfg.type !== 'auto'
            "
            :label="t('chart.value_formatter_decimal_count')"
            class="form-item"
            :class="'form-item-' + themes"
          >
            <el-input-number
              controls-position="right"
              :disabled="!curSeriesFormatter.show"
              style="width: 100%"
              :effect="props.themes"
              v-model="curSeriesFormatter.formatterCfg.decimalCount"
              :precision="0"
              :min="0"
              :max="10"
              size="small"
              @change="changeLabelAttr('seriesLabelFormatter')"
            />
          </el-form-item>

          <el-row
            :gutter="8"
            v-if="
              curSeriesFormatter.show &&
              curSeriesFormatter.formatterCfg &&
              curSeriesFormatter.formatterCfg.type !== 'percent'
            "
          >
            <el-col :span="12">
              <el-form-item
                :label="t('chart.value_formatter_unit')"
                class="form-item"
                :class="'form-item-' + themes"
              >
                <el-select
                  :disabled="!curSeriesFormatter.show"
                  :effect="props.themes"
                  v-model="curSeriesFormatter.formatterCfg.unit"
                  :placeholder="t('chart.pls_select_field')"
                  size="small"
                  @change="changeLabelAttr('seriesLabelFormatter')"
                >
                  <el-option
                    v-for="item in unitType"
                    :key="item.value"
                    :label="t('chart.' + item.name)"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item
                :label="t('chart.value_formatter_suffix')"
                class="form-item"
                :class="'form-item-' + themes"
              >
                <el-input
                  :disabled="!curSeriesFormatter.show"
                  :effect="props.themes"
                  v-model="curSeriesFormatter.formatterCfg.suffix"
                  size="small"
                  clearable
                  :placeholder="t('commons.input_content')"
                  @change="changeLabelAttr('seriesLabelFormatter')"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item class="form-item" :class="'form-item-' + themes">
            <el-checkbox
              :disabled="!curSeriesFormatter.show"
              size="small"
              :effect="props.themes"
              v-model="curSeriesFormatter.formatterCfg.thousandSeparator"
              @change="changeLabelAttr('seriesLabelFormatter')"
              :label="t('chart.value_formatter_thousand_separator')"
            />
          </el-form-item>
        </div>
        <el-form-item
          class="form-item form-item-checkbox"
          :class="'form-item-' + themes"
          v-if="showProperty('showExtremum')"
        >
          <el-checkbox
            :effect="themes"
            size="small"
            @change="changeLabelAttr('seriesLabelFormatter')"
            v-model="curSeriesFormatter.showExtremum"
            label="quota"
          >
            {{ t('chart.show') }}最值
          </el-checkbox>
        </el-form-item>
      </template>
    </div>
    <template v-if="isGroupBar">
      <el-form-item class="form-item form-item-checkbox" :class="'form-item-' + themes">
        <el-checkbox
          :effect="themes"
          size="small"
          @change="changeLabelAttr('childrenShow')"
          v-model="state.labelForm.childrenShow"
          label="quota"
        >
          {{ t('chart.label') + t('chart.show') }}
        </el-checkbox>
      </el-form-item>
      <div style="padding-left: 22px">
        <el-space>
          <el-form-item
            class="form-item"
            :class="'form-item-' + themes"
            v-if="showProperty('color')"
            :label="t('chart.text')"
          >
            <el-color-picker
              :disabled="!state.labelForm.childrenShow"
              :effect="themes"
              v-model="state.labelForm.color"
              class="color-picker-style"
              :predefine="COLOR_PANEL"
              @change="changeLabelAttr('color')"
              is-custom
            />
          </el-form-item>
          <el-form-item
            class="form-item"
            :class="'form-item-' + themes"
            v-if="showProperty('fontSize')"
          >
            <template #label>&nbsp;</template>
            <el-tooltip content="字号" :effect="toolTip" placement="top">
              <el-select
                :disabled="!state.labelForm.childrenShow"
                size="small"
                style="width: 108px"
                :effect="themes"
                v-model.number="state.labelForm.fontSize"
                :placeholder="t('chart.text_fontsize')"
                @change="changeLabelAttr('fontSize')"
              >
                <el-option
                  v-for="option in fontSizeList"
                  :key="option.value"
                  :label="option.name"
                  :value="option.value"
                />
              </el-select>
            </el-tooltip>
          </el-form-item>
        </el-space>
        <el-form-item
          v-if="showProperty('vPosition')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <template #label>
            {{ t('chart.label_position') }}
            <el-tooltip
              class="item"
              :effect="toolTip"
              placement="top"
              v-if="chart.type.includes('chart-mix')"
            >
              <template #content>
                <span v-html="t('chart.chart_mix_label_only_left')"></span>
              </template>
              <span style="vertical-align: middle">
                <el-icon style="cursor: pointer">
                  <Icon name="icon_info_outlined" />
                </el-icon>
              </span>
            </el-tooltip>
          </template>
          <el-select
            :disabled="!state.labelForm.childrenShow"
            size="small"
            :effect="themes"
            v-model="state.labelForm.position"
            :placeholder="t('chart.label_position')"
            @change="changeLabelAttr('position')"
          >
            <el-option
              v-for="option in labelPositionV"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          :label="$t('chart.value_formatter_type')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-select
            :disabled="!state.labelForm.childrenShow"
            size="small"
            :effect="themes"
            v-model="state.labelForm.labelFormatter.type"
            @change="changeLabelAttr('labelFormatter.type')"
          >
            <el-option
              v-for="type in formatterType"
              :key="type.value"
              :label="$t('chart.' + type.name)"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="state.labelForm.labelFormatter && state.labelForm.labelFormatter.type !== 'auto'"
          :label="$t('chart.value_formatter_decimal_count')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-input-number
            :disabled="!state.labelForm.childrenShow"
            controls-position="right"
            :effect="themes"
            v-model="state.labelForm.labelFormatter.decimalCount"
            :precision="0"
            :min="0"
            :max="10"
            @change="changeLabelAttr('labelFormatter.decimalCount')"
          />
        </el-form-item>

        <el-row
          :gutter="8"
          v-if="state.labelForm.labelFormatter && state.labelForm.labelFormatter.type !== 'percent'"
        >
          <el-col :span="12">
            <el-form-item
              :label="$t('chart.value_formatter_unit')"
              class="form-item"
              :class="'form-item-' + themes"
            >
              <el-select
                :disabled="!state.labelForm.childrenShow"
                size="small"
                :effect="themes"
                v-model="state.labelForm.labelFormatter.unit"
                :placeholder="$t('chart.pls_select_field')"
                @change="changeLabelAttr('labelFormatter.unit')"
              >
                <el-option
                  v-for="item in unitType"
                  :key="item.value"
                  :label="$t('chart.' + item.name)"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              :label="$t('chart.value_formatter_suffix')"
              class="form-item"
              :class="'form-item-' + themes"
            >
              <el-input
                :disabled="!state.labelForm.childrenShow"
                :effect="themes"
                v-model="state.labelForm.labelFormatter.suffix"
                clearable
                :placeholder="$t('commons.input_content')"
                @change="changeLabelAttr('labelFormatter.suffix')"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-checkbox
            size="small"
            :effect="themes"
            v-model="state.labelForm.labelFormatter.thousandSeparator"
            @change="changeLabelAttr('labelFormatter.thousandSeparator')"
            :label="t('chart.value_formatter_thousand_separator')"
            :disabled="!state.labelForm.childrenShow"
          />
        </el-form-item>
      </div>
    </template>
    <el-form-item
      class="form-item form-item-checkbox"
      :class="'form-item-' + themes"
      v-if="showProperty('showExtremum') && !showSeriesLabelFormatter"
    >
      <el-checkbox
        :effect="themes"
        size="small"
        @change="changeLabelAttr('showExtremum')"
        v-model="state.labelForm.showExtremum"
        label="quota"
      >
        {{ t('chart.show') }}最值
      </el-checkbox>
    </el-form-item>
    <el-form-item class="form-item" :class="'form-item-' + themes" v-show="showProperty('showGap')">
      <el-checkbox
        :effect="themes"
        @change="changeLabelAttr('showGap')"
        v-model="state.labelForm.showGap"
      >
        {{ t('chart.show_gap') }}
      </el-checkbox>
    </el-form-item>
  </el-form>
</template>

<style lang="less" scoped>
.form-item-checkbox {
  margin-bottom: 8px !important;
}

.series-select {
  :deep(.ed-select__prefix--light) {
    padding-right: unset;
    border-right: unset;
  }

  :deep(.ed-select__prefix--dark) {
    padding-right: unset;
    border-right: unset;
  }
}

.series-select-option {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 0 11px;
}

.m-divider {
  margin: 0 0 16px;
  border-color: rgba(31, 35, 41, 0.15);

  &.divider-dark {
    border-color: rgba(255, 255, 255, 0.15);
  }
}
.data-area-label {
  text-align: left;
  position: relative;
  width: 100%;
  display: flex;
  flex-direction: row;
  align-items: center;
}
</style>
