<script lang="ts" setup>
import { computed, onMounted, PropType, reactive, ref, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_LABEL } from '@/views/chart/components/editor/util/chart'
import { ElSpace } from 'element-plus-secondary'
import { formatterType, unitType } from '../../../js/formatter'
import { defaultsDeep, cloneDeep } from 'lodash-es'
import { includesAny } from '../../util/StringUtils'
import { fieldType } from '@/utils/attr'

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

watch(
  () => props.chart.customAttr.label,
  () => {
    init()
  },
  { deep: true }
)
watch(
  () => props.chart.yAxis,
  () => {
    initSeriesLabel()
  },
  { deep: true }
)
// 初始化系列标签
const initSeriesLabel = () => {
  if (!showProperty('seriesLabelFormatter')) {
    return
  }
  const formatter = state.labelForm.seriesLabelFormatter
  const yAxis = props.chart.yAxis
  const seriesAxisMap = formatter.reduce((pre, next) => {
    pre[next.id] = next
    return pre
  }, {})
  formatter.splice(0, formatter.length)
  if (!yAxis.length) {
    curSeriesFormatter.value = {}
    return
  }
  const axisMap = yAxis.reduce((pre, next) => {
    let tmp = {
      ...next,
      show: true,
      color: DEFAULT_LABEL.color,
      fontSize: DEFAULT_LABEL.fontSize
    } as SeriesFormatter
    if (seriesAxisMap[next.id]) {
      tmp = {
        ...tmp,
        formatterCfg: seriesAxisMap[next.id].formatterCfg,
        show: seriesAxisMap[next.id].show,
        color: seriesAxisMap[next.id].color,
        fontSize: seriesAxisMap[next.id].fontSize
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

const state = reactive({
  labelForm: JSON.parse(JSON.stringify(DEFAULT_LABEL))
})

const emit = defineEmits(['onLabelChange'])

const changeLabelAttr = prop => {
  emit('onLabelChange', state.labelForm)
}
const curSeriesFormatter = ref<Partial<SeriesFormatter>>({
  deType: 0
})

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customAttr) {
    let customAttr = null
    if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
      customAttr = JSON.parse(JSON.stringify(chart.customAttr))
    } else {
      customAttr = JSON.parse(chart.customAttr)
    }
    if (customAttr.label) {
      state.labelForm = defaultsDeep(customAttr.label, cloneDeep(DEFAULT_LABEL))
      initSeriesLabel()
    }
  }
}
const showProperty = prop => {
  return props.propertyInner?.includes(prop)
}
const showDivider = computed(() => {
  const DIVIDER_PROPS = ['labelFormatter', 'showDimension', 'showQuota', 'showProportion']
  return includesAny(props.propertyInner, ...DIVIDER_PROPS)
})
onMounted(() => {
  init()
})
</script>

<template>
  <el-form
    ref="labelForm"
    :disabled="!state.labelForm.show"
    :model="state.labelForm"
    label-position="top"
  >
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
        <el-select
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
      </el-form-item>
    </el-space>

    <el-form-item
      v-if="showProperty('rPosition')"
      :label="t('chart.label_position')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-select
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
      v-if="showProperty('hPosition')"
      :label="t('chart.label_position')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-select
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
    <el-form-item
      v-if="showProperty('vPosition')"
      :label="t('chart.label_position')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-select
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
    <el-divider v-if="showDivider" style="margin: 0 0 16px" />
    <template v-if="showProperty('labelFormatter')">
      <el-form-item
        :label="$t('chart.value_formatter_type')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-select
          :effect="themes"
          v-model="state.labelForm.labelFormatter.type"
          @change="changeLabelAttr('labelFormatter')"
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
        v-if="state.labelForm.labelFormatter.type !== 'auto'"
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
          @change="changeLabelAttr('labelFormatter')"
        />
      </el-form-item>

      <el-row :gutter="8" v-if="state.labelForm.labelFormatter.type !== 'percent'">
        <el-col :span="12">
          <el-form-item
            :label="$t('chart.value_formatter_unit')"
            class="form-item"
            :class="'form-item-' + themes"
          >
            <el-select
              :disabled="state.labelForm.labelFormatter.type === 'percent'"
              :effect="themes"
              v-model="state.labelForm.labelFormatter.unit"
              :placeholder="$t('chart.pls_select_field')"
              @change="changeLabelAttr('labelFormatter')"
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
              @change="changeLabelAttr('labelFormatter')"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item class="form-item" :class="'form-item-' + themes">
        <el-checkbox
          size="small"
          :effect="themes"
          v-model="state.labelForm.labelFormatter.thousandSeparator"
          @change="changeLabelAttr('labelFormatter')"
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
        :effect="themes"
        size="small"
        @change="changeLabelAttr('showDimension')"
        v-model="state.labelForm.showDimension"
        label="dimension"
      >
        {{ t('chart.dimension') }}
      </el-checkbox>
    </el-form-item>
    <template v-if="showProperty('showQuota')">
      <el-form-item class="form-item form-item-checkbox" :class="'form-item-' + themes">
        <el-checkbox
          :effect="themes"
          size="small"
          @change="changeLabelAttr('showQuota')"
          v-model="state.labelForm.showQuota"
          label="quota"
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
            :disabled="!state.labelForm.showQuota"
            style="width: 100%"
            :effect="themes"
            v-model="state.labelForm.quotaLabelFormatter.type"
            @change="changeLabelAttr('quotaLabelFormatter')"
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
          v-if="state.labelForm.quotaLabelFormatter.type !== 'auto'"
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
            @change="changeLabelAttr('quotaLabelFormatter')"
          />
        </el-form-item>

        <el-row :gutter="8" v-if="state.labelForm.quotaLabelFormatter.type !== 'percent'">
          <el-col :span="12">
            <el-form-item
              :label="t('chart.value_formatter_unit')"
              class="form-item"
              :class="'form-item-' + themes"
            >
              <el-select
                :disabled="
                  !state.labelForm.showQuota ||
                  state.labelForm.quotaLabelFormatter.type === 'percent'
                "
                :effect="themes"
                v-model="state.labelForm.quotaLabelFormatter.unit"
                :placeholder="t('chart.pls_select_field')"
                size="small"
                @change="changeLabelAttr('quotaLabelFormatter')"
              >
                <el-option
                  v-for="item in unitList"
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
                @change="changeLabelAttr('quotaLabelFormatter')"
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
            @change="changeLabelAttr('quotaLabelFormatter')"
            :label="t('chart.value_formatter_thousand_separator')"
          />
        </el-form-item>
      </div>
    </template>
    <template v-if="showProperty('showProportion')">
      <el-form-item class="form-item form-item-checkbox" :class="'form-item-' + themes">
        <el-checkbox
          :effect="themes"
          size="small"
          @change="changeLabelAttr('showProportion')"
          v-model="state.labelForm.showProportion"
          label="proportion"
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
        :effect="themes"
        v-model="state.labelForm.reserveDecimalCount"
        @change="changeLabelAttr('reserveDecimalCount')"
      >
        <el-option :label="t('chart.reserve_zero')" :value="0" />
        <el-option :label="t('chart.reserve_one')" :value="1" />
        <el-option :label="t('chart.reserve_two')" :value="2" />
      </el-select>
    </el-form-item>
    <div v-if="showProperty('seriesLabelFormatter')">
      <el-form-item>
        <el-select v-model="curSeriesFormatter" value-key="id" class="series-select">
          <template #prefix>
            <el-icon style="font-size: 14px">
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
            :label="`${item.chartShowName ?? item.name}${
              item.summary !== '' ? '(' + t('chart.' + item.summary) + ')' : ''
            }`"
            v-for="item in state.labelForm.seriesLabelFormatter"
          >
            <el-icon style="margin-right: 8px">
              <Icon
                :className="`field-icon-${fieldType[item.deType]}`"
                :name="`field_${fieldType[item.deType]}`"
              />
            </el-icon>
            {{ item.chartShowName ?? item.name }}
            {{ item.summary !== '' ? '(' + t('chart.' + item.summary) + ')' : '' }}
          </el-option>
        </el-select>
      </el-form-item>
      <template v-if="curSeriesFormatter?.id">
        <el-form-item class="form-item form-item-checkbox" :class="'form-item-' + themes">
          <el-checkbox
            size="small"
            @change="changeLabelAttr('seriesLabelFormatter')"
            v-model="curSeriesFormatter.show"
            label="quota"
          >
            {{ t('chart.show') }}
          </el-checkbox>
        </el-form-item>

        <div style="padding-left: 22px">
          <el-row :gutter="8">
            <el-col :span="8">
              <el-form-item
                class="form-item"
                :class="'form-item-' + themes"
                :label="t('chart.text')"
              >
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
            </el-col>
            <el-col :span="12" style="display: flex; align-items: end">
              <el-form-item class="form-item" :class="'form-item-' + themes">
                <el-select
                  :disabled="!curSeriesFormatter.show"
                  style="width: 100%"
                  :effect="themes"
                  v-model.number="curSeriesFormatter.fontSize"
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
              @change="changeLabelAttr('quotaLabelFormatter')"
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
              @change="changeLabelAttr('quotaLabelFormatter')"
            />
          </el-form-item>

          <el-row
            :gutter="8"
            v-if="curSeriesFormatter.show && curSeriesFormatter.formatterCfg.type !== 'percent'"
          >
            <el-col :span="12">
              <el-form-item
                :label="t('chart.value_formatter_unit')"
                class="form-item"
                :class="'form-item-' + themes"
              >
                <el-select
                  :disabled="
                    !curSeriesFormatter.show || curSeriesFormatter.formatterCfg.type === 'percent'
                  "
                  :effect="props.themes"
                  v-model="curSeriesFormatter.formatterCfg.unit"
                  :placeholder="t('chart.pls_select_field')"
                  size="small"
                  @change="changeLabelAttr('quotaLabelFormatter')"
                >
                  <el-option
                    v-for="item in unitList"
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
                  @change="changeLabelAttr('quotaLabelFormatter')"
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
              @change="changeLabelAttr('quotaLabelFormatter')"
              :label="t('chart.value_formatter_thousand_separator')"
            />
          </el-form-item>
        </div>
      </template>
    </div>
  </el-form>
</template>

<style lang="less" scoped>
.form-item-checkbox {
  margin-bottom: 8px !important;
}
.series-select {
  ::v-deep(.ed-select__prefix--light) {
    padding-right: unset;
    border-right: unset;
  }
}
</style>
<style lang="less">
.series-select-option {
  display: flex;
  align-items: center;
  justify-content: start;
  padding: 0 11px;
}
</style>
