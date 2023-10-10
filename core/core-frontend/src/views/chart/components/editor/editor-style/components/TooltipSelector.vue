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

const predefineColors = COLOR_PANEL

const emit = defineEmits(['onTooltipChange', 'onExtTooltipChange'])

const curSeriesFormatter = ref<DeepPartial<SeriesFormatter>>({})
const quotaData = ref<Axis[]>(inject('quotaData'))
// 初始化系列提示
const initSeriesTooltip = (firstLoad: boolean) => {
  if (!showProperty('seriesTooltipFormatter')) {
    return
  }
  const formatter = state.tooltipForm.seriesTooltipFormatter
  const yAxis = props.chart.yAxis?.concat(props.chart.extBubble)
  const seriesAxisMap = formatter.reduce((pre, next) => {
    pre[next.id] = next
    return pre
  }, {})
  // 新增视图
  if (!yAxis.length) {
    if (!formatter.length) {
      quotaData.value?.forEach(i => formatter.push({ ...i, show: false }))
    }
    curSeriesFormatter.value = {}
    return
  }
  // 首次加载不对原始配置做更改
  if (firstLoad) {
    if (!formatter.length) {
      return
    }
    if (!curSeriesFormatter.value || !seriesAxisMap[curSeriesFormatter.value.id]) {
      curSeriesFormatter.value = seriesAxisMap[formatter[0].id]
      return
    }
    curSeriesFormatter.value = seriesAxisMap[curSeriesFormatter.value.id]
    return
  }
  formatter.splice(0, formatter.length)
  const yAxisMap = yAxis.reduce((pre, next) => {
    pre[next.id] = next
    return pre
  }, {})
  const allQuotaAxis = yAxis.concat(quotaData.value?.filter(ele => !yAxisMap[ele.id]))
  const axisMap = allQuotaAxis.reduce((pre, next, index) => {
    let tmp = {
      ...next,
      show: index <= yAxis.length - 1,
      summary: 'sum'
    } as SeriesFormatter
    if (seriesAxisMap[next.id]) {
      tmp = {
        ...tmp,
        formatterCfg: seriesAxisMap[next.id].formatterCfg,
        show: seriesAxisMap[next.id].show,
        summary: seriesAxisMap[next.id].summary,
        chartShowName: seriesAxisMap[next.id].chartShowName
      }
    }
    formatter.push(tmp)
    pre[next.id] = tmp
    return pre
  }, {})
  if (!curSeriesFormatter.value || !axisMap[curSeriesFormatter.value.id]) {
    curSeriesFormatter.value = axisMap[formatter[0].id]
    return
  }
  curSeriesFormatter.value = axisMap[curSeriesFormatter.value.id]
}
// 更新系列提示
const updateSeriesTooltip = (newAxis?: Axis[], oldAxis?: Axis[]) => {
  if (!showProperty('seriesTooltipFormatter')) {
    return
  }
  const axisMap: Record<string, Axis> = newAxis?.reduce((pre, next) => {
    pre[next.id] = next
    return pre
  }, {})
  // 新增
  const addedAxis = differenceBy(newAxis, oldAxis, 'id').map(i => i.id)
  // 删除
  const removedAxis = differenceBy(oldAxis, newAxis, 'id').map(i => i.id)
  const quotaIds = quotaData.value?.map(i => i.id)
  state.tooltipForm.seriesTooltipFormatter = state.tooltipForm.seriesTooltipFormatter?.filter(
    i => !removedAxis?.includes(i.id) || quotaIds?.includes(i.id)
  )
  state.tooltipForm.seriesTooltipFormatter?.forEach(ele => {
    ele.chartShowName = axisMap[ele.id]?.chartShowName
    ele.summary = axisMap[ele.id]?.summary ?? ele.summary
    if (addedAxis?.includes(ele.id)) {
      ele.show = true
      return
    }
    if (removedAxis?.includes(ele.id)) {
      ele.show = false
    }
  })
  // 重新排序
  state.tooltipForm.seriesTooltipFormatter = partition(
    state.tooltipForm.seriesTooltipFormatter,
    ele => axisMap[ele.id]
  ).flat()
  const extTooltip = state.tooltipForm.seriesTooltipFormatter.filter(
    i => !axisMap[i.id] && i.show && quotaData.value?.findIndex(j => j.id === i.id) !== -1
  )
  if (removedAxis?.includes(curSeriesFormatter.value?.id)) {
    curSeriesFormatter.value = state.tooltipForm.seriesTooltipFormatter?.[0]
  }
  if (!newAxis.length) {
    curSeriesFormatter.value = {}
  }
  emit('onTooltipChange', { data: state.tooltipForm, render: false })
  emit('onExtTooltipChange', extTooltip)
}
const showFormatterSummary = computed(() => {
  return (
    props.chart.yAxis.findIndex(i => curSeriesFormatter.value.id === i.id) === -1 &&
    curSeriesFormatter.value.id !== '-1'
  )
})
const formatterNameEditable = computed(() => {
  return props.chart.yAxis.findIndex(i => curSeriesFormatter.value.id === i.id) !== -1
})
const formatterEditable = computed(() => {
  return showProperty('seriesTooltipFormatter') && props.chart.yAxis?.length
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
  () => cloneDeep(props.chart.yAxis.concat(props.chart.extBubble)),
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
  () => {
    initSeriesTooltip(false)
  },
  { deep: false }
)

const state = reactive({
  tooltipForm: {} as DeepPartial<ChartTooltipAttr>
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
    const quotaAxis = props.chart.yAxis?.concat(props.chart.extBubble).map(i => i.id)
    const extTooltip = state.tooltipForm.seriesTooltipFormatter.filter(
      i => !quotaAxis?.includes(i.id) && i.show
    )
    emit('onExtTooltipChange', extTooltip)
  }
  emit('onTooltipChange', { data: state.tooltipForm, requestData, render })
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customAttr) {
    const customAttr = JSON.parse(JSON.stringify(chart.customAttr))
    if (customAttr.tooltip) {
      state.tooltipForm = defaultsDeep(customAttr.tooltip, cloneDeep(DEFAULT_TOOLTIP))
      initSeriesTooltip(true)
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
        <el-select
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
      </el-form-item>
    </el-space>
    <template v-if="showProperty('tooltipFormatter')">
      <el-form-item
        :label="t('chart.value_formatter_type')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-select
          style="width: 100%"
          :effect="props.themes"
          v-model="state.tooltipForm.tooltipFormatter.type"
          @change="changeTooltipAttr('tooltipFormatter')"
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
          @change="changeTooltipAttr('tooltipFormatter')"
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
              @change="changeTooltipAttr('tooltipFormatter')"
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
              @change="changeTooltipAttr('tooltipFormatter')"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item class="form-item" :class="'form-item-' + themes">
        <el-checkbox
          size="small"
          :effect="props.themes"
          v-model="state.tooltipForm.tooltipFormatter.thousandSeparator"
          @change="changeTooltipAttr('tooltipFormatter')"
          :label="t('chart.value_formatter_thousand_separator')"
        />
      </el-form-item>
    </template>
    <div v-if="showProperty('seriesTooltipFormatter')">
      <el-form-item>
        <el-select
          :disabled="!formatterEditable"
          v-model="curSeriesFormatter"
          value-key="id"
          class="series-select"
        >
          <template #prefix>
            <el-icon v-if="curSeriesFormatter.id" style="font-size: 14px">
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
      <template v-if="curSeriesFormatter?.id">
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
  ::v-deep(.ed-select__prefix--light) {
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
</style>
