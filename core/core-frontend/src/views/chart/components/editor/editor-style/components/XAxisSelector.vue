<script lang="tsx" setup>
import { computed, onMounted, PropType, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_XAXIS_STYLE } from '@/views/chart/components/editor/util/chart'
import { formatterType, unitType } from '@/views/chart/components/js/formatter'
import { ElMessage } from 'element-plus-secondary'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object,
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
const typeList = formatterType
const unitList = unitType

const state = reactive({
  axisForm: JSON.parse(JSON.stringify(DEFAULT_XAXIS_STYLE))
})
const toolTip = computed(() => {
  return props.themes === 'dark' ? 'ndark' : 'dark'
})
const emit = defineEmits(['onChangeXAxisForm'])

watch(
  () => props.chart.customStyle.xAxis,
  () => {
    init()
  },
  { deep: true }
)

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

const splitLineStyle = [
  { label: t('chart.line_type_solid'), value: 'solid' },
  { label: t('chart.line_type_dashed'), value: 'dashed' },
  { label: t('chart.line_type_dotted'), value: 'dotted' }
]

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

const changeAxisStyle = prop => {
  if (
    state.axisForm.axisValue.splitCount &&
    (state.axisForm.axisValue.splitCount > 100 || state.axisForm.axisValue.splitCount < 0)
  ) {
    ElMessage.error(t('chart.splitCount_less_100'))
    return
  }
  emit('onChangeXAxisForm', state.axisForm, prop)
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customStyle) {
    let customStyle = null
    if (Object.prototype.toString.call(chart.customStyle) === '[object Object]') {
      customStyle = JSON.parse(JSON.stringify(chart.customStyle))
    } else {
      customStyle = JSON.parse(chart.customStyle)
    }
    if (customStyle.xAxis) {
      state.axisForm = customStyle.xAxis
    }
  }
}

const showProperty = prop => props.propertyInner?.includes(prop)

const isBidirectionalBar = computed(() => {
  return props.chart.type === 'bidirectional-bar'
})

const isHorizontalLayout = computed(() => {
  return props.chart.customAttr.basicStyle.layout === 'horizontal'
})

onMounted(() => {
  init()
})
</script>

