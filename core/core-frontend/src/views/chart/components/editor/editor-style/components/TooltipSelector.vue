<script lang="ts" setup>
import { PropType, computed, onMounted, reactive, watch, ref, inject } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_TOOLTIP } from '@/views/chart/components/editor/util/chart'
import { ElIcon, ElSpace } from 'element-plus-secondary'
import cloneDeep from 'lodash-es/cloneDeep'
import defaultsDeep from 'lodash-es/defaultsDeep'
import { formatterType, unitType } from '../../../js/formatter'
import { fieldType } from '@/utils/attr'
import { defaultTo, partition, map, includes } from 'lodash-es'
import chartViewManager from '../../../js/panel'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { useEmitt } from '@/hooks/web/useEmitt'
import Icon from '../../../../../../components/icon-custom/src/Icon.vue'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object as PropType<ChartObj>,
    required: true
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
const { batchOptStatus } = storeToRefs(dvMainStore)
const predefineColors = COLOR_PANEL
const toolTip = computed(() => {
  return props.themes === 'dark' ? 'ndark' : 'dark'
})
const emit = defineEmits(['onTooltipChange', 'onExtTooltipChange'])
const curSeriesFormatter = ref<DeepPartial<SeriesFormatter>>({})
const quotaData = ref<Axis[]>(inject('quotaData'))
const showSeriesTooltipFormatter = computed(() => {
  return showProperty('seriesTooltipFormatter') && !batchOptStatus.value && props.chart.id
})

// 切换图表类型直接重置为默认
const changeChartType = () => {
  if (!showSeriesTooltipFormatter.value) {
    return
  }
  curSeriesFormatter.value = {}
  const formatter = state.tooltipForm.seriesTooltipFormatter
  formatter.splice(0, formatter.length)
  const axisIds = []
  quotaAxis.value.forEach(axis => {
    formatter.push({
      ...axis,
      show: true
    })
    axisIds.push(axis.id)
  })
  quotaData.value.forEach(quotaAxis => {
    if (!axisIds.includes(quotaAxis.id)) {
      formatter.push({
        ...quotaAxis,
        seriesId: quotaAxis.id,
        show: false
      })
    }
  })
  emit('onTooltipChange', { data: state.tooltipForm, render: false }, 'seriesTooltipFormatter')
  emit('onExtTooltipChange', extTooltip.value)
}
// 切换数据集
const changeDataset = () => {
  curSeriesFormatter.value = {}
  const formatter = state.tooltipForm.seriesTooltipFormatter
  const quotaIds = quotaData.value.map(i => i.id)
  for (let i = formatter.length - 1; i >= 0; i--) {
    if (!quotaIds.includes(formatter[i].id)) {
      formatter.splice(i, 1)
    }
  }
  const formatterIds = formatter.map(i => i.id)
  quotaData.value.forEach(axis => {
    if (!formatterIds.includes(axis.id)) {
      formatter.push({
        ...axis,
        seriesId: axis.id,
        show: false
      })
    }
  })
  if (showProperty('showFields')) {
    state.tooltipForm.showFields = []
    emit('onTooltipChange', { data: state.tooltipForm }, 'showFields')
  }
}

const AXIS_PROP: AxisType[] = ['yAxis', 'yAxisExt', 'extBubble']
const quotaAxis = computed(() => {
  let result = []
  AXIS_PROP.forEach(prop => {
    if (!chartViewInstance.value?.axis?.includes(prop)) {
      return
    }
    const axis = props.chart[prop]
    axis?.forEach(item => {
      result.push({ ...item, seriesId: `${item.id}-${prop}` })
    })
  })
  return result
})

const quotaAxisIds = computed(() => {
  return map(quotaAxis.value, a => a.id)
})

function showOption(item) {
  if (props.chart.type.includes('chart-mix')) {
    return includes(quotaAxisIds.value, item.id)
  }
  return true
}

