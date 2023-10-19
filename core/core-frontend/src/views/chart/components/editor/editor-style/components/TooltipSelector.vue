<script lang="ts" setup>
import { PropType, computed, onMounted, reactive, watch, ref, inject } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_TOOLTIP } from '@/views/chart/components/editor/util/chart'
import { ElSpace } from 'element-plus-secondary'
import cloneDeep from 'lodash-es/cloneDeep'
import defaultsDeep from 'lodash-es/defaultsDeep'
import { formatterType, unitType } from '../../../js/formatter'
import { fieldType } from '@/utils/attr'
import { differenceBy, partition } from 'lodash-es'
import chartViewManager from '../../../js/panel'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'

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
  return showProperty('seriesTooltipFormatter') && !batchOptStatus.value
})
// 初始化系列提示
const initSeriesTooltip = () => {
  if (!showSeriesTooltipFormatter.value) {
    return
  }
  const formatter = state.tooltipForm.seriesTooltipFormatter
  const seriesAxisMap = formatter.reduce((pre, next) => {
    next.seriesId = next.seriesId ?? next.id
    pre[next.seriesId] = next
    return pre
  }, {})
  // 新增视图
  if (!quotaAxis.value?.length) {
    if (!formatter.length) {
      quotaData.value?.forEach(i => formatter.push({ ...i, seriesId: i.id, show: false }))
    }
    curSeriesFormatter.value = {}
    return
  }
  formatter.splice(0, formatter.length)
  const axisIds = quotaAxis.value?.map(i => i.id)
  const allQuotaAxis = quotaAxis.value?.concat(
    quotaData.value?.filter(ele => !axisIds.includes(ele.id))
  )
  const axisMap = allQuotaAxis.reduce((pre, next, index) => {
    let tmp = {
      ...next,
      seriesId: next.seriesId ?? next.id,
      show: index <= quotaAxis.value.length - 1,
      summary: 'sum'
    } as SeriesFormatter
    if (seriesAxisMap[tmp.seriesId]) {
      tmp = {
        ...tmp,
        formatterCfg: seriesAxisMap[tmp.seriesId].formatterCfg,
        show: seriesAxisMap[tmp.seriesId].show,
        summary: seriesAxisMap[tmp.seriesId].summary,
        chartShowName: seriesAxisMap[tmp.seriesId].chartShowName
      }
    }
    formatter.push(tmp)
    pre[tmp.seriesId] = tmp
    return pre
  }, {})
  if (!curSeriesFormatter.value || !axisMap[curSeriesFormatter.value.seriesId]) {
    curSeriesFormatter.value = axisMap[formatter[0].seriesId]
    return
  }
  curSeriesFormatter.value = axisMap[curSeriesFormatter.value.seriesId]
}
// 更新系列提示
const updateSeriesTooltip = (newAxis?: SeriesFormatter[], oldAxis?: SeriesFormatter[]) => {
  if (
    !showSeriesTooltipFormatter.value ||
    !state.tooltipForm.seriesTooltipFormatter.length ||
    !quotaData.value?.length
  ) {
    return
  }
  const axisMap: Record<string, Axis> = newAxis?.reduce((pre, next) => {
    pre[next.seriesId] = next
    return pre
  }, {})
  // 新增
  const addedAxisMap = differenceBy(newAxis, oldAxis, 'seriesId').reduce((pre, next) => {
    pre[next.id] = next
    return pre
  }, {}) as Record<string, SeriesFormatter>
  // 删除
  const removedAxisMap = differenceBy(oldAxis, newAxis, 'seriesId').reduce((pre, next) => {
    pre[next.seriesId] = next
    return pre
  }, {}) as Record<string, SeriesFormatter>
  const quotaIds = quotaData.value?.map(i => i.id)
  state.tooltipForm.seriesTooltipFormatter = state.tooltipForm.seriesTooltipFormatter?.filter(i =>
    quotaIds?.includes(i.id)
  )
  const dupAxis: SeriesFormatter[] = []
  state.tooltipForm.seriesTooltipFormatter?.forEach(ele => {
    if (addedAxisMap[ele.id]) {
      // 数据集中的字段
      ele.show = true
      if (ele.seriesId === ele.id) {
        ele.seriesId = addedAxisMap[ele.id].seriesId
        ele.axisType = addedAxisMap[ele.id].axisType
      } else {
        // 其他轴已有的字段
        const tmp = cloneDeep(addedAxisMap[ele.id])
        tmp.show = true
        dupAxis.push(tmp)
      }
    }
    if (removedAxisMap[ele.seriesId]) {
      ele.show = false
      ele.seriesId = ele.id
    }
    ele.chartShowName = axisMap[ele.seriesId]?.chartShowName
    ele.summary = axisMap[ele.seriesId]?.summary ?? ele.summary
  })
  // 重新排序
  state.tooltipForm.seriesTooltipFormatter =
    state.tooltipForm.seriesTooltipFormatter.concat(dupAxis)
  state.tooltipForm.seriesTooltipFormatter = partition(
    state.tooltipForm.seriesTooltipFormatter,
    ele => axisMap[ele.seriesId]
  ).flat()
  if (removedAxisMap[curSeriesFormatter.value?.seriesId]) {
    curSeriesFormatter.value = state.tooltipForm.seriesTooltipFormatter?.[0]
  }
  if (!newAxis.length) {
    curSeriesFormatter.value = {}
  }
  emit('onTooltipChange', { data: state.tooltipForm, render: false })
  emit('onExtTooltipChange', extTooltip.value)
}
const quotaAxis = computed(() => {
  let result = []
  const axisProp: AxisType[] = ['yAxis', 'yAxisExt', 'extBubble']
  axisProp.forEach(prop => {
    if (!chartViewInstance.value?.axis?.includes(prop)) {
      return
    }
    const axis = props.chart[prop]
    axis?.forEach(item => {
      item.axisType = prop
      item.seriesId = `${item.id}-${prop}`
      result.push(item)
    })
  })
  return result
})
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
watch(
  () => cloneDeep(quotaAxis.value),
  (newVal, oldVal) => {
    updateSeriesTooltip(newVal, oldVal)
  },
  { deep: true }
)
watch(
  [() => props.chart.customAttr.tooltip, () => props.chart.customAttr.tooltip.show],
  () => {
    init()
  },
  { deep: false }
)
watch(
  [quotaData, () => props.chart.type],
  newVal => {
    if (!newVal?.[0]?.length) {
      return
    }
    initSeriesTooltip()
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
      const formatter = state.tooltipForm.seriesTooltipFormatter
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

const showProperty = prop => props.propertyInner?.includes(prop)

onMounted(() => {
  init()
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
    <template v-if="showProperty('tooltipFormatter')">
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
          <el-option
            class="series-select-option"
            :key="item.id"
            :value="item"
            :label="`${item.name}${
              item.summary !== '' ? '(' + t('chart.' + item.summary) + ')' : ''
            }`"
            v-for="item in state.tooltipForm.seriesTooltipFormatter"
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
        </el-select>
      </el-form-item>
      <template v-if="curSeriesFormatter?.seriesId">
        <el-form-item class="form-item form-item-checkbox" :class="'form-item-' + themes">
          <el-checkbox
            size="small"
            @change="changeTooltipAttr('seriesTooltipFormatter', true)"
            v-model="curSeriesFormatter.show"
            label="quota"
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
                    v-for="item in AGGREGATION_TYPE"
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
</style>