<template>
  <el-form
    ref="axisForm"
    :disabled="!state.axisForm.show"
    :model="state.axisForm"
    size="small"
    label-position="top"
  >
    <el-form-item
      class="form-item"
      :class="'form-item-' + themes"
      :label="t('chart.position')"
      v-if="showProperty('position')"
    >
      <el-radio-group
        v-model="state.axisForm.position"
        size="small"
        @change="changeAxisStyle('position')"
      >
        <div v-if="isBidirectionalBar">
          <el-radio :effect="props.themes" label="top">{{
            isHorizontalLayout ? t('chart.text_pos_left') : t('chart.text_pos_top')
          }}</el-radio>
          <el-radio :effect="props.themes" label="bottom">{{
            t('chart.text_pos_center')
          }}</el-radio>
        </div>
        <div v-else>
          <el-radio :effect="props.themes" label="top">{{ t('chart.text_pos_top') }}</el-radio>
          <el-radio :effect="props.themes" label="bottom">{{
            t('chart.text_pos_bottom')
          }}</el-radio>
        </div>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      class="form-item"
      :class="'form-item-' + themes"
      :label="t('chart.name')"
      v-if="showProperty('name')"
    >
      <el-input
        :effect="props.themes"
        v-model="state.axisForm.name"
        size="small"
        maxlength="50"
        @blur="changeAxisStyle('name')"
      />
    </el-form-item>

    <div style="display: flex">
      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        v-if="showProperty('color')"
        :label="t('chart.chart_style')"
      >
        <el-color-picker
          v-model="state.axisForm.color"
          class="color-picker-style"
          :predefine="predefineColors"
          @change="changeAxisStyle('color')"
          :effect="themes"
          is-custom
        />
      </el-form-item>
      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        v-if="showProperty('fontSize')"
        style="padding-left: 4px"
      >
        <template #label>&nbsp;</template>
        <el-tooltip content="字号" :effect="toolTip" placement="top">
          <el-select
            style="width: 108px"
            :effect="props.themes"
            v-model="state.axisForm.fontSize"
            :placeholder="t('chart.axis_name_fontsize')"
            @change="changeAxisStyle('fontSize')"
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
    </div>

    <template v-if="showProperty('axisValue')">
      <el-divider class="m-divider" :class="'m-divider--' + themes" />
      <div style="display: flex; flex-direction: row; justify-content: space-between">
        <label class="custom-form-item-label" :class="'custom-form-item-label--' + themes">
          {{ t('chart.axis_value') }}
          <el-tooltip class="item" :effect="toolTip" placement="top">
            <template #content><span v-html="t('chart.axis_tip')"></span></template>
            <span style="vertical-align: middle">
              <el-icon style="cursor: pointer">
                <Icon name="icon_info_outlined" />
              </el-icon>
            </span>
          </el-tooltip>
        </label>

        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-checkbox
            size="small"
            :effect="props.themes"
            v-model="state.axisForm.axisValue.auto"
            @change="changeAxisStyle('axisValue.auto')"
          >
            {{ t('chart.axis_auto') }}
          </el-checkbox>
        </el-form-item>
      </div>

      <template v-if="showProperty('axisValue') && !state.axisForm.axisValue.auto">
        <el-row :gutter="8">
          <el-col :span="12">
            <el-form-item
              class="form-item"
              :class="'form-item-' + themes"
              :label="t('chart.axis_value_max')"
            >
              <el-input-number
                controls-position="right"
                :effect="props.themes"
                v-model.number="state.axisForm.axisValue.max"
                @change="changeAxisStyle('axisValue.max')"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              class="form-item"
              :class="'form-item-' + themes"
              :label="t('chart.axis_value_min')"
            >
              <el-input-number
                controls-position="right"
                :effect="props.themes"
                v-model.number="state.axisForm.axisValue.min"
                @change="changeAxisStyle('axisValue.min')"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <label class="custom-form-item-label" :class="'custom-form-item-label--' + themes">
          {{ t('chart.axis_value_split_count') }}
          <el-tooltip class="item" :effect="toolTip" placement="top">
            <template #content>期望的坐标轴刻度数量，非最终结果。</template>
            <span style="vertical-align: middle">
              <el-icon style="cursor: pointer">
                <Icon name="icon_info_outlined" />
              </el-icon>
            </span>
          </el-tooltip>
        </label>

        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-input-number
            controls-position="right"
            style="width: 100%"
            :effect="props.themes"
            v-model.number="state.axisForm.axisValue.splitCount"
            @change="changeAxisStyle('axisValue.splitCount')"
          />
        </el-form-item>
      </template>
    </template>
    <el-divider class="m-divider" :class="'m-divider--' + themes" />
    <el-form-item class="form-item" :class="'form-item-' + themes" v-if="showProperty('axisLine')">
      <el-checkbox
        size="small"
        :effect="props.themes"
        v-model="state.axisForm.axisLine.show"
        @change="changeAxisStyle('axisLine.show')"
      >
        {{ t('chart.axis_show') }}
      </el-checkbox>
    </el-form-item>
    <el-form-item
      class="form-item form-item-checkbox"
      :class="{
        'form-item-dark': themes === 'dark'
      }"
      v-if="showProperty('splitLine')"
    >
      <el-checkbox
        size="small"
        :effect="props.themes"
        v-model="state.axisForm.splitLine.show"
        @change="changeAxisStyle('splitLine.show')"
      >
        {{ t('chart.grid_show') }}
      </el-checkbox>
    </el-form-item>
    <div style="padding-left: 22px" v-if="showProperty('splitLine')">
      <div style="flex: 1; display: flex">
        <el-form-item class="form-item" :class="'form-item-' + themes" style="padding-right: 4px">
          <el-color-picker
            :disabled="!state.axisForm.splitLine.show"
            v-model="state.axisForm.splitLine.lineStyle.color"
            :predefine="predefineColors"
            :effect="themes"
            @change="changeAxisStyle('splitLine.lineStyle.color')"
            is-custom
          />
        </el-form-item>
        <el-form-item class="form-item" :class="'form-item-' + themes" style="padding: 0 4px">
          <el-select
            :disabled="!state.axisForm.splitLine.show"
            style="width: 62px"
            :effect="props.themes"
            v-model="state.axisForm.splitLine.lineStyle.style"
            @change="changeAxisStyle('splitLine.lineStyle.style')"
          >
            <el-option
              v-for="option in splitLineStyle"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item class="form-item" :class="'form-item-' + themes" style="padding-left: 4px">
          <el-input-number
            :disabled="!state.axisForm.splitLine.show"
            style="width: 70px"
            :effect="props.themes"
            v-model="state.axisForm.splitLine.lineStyle.width"
            :min="1"
            :max="10"
            size="small"
            controls-position="right"
            @change="changeAxisStyle('splitLine.lineStyle.width')"
          />
        </el-form-item>
      </div>
    </div>
    <el-divider class="m-divider" :class="'m-divider--' + themes" />
    <el-form-item
      class="form-item form-item-checkbox"
      :class="{
        'form-item-dark': themes === 'dark'
      }"
      v-if="showProperty('axisLabel')"
    >
      <el-checkbox
        size="small"
        :effect="props.themes"
        v-model="state.axisForm.axisLabel.show"
        @change="changeAxisStyle('axisLabel.show')"
      >
        {{ t('chart.axis_label_show') }}
      </el-checkbox>
    </el-form-item>

    <div style="padding-left: 22px" v-if="showProperty('axisLabel')">
      <div style="display: flex">
        <el-form-item
          class="form-item"
          :class="'form-item-' + themes"
          style="padding-right: 4px"
          :label="t('chart.text')"
        >
          <el-color-picker
            :disabled="!state.axisForm.axisLabel.show"
            v-model="state.axisForm.axisLabel.color"
            :predefine="predefineColors"
            @change="changeAxisStyle('axisLabel.color')"
            :effect="themes"
            is-custom
          />
        </el-form-item>
        <el-form-item class="form-item" :class="'form-item-' + themes" style="padding-left: 4px">
          <template #label>&nbsp;</template>
          <el-tooltip content="字号" :effect="toolTip" placement="top">
            <el-select
              :disabled="!state.axisForm.axisLabel.show"
              style="width: 108px"
              :effect="props.themes"
              v-model="state.axisForm.axisLabel.fontSize"
              :placeholder="t('chart.axis_label_fontsize')"
              @change="changeAxisStyle('axisLabel.fontSize')"
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
      </div>

      <el-form-item class="form-item" :class="'form-item-' + themes" :label="t('chart.rotate')">
        <el-input-number
          :disabled="!state.axisForm.axisLabel.show"
          style="width: 100%"
          :effect="props.themes"
          v-model="state.axisForm.axisLabel.rotate"
          :min="-90"
          :max="90"
          size="small"
          controls-position="right"
          @change="changeAxisStyle('axisLabel.rotate')"
        />
      </el-form-item>

      <template v-if="showProperty('axisLabelFormatter') && !isBarRangeTime">
        <el-form-item
          class="form-item"
          :class="'form-item-' + themes"
          :label="t('chart.value_formatter_type')"
        >
          <el-select
            :disabled="!state.axisForm.axisLabel.show"
            style="width: 100%"
            :effect="props.themes"
            v-model="state.axisForm.axisLabelFormatter.type"
            @change="changeAxisStyle('axisLabelFormatter.type')"
          >
            <el-option
              v-for="type in typeList"
              :key="type.value"
              :label="t('chart.' + type.name)"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="state.axisForm.axisLabelFormatter.type !== 'auto'"
          class="form-item"
          :class="'form-item-' + themes"
          :label="t('chart.value_formatter_decimal_count')"
        >
          <el-input-number
            :disabled="!state.axisForm.axisLabel.show"
            style="width: 100%"
            :effect="props.themes"
            v-model="state.axisForm.axisLabelFormatter.decimalCount"
            :precision="0"
            :min="0"
            :max="10"
            size="small"
            controls-position="right"
            @change="changeAxisStyle('axisLabelFormatter.decimalCount')"
          />
        </el-form-item>

        <el-row
          :gutter="8"
          v-if="
            state.axisForm.axisLabel.show && state.axisForm.axisLabelFormatter.type !== 'percent'
          "
        >
          <el-col :span="12">
            <el-form-item
              class="form-item"
              :class="'form-item-' + themes"
              :label="t('chart.value_formatter_unit')"
            >
              <el-select
                :effect="props.themes"
                v-model="state.axisForm.axisLabelFormatter.unit"
                :placeholder="t('chart.pls_select_field')"
                size="small"
                @change="changeAxisStyle('axisLabelFormatter.unit')"
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
              class="form-item"
              :class="'form-item-' + themes"
              :label="t('chart.value_formatter_suffix')"
            >
              <el-input
                :disabled="!state.axisForm.axisLabel.show"
                :effect="props.themes"
                v-model="state.axisForm.axisLabelFormatter.suffix"
                size="small"
                clearable
                :placeholder="t('commons.input_content')"
                @change="changeAxisStyle('axisLabelFormatter.suffix')"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-checkbox
            :disabled="!state.axisForm.axisLabel.show"
            size="small"
            :effect="props.themes"
            v-model="state.axisForm.axisLabelFormatter.thousandSeparator"
            @change="changeAxisStyle('axisLabelFormatter.thousandSeparator')"
            :label="t('chart.value_formatter_thousand_separator')"
          />
        </el-form-item>
      </template>
    </div>
  </el-form>
</template>

<style lang="less" scoped>
.custom-form-item-label {
  margin-bottom: 4px;
  line-height: 20px;
  color: #646a73;
  font-size: 12px;
  font-style: normal;
  font-weight: 400;
  padding: 2px 12px 0 0;

  &.custom-form-item-label--dark {
    color: #a6a6a6;
  }
}

.form-item-checkbox {
  margin-bottom: 10px !important;
}
.m-divider {
  border-color: rgba(31, 35, 41, 0.15);
  margin: 0 0 16px;

  &.m-divider--dark {
    border-color: rgba(235, 235, 235, 0.15);
  }
}
</style>