const extTooltip = computed(() => {
  const quotaIds = quotaAxis.value?.map(i => i.id)
  return state.tooltipForm.seriesTooltipFormatter.filter(
    i => !quotaIds.includes(i.id) && i.show && quotaData.value?.findIndex(j => j.id === i.id) !== -1
  )
})
const showFormatterSummary = computed(() => {
  return (
    quotaAxis.value?.findIndex(i => curSeriesFormatter.value.id === i.id) === -1 &&
    curSeriesFormatter.value.id !== '-1'
  )
})
const formatterNameEditable = computed(() => {
  return quotaAxis.value?.findIndex(i => curSeriesFormatter.value.id === i.id) !== -1
})
const formatterEditable = computed(() => {
  return (
    showProperty('seriesTooltipFormatter') &&
    (props.chart.yAxis?.length || props.chart.yAxisExt?.length)
  )
})
const chartViewInstance = computed(() => {
  return chartViewManager.getChartView(props.chart.render, props.chart.type)
})
const AGGREGATION_TYPE = [
  { name: t('chart.sum'), value: 'sum' },
  { name: t('chart.avg'), value: 'avg' },
  { name: t('chart.max'), value: 'max' },
  { name: t('chart.min'), value: 'min' },
  { name: t('chart.stddev_pop'), value: 'stddev_pop' },
  { name: t('chart.var_pop'), value: 'var_pop' },
  { name: t('chart.count'), value: 'count' },
  { name: t('chart.count_distinct'), value: 'count_distinct' }
]
const COUNT_AGGREGATION_TYPE = [
  { name: t('chart.count'), value: 'count' },
  { name: t('chart.count_distinct'), value: 'count_distinct' }
]
const COUNT_DE_TYPE = [0, 1, 5]

const aggregationList = computed(() => {
  if (COUNT_DE_TYPE.includes(curSeriesFormatter.value?.deType)) {
    return COUNT_AGGREGATION_TYPE
  }
  return AGGREGATION_TYPE
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

watch(
  [() => props.chart.customAttr.tooltip, () => props.chart.customAttr.tooltip.show],
  () => {
    init()
  },
  { deep: false }
)

const state = reactive({
  tooltipForm: {
    tooltipFormatter: DEFAULT_TOOLTIP.tooltipFormatter
  } as DeepPartial<ChartTooltipAttr>
})

const fontSizeList = computed(() => {
  const arr = []
  for (let i = 10; i <= 40; i = i + 2) {
    arr.push({
      name: i + '',
      value: i
    })
  }
  return arr
})

const changeTooltipAttr = (prop: string, requestData = false, render = true) => {
  // 多序列处理 extTooltip
  if (prop === 'seriesTooltipFormatter') {
    emit('onExtTooltipChange', extTooltip.value)
  }
  emit('onTooltipChange', { data: state.tooltipForm, requestData, render }, prop)
}
const formatterSelector = ref()
const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customAttr) {
    const customAttr = JSON.parse(JSON.stringify(chart.customAttr))
    if (customAttr.tooltip) {
      state.tooltipForm = defaultsDeep(customAttr.tooltip, cloneDeep(DEFAULT_TOOLTIP))
      formatterSelector.value?.blur()
      // 新增图表
      const formatter = state.tooltipForm.seriesTooltipFormatter
      if (!formatter.length) {
        quotaData.value?.forEach(i => formatter.push({ ...i, seriesId: i.id, show: false }))
        curSeriesFormatter.value = {}
        return
      }
      const seriesAxisMap = formatter.reduce((pre, next) => {
        next.seriesId = next.seriesId ?? next.id
        pre[next.seriesId] = next
        return pre
      }, {})
      if (!curSeriesFormatter?.value || !seriesAxisMap[curSeriesFormatter.value?.seriesId]) {
        curSeriesFormatter.value = {}
      } else {
        curSeriesFormatter.value = seriesAxisMap[curSeriesFormatter.value?.seriesId]
      }
    }
  }
}

const showProperty = prop => {
  const instance = chartViewManager.getChartView(props.chart.render, props.chart.type)
  if (instance) {
    return instance.propertyInner['tooltip-selector'].includes(prop)
  }
  return props.propertyInner?.includes(prop)
}
const updateSeriesTooltipFormatter = (form: AxisEditForm) => {
  const { axisType, editType } = form
  if (
    !showSeriesTooltipFormatter.value ||
    !state.tooltipForm.seriesTooltipFormatter.length ||
    !quotaData.value?.length ||
    !AXIS_PROP.includes(axisType)
  ) {
    return
  }
  switch (editType) {
    case 'add':
      addAxis(form)
      break
    case 'remove':
      removeAxis(form)
      break
    case 'update':
      updateAxis(form)
      break
    default:
      break
  }
  emit('onTooltipChange', { data: state.tooltipForm, render: false }, 'seriesTooltipFormatter')
  emit('onExtTooltipChange', extTooltip.value)
}
const addAxis = (form: AxisEditForm) => {
  const { axis, axisType } = form
  const axisMap = axis.reduce((pre, next) => {
    if (!next) {
      return pre
    }
    next.axisType = axisType
    next.seriesId = `${next.id}-${axisType}`
    pre[next.id] = next
    return pre
  }, {})
  const dupAxis = []
  state.tooltipForm.seriesTooltipFormatter.forEach(ele => {
    if (axisMap[ele.id]) {
      // 数据集中的字段
      ele.show = true
      if (ele.seriesId === ele.id) {
        ele.seriesId = axisMap[ele.id].seriesId
        ele.axisType = axisMap[ele.id].axisType
        ele.summary = axisMap[ele.id].summary
        ele.chartShowName = axisMap[ele.id].chartShowName
      } else {
        // 其他轴已有的字段
        if (dupAxis.findIndex(i => i.id === ele.id) !== -1) {
          return
        }
        const tmp = cloneDeep(axisMap[ele.id])
        tmp.show = true
        dupAxis.push(tmp)
      }
    }
  })
  state.tooltipForm.seriesTooltipFormatter = partition(
    state.tooltipForm.seriesTooltipFormatter.concat(dupAxis),
    ele => quotaAxis.value.findIndex(item => item.id === ele.id) !== -1
  ).flat()
}
const removeAxis = (form: AxisEditForm) => {
  const { axis, axisType } = form
  const axisMap = axis.reduce((pre, next) => {
    if (!next) {
      return pre
    }
    next.axisType = axisType
    next.seriesId = `${next.id}-${axisType}`
    pre[next.seriesId] = next
    return pre
  }, {})
  const quotaIds = quotaData.value?.map(i => i.id)
  const formatterDupMap = state.tooltipForm.seriesTooltipFormatter.reduce((pre, next) => {
    if (pre[next.id] !== undefined) {
      pre[`${next.id}-${axisType}`] = true
    } else {
      pre[next.id] = false
    }
    return pre
  }, {})
  state.tooltipForm.seriesTooltipFormatter = state.tooltipForm.seriesTooltipFormatter?.filter(
    i => quotaIds?.includes(i.id) && !formatterDupMap[i.seriesId]
  )
  state.tooltipForm.seriesTooltipFormatter.forEach(ele => {
    if (axisMap[ele.seriesId]) {
      // 数据集中的字段
      ele.show = false
      ele.summary = axisMap[ele.seriesId].summary
      ele.seriesId = ele.id
    }
  })
  state.tooltipForm.seriesTooltipFormatter = partition(
    state.tooltipForm.seriesTooltipFormatter,
    ele => quotaAxis.value.findIndex(item => item.id === ele.id) !== -1
  ).flat()
  if (!quotaAxis.value?.length) {
    curSeriesFormatter.value = {}
    return
  }
  if (axisMap[curSeriesFormatter.value?.seriesId]) {
    curSeriesFormatter.value = state.tooltipForm.seriesTooltipFormatter?.[0]
  }
}
const updateAxis = (form: AxisEditForm) => {
  const { axis, axisType } = form
  const axisMap = axis.reduce((pre, next) => {
    if (!next) {
      return pre
    }
    next.axisType = axisType
    next.seriesId = `${next.id}-${axisType}`
    pre[next.seriesId] = next
    return pre
  }, {})
  state.tooltipForm.seriesTooltipFormatter.forEach(ele => {
    if (axisMap[ele.seriesId]) {
      ele.chartShowName = axisMap[ele.seriesId]?.chartShowName
      ele.summary = axisMap[ele.seriesId]?.summary ?? ele.summary
    }
  })
}
const allFields = computed(() => {
  return defaultTo(props.allFields, [])
})
const defaultPlaceholder = computed(() => {
  if (state.tooltipForm.showFields && state.tooltipForm.showFields.length > 0) {
    return state.tooltipForm.showFields
      .map(field => {
        const v = field.split('@')
        return v[1] + ': ${' + field.split('@')[1] + '}'
      })
      .join('\n')
  }
  return ''
})
onMounted(() => {
  init()
  useEmitt({ name: 'addAxis', callback: updateSeriesTooltipFormatter })
  useEmitt({ name: 'removeAxis', callback: updateSeriesTooltipFormatter })
  useEmitt({ name: 'updateAxis', callback: updateSeriesTooltipFormatter })
  useEmitt({ name: 'chart-type-change', callback: changeChartType })
  useEmitt({ name: 'dataset-change', callback: changeDataset })
})
</script>

<template>
  <el-form
    ref="tooltipForm"
    :disabled="!state.tooltipForm.show"
    :model="state.tooltipForm"
    label-position="top"
  >
    <el-form-item
      :label="t('chart.background') + t('chart.color')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-color-picker
        :effect="themes"
        v-model="state.tooltipForm.backgroundColor"
        class="color-picker-style"
        :predefine="predefineColors"
        @change="changeTooltipAttr('backgroundColor')"
        is-custom
        :trigger-width="108"
      />
    </el-form-item>
    <el-space>
      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        v-if="showProperty('color')"
        :label="t('chart.text')"
      >
        <el-color-picker
          :effect="themes"
          v-model="state.tooltipForm.color"
          class="color-picker-style"
          :predefine="predefineColors"
          @change="changeTooltipAttr('color')"
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
            v-model.number="state.tooltipForm.fontSize"
            :placeholder="t('chart.text_fontsize')"
            @change="changeTooltipAttr('fontSize')"
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

    <div v-if="showProperty('showFields') && !batchOptStatus">
      <el-form-item :label="t('chart.tooltip')" class="form-item" :class="'form-item-' + themes">
        <el-select
          size="small"
          :effect="themes"
          filterable
          multiple
          collapse-tags
          collapse-tags-tooltip
          v-model="state.tooltipForm.showFields"
          @change="changeTooltipAttr('showFields')"
        >
          <el-option
            v-for="option in allFields"
            :key="option.dataeaseName"
            :label="option.name"
            :value="option.dataeaseName + '@' + option.name"
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
                <div>可以${fieldName}的形式读取字段值（支持HTML）</div>
              </template>
              <el-icon class="hint-icon" :class="{ 'hint-icon--dark': themes === 'dark' }">
                <Icon name="icon_info_outlined" />
              </el-icon>
            </el-tooltip>
          </span>
        </template>
        <el-input
          style="font-size: smaller; font-weight: normal"
          :effect="themes"
          v-model="state.tooltipForm.customContent"
          type="textarea"
          :autosize="{ minRows: 4, maxRows: 4 }"
          :placeholder="defaultPlaceholder"
          @blur="changeTooltipAttr('customContent')"
        />
      </el-form-item>
    </div>

    <template v-if="showProperty('tooltipFormatter') && !isBarRangeTime">
      <el-form-item
        :label="t('chart.value_formatter_type')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-select
          size="small"
          style="width: 100%"
          :effect="props.themes"
          v-model="state.tooltipForm.tooltipFormatter.type"
          @change="changeTooltipAttr('tooltipFormatter.type')"
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
        v-if="state.tooltipForm.tooltipFormatter.type !== 'auto'"
        :label="t('chart.value_formatter_decimal_count')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-input-number
          controls-position="right"
          style="width: 100%"
          :effect="props.themes"
          v-model="state.tooltipForm.tooltipFormatter.decimalCount"
          :precision="0"
          :min="0"
          :max="10"
          size="small"
          @change="changeTooltipAttr('tooltipFormatter.decimalCount')"
        />
      </el-form-item>

      <el-row :gutter="8" v-if="state.tooltipForm.tooltipFormatter.type !== 'percent'">
        <el-col :span="12">
          <el-form-item
            :label="t('chart.value_formatter_unit')"
            class="form-item"
            :class="'form-item-' + themes"
          >
            <el-select
              :disabled="state.tooltipForm.tooltipFormatter.type === 'percent'"
              :effect="props.themes"
              v-model="state.tooltipForm.tooltipFormatter.unit"
              :placeholder="t('chart.pls_select_field')"
              size="small"
              @change="changeTooltipAttr('tooltipFormatter.unit')"
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
              :effect="props.themes"
              v-model="state.tooltipForm.tooltipFormatter.suffix"
              size="small"
              clearable
              :placeholder="t('commons.input_content')"
              @change="changeTooltipAttr('tooltipFormatter.suffix')"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item class="form-item" :class="'form-item-' + themes">
        <el-checkbox
          size="small"
          :effect="props.themes"
          v-model="state.tooltipForm.tooltipFormatter.thousandSeparator"
          @change="changeTooltipAttr('tooltipFormatter.thousandSeparator')"
          :label="t('chart.value_formatter_thousand_separator')"
        />
      </el-form-item>
    </template>
    <div v-if="showSeriesTooltipFormatter">
      <el-form-item>
        <el-select
          size="small"
          v-model="curSeriesFormatter"
          :disabled="!formatterEditable"
          :effect="themes"
          ref="formatterSelector"
          value-key="seriesId"
          class="series-select"
        >
          <template #prefix>
            <el-icon v-if="curSeriesFormatter.seriesId" style="font-size: 14px">
              <Icon
                :className="`field-icon-${fieldType[curSeriesFormatter.deType]}`"
                :name="`field_${fieldType[curSeriesFormatter.deType]}`"
              />
            </el-icon>
          </template>
          <template v-for="item in state.tooltipForm.seriesTooltipFormatter" :key="item.seriesId">
            <el-option
              class="series-select-option"
              :value="item"
              :label="`${item.name}${
                item.summary !== '' ? '(' + t('chart.' + item.summary) + ')' : ''
              }`"
              v-if="showOption(item)"
            >
              <el-icon style="margin-right: 8px">
                <Icon
                  :className="`field-icon-${fieldType[item.deType]}`"
                  :name="`field_${fieldType[item.deType]}`"
                />
              </el-icon>
              {{ item.name }}
              {{ item.summary !== '' ? '(' + t('chart.' + item.summary) + ')' : '' }}
            </el-option>
          </template>
        </el-select>
      </el-form-item>
      <template v-if="curSeriesFormatter?.seriesId">
        <el-form-item class="form-item form-item-checkbox" :class="'form-item-' + themes">
          <el-checkbox
            :disabled="!formatterEditable"
            v-model="curSeriesFormatter.show"
            :effect="themes"
            size="small"
            label="quota"
            @change="changeTooltipAttr('seriesTooltipFormatter', true)"
          >
            {{ t('chart.show') }}
          </el-checkbox>
        </el-form-item>
        <div style="padding-left: 22px">
          <el-form-item
            class="form-item"
            :class="'form-item-' + themes"
            :label="t('chart.show_name')"
          >
            <el-input
              :effect="themes"
              size="small"
              :maxlength="20"
              @change="changeTooltipAttr('seriesTooltipFormatter')"
              v-model="curSeriesFormatter.chartShowName"
              :disabled="!curSeriesFormatter.show || formatterNameEditable"
            />
          </el-form-item>
          <el-row v-if="showFormatterSummary">
            <el-col>
              <el-form-item
                :label="t('common.please_select') + t('chart.aggregation')"
                class="form-item"
                :class="'form-item-' + themes"
              >
                <el-select
                  size="small"
                  v-model="curSeriesFormatter.summary"
                  :disabled="!curSeriesFormatter.show"
                  :effect="props.themes"
                  @change="changeTooltipAttr('seriesTooltipFormatter', true)"
                >
                  <el-option
                    v-for="item in aggregationList"
                    :label="item.name"
                    :value="item.value"
                    :key="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item
            :label="t('chart.value_formatter_type')"
            class="form-item"
            :class="'form-item-' + themes"
          >
            <el-select
              size="small"
              :disabled="!curSeriesFormatter.show"
              style="width: 100%"
              :effect="props.themes"
              v-model="curSeriesFormatter.formatterCfg.type"
              @change="changeTooltipAttr('seriesTooltipFormatter')"
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
            v-if="curSeriesFormatter.formatterCfg.type !== 'auto'"
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
              @change="changeTooltipAttr('seriesTooltipFormatter')"
            />
          </el-form-item>

          <el-row :gutter="8" v-if="curSeriesFormatter.formatterCfg.type !== 'percent'">
            <el-col :span="12">
              <el-form-item
                :label="t('chart.value_formatter_unit')"
                class="form-item"
                :class="'form-item-' + themes"
              >
                <el-select
                  :disabled="
                    !curSeriesFormatter.show || curSeriesFormatter.formatterCfg.type == 'percent'
                  "
                  :effect="props.themes"
                  v-model="curSeriesFormatter.formatterCfg.unit"
                  :placeholder="t('chart.pls_select_field')"
                  size="small"
                  @change="changeTooltipAttr('seriesTooltipFormatter')"
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
                  @change="changeTooltipAttr('seriesTooltipFormatter')"
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
              @change="changeTooltipAttr('seriesTooltipFormatter')"
              :label="t('chart.value_formatter_thousand_separator')"
            />
          </el-form-item>
        </div>
      </template>
    </div>
    <el-form-item class="form-item" :class="'form-item-' + themes" v-show="showProperty('showGap')">
      <el-checkbox
        :effect="themes"
        @change="changeTooltipAttr('showGap')"
        v-model="state.tooltipForm.showGap"
      >
        {{ t('chart.show_gap') }}
      </el-checkbox>
    </el-form-item>
  </el-form>
</template>

<style lang="less" scoped>
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
.form-item-checkbox {
  margin-bottom: 8px !important;
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
