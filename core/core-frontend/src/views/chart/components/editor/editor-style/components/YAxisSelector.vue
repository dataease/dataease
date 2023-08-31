<script lang="tsx" setup>
import { PropType, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_YAXIS_STYLE } from '@/views/chart/components/editor/util/chart'
import { formatterType, unitType } from '@/views/chart/components/editor/util/formatter'
import { ElMessage } from 'element-plus-secondary'

const { t } = useI18n()

const props = defineProps({
  themes: {
    type: String as PropType<'light' | 'dark' | 'plain'>,
    default: 'dark'
  },
  chart: {
    type: Object,
    required: true
  },
  propertyInner: {
    type: Array<string>
  }
})

const predefineColors = COLOR_PANEL
const typeList = formatterType
const unitList = unitType

const state = reactive({
  axisForm: JSON.parse(JSON.stringify(DEFAULT_YAXIS_STYLE)),
  fontSize: []
})

const emit = defineEmits(['onChangeYAxisForm'])

watch(
  () => props.chart.customStyle.yAxis,
  () => {
    init()
  },
  { deep: true }
)

const initFontSize = () => {
  const arr = []
  for (let i = 10; i <= 40; i = i + 2) {
    arr.push({
      name: i + '',
      value: i
    })
  }
  state.fontSize = arr
}

const changeAxisStyle = prop => {
  if (
    state.axisForm.axisValue.splitCount &&
    (parseInt(state.axisForm.axisValue.splitCount) > 100 ||
      parseInt(state.axisForm.axisValue.splitCount) < 0)
  ) {
    ElMessage.error(t('chart.splitCount_less_100'))
    return
  }
  emit('onChangeYAxisForm', state.axisForm)
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
    if (customStyle.yAxis) {
      state.axisForm = customStyle.yAxis
    }
  }
}

const showProperty = prop => props.propertyInner?.includes(prop)

initFontSize()
init()
</script>

<template>
  <div style="width: 100%">
    <el-col>
      <el-form
        ref="axisForm"
        :disabled="!state.axisForm.show"
        :model="state.axisForm"
        size="small"
        label-position="top"
      >
        <el-form-item :label="t('chart.position')" v-if="showProperty('hPosition')">
          <el-radio-group
            v-model="state.axisForm.position"
            size="small"
            @change="changeAxisStyle('position')"
          >
            <el-radio :effect="props.themes" label="bottom">{{
              t('chart.text_pos_left')
            }}</el-radio>
            <el-radio :effect="props.themes" label="top">{{ t('chart.text_pos_right') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('chart.position')" v-if="showProperty('vPosition')">
          <el-radio-group
            v-model="state.axisForm.position"
            size="small"
            @change="changeAxisStyle('position')"
          >
            <el-radio :effect="props.themes" label="left">{{ t('chart.text_pos_left') }}</el-radio>
            <el-radio :effect="props.themes" label="right">{{
              t('chart.text_pos_right')
            }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('chart.name')" v-if="showProperty('name')">
          <el-input
            :effect="props.themes"
            v-model="state.axisForm.name"
            size="small"
            @blur="changeAxisStyle('name')"
          />
        </el-form-item>

        <div class="custom-form-item-label">{{ t('chart.name') }}{{ t('chart.text') }}</div>
        <div style="display: flex">
          <el-form-item v-if="showProperty('color')" style="padding-right: 4px">
            <el-color-picker
              v-model="state.axisForm.color"
              class="color-picker-style"
              :predefine="predefineColors"
              @change="changeAxisStyle('color')"
              is-custom
            />
          </el-form-item>
          <el-form-item v-if="showProperty('fontSize')" style="padding-left: 4px">
            <el-select
              style="width: 108px"
              :effect="props.themes"
              v-model="state.axisForm.fontSize"
              :placeholder="t('chart.axis_name_fontsize')"
              @change="changeAxisStyle('fontSize')"
            >
              <el-option
                v-for="option in state.fontSize"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </div>

        <template v-if="showProperty('axisValue')">
          <el-divider />

          <div style="display: flex; flex-direction: row; justify-content: space-between">
            <div class="custom-form-item-label">
              {{ t('chart.axis_value') }}
              <el-tooltip class="item" effect="dark" placement="top">
                <template #content><span v-html="t('chart.axis_tip')"></span></template>
                <span style="vertical-align: middle">
                  <el-icon style="cursor: pointer">
                    <Icon name="icon_info_outlined" />
                  </el-icon>
                </span>
              </el-tooltip>
            </div>

            <el-form-item>
              <el-checkbox
                :effect="props.themes"
                v-model="state.axisForm.axisValue.auto"
                @change="changeAxisStyle('axisValue')"
              >
                {{ t('chart.axis_auto') }}
              </el-checkbox>
            </el-form-item>
          </div>

          <template v-if="showProperty('axisValue') && !state.axisForm.axisValue.auto">
            <el-row :gutter="8">
              <el-col :span="12">
                <el-form-item :label="t('chart.axis_value_max')">
                  <el-input-number
                    controls-position="right"
                    :effect="props.themes"
                    v-model.number="state.axisForm.axisValue.max"
                    @blur="changeAxisStyle('axisValue')"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="t('chart.axis_value_min')">
                  <el-input-number
                    controls-position="right"
                    :effect="props.themes"
                    v-model.number="state.axisForm.axisValue.min"
                    @blur="changeAxisStyle('axisValue')"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <div class="custom-form-item-label">
              {{ t('chart.axis_value_split_count') }}
              <el-tooltip class="item" effect="dark" placement="top">
                <template #content>期望的坐标轴刻度数量，非最终结果。</template>
                <span style="vertical-align: middle">
                  <el-icon style="cursor: pointer">
                    <Icon name="icon_info_outlined" />
                  </el-icon>
                </span>
              </el-tooltip>
            </div>

            <el-form-item>
              <el-input-number
                style="width: 100%"
                :effect="props.themes"
                v-model.number="state.axisForm.axisValue.splitCount"
                @blur="changeAxisStyle('axisValue')"
              />
            </el-form-item>
          </template>
        </template>
        <el-divider />
        <el-form-item v-if="showProperty('axisLine')">
          <el-checkbox
            :effect="props.themes"
            v-model="state.axisForm.axisLine.show"
            @change="changeAxisStyle('axisLine')"
          >
            {{ t('chart.axis_show') }}
          </el-checkbox>
        </el-form-item>
        <el-form-item class="form-item-checkbox" v-if="showProperty('splitLine')">
          <el-checkbox
            :effect="props.themes"
            v-model="state.axisForm.splitLine.show"
            @change="changeAxisStyle('splitLine')"
          >
            {{ t('chart.grid_show') }}
          </el-checkbox>
        </el-form-item>
        <div
          style="display: flex"
          v-if="showProperty('splitLine') && state.axisForm.splitLine.show"
        >
          <div style="width: 22px"></div>
          <div style="flex: 1; display: flex">
            <el-form-item style="padding-right: 4px">
              <el-color-picker
                v-model="state.axisForm.splitLine.lineStyle.color"
                :predefine="predefineColors"
                @change="changeAxisStyle('splitLine')"
                is-custom
              />
            </el-form-item>
            <el-form-item class="form-item form-item-slider" style="padding-left: 4px">
              <el-input-number
                style="width: 108px"
                :effect="props.themes"
                v-model="state.axisForm.splitLine.lineStyle.width"
                :min="1"
                :max="10"
                size="small"
                controls-position="right"
                @change="changeAxisStyle('splitLine')"
              />
            </el-form-item>
          </div>
        </div>
        <el-divider />
        <el-form-item class="form-item-checkbox" v-if="showProperty('axisLabel')">
          <el-checkbox
            :effect="props.themes"
            v-model="state.axisForm.axisLabel.show"
            @change="changeAxisStyle('axisLabel')"
          >
            {{ t('chart.axis_label_show') }}
          </el-checkbox>
        </el-form-item>
        <div
          style="display: flex"
          v-if="showProperty('axisLabel') && state.axisForm.axisLabel.show"
        >
          <div style="width: 22px"></div>
          <div style="flex: 1">
            <div class="custom-form-item-label">
              {{ t('chart.text') }}
            </div>
            <div style="display: flex">
              <el-form-item style="padding-right: 4px">
                <el-color-picker
                  v-model="state.axisForm.axisLabel.color"
                  :predefine="predefineColors"
                  @change="changeAxisStyle('axisLabel')"
                  is-custom
                />
              </el-form-item>
              <el-form-item style="padding-left: 4px">
                <el-select
                  style="width: 108px"
                  :effect="props.themes"
                  v-model="state.axisForm.axisLabel.fontSize"
                  :placeholder="t('chart.axis_label_fontsize')"
                  @change="changeAxisStyle('axisLabel')"
                >
                  <el-option
                    v-for="option in state.fontSize"
                    :key="option.value"
                    :label="option.name"
                    :value="option.value"
                  />
                </el-select>
              </el-form-item>
            </div>

            <el-form-item :label="t('chart.rotate')" class="form-item form-item-slider">
              <el-input-number
                style="width: 100%"
                :effect="props.themes"
                v-model="state.axisForm.axisLabel.rotate"
                :min="-90"
                :max="90"
                size="small"
                controls-position="right"
                @change="changeAxisStyle('axisLabel')"
              />
            </el-form-item>

            <template v-if="showProperty('axisLabelFormatter')">
              <el-form-item :label="t('chart.value_formatter_type')">
                <el-select
                  style="width: 100%"
                  :effect="props.themes"
                  v-model="state.axisForm.axisLabelFormatter.type"
                  @change="changeAxisStyle('axisLabelFormatter')"
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
                :label="t('chart.value_formatter_decimal_count')"
              >
                <el-input-number
                  style="width: 100%"
                  :effect="props.themes"
                  v-model="state.axisForm.axisLabelFormatter.decimalCount"
                  :precision="0"
                  :min="0"
                  :max="10"
                  size="small"
                  @change="changeAxisStyle('axisLabelFormatter')"
                />
              </el-form-item>

              <el-row :gutter="8">
                <el-col :span="12">
                  <el-form-item
                    v-if="state.axisForm.axisLabelFormatter.type !== 'percent'"
                    :label="t('chart.value_formatter_unit')"
                  >
                    <el-select
                      :effect="props.themes"
                      v-model="state.axisForm.axisLabelFormatter.unit"
                      :placeholder="t('chart.pls_select_field')"
                      size="small"
                      @change="changeAxisStyle('axisLabelFormatter')"
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
                  <el-form-item :label="t('chart.value_formatter_suffix')">
                    <el-input
                      :effect="props.themes"
                      v-model="state.axisForm.axisLabelFormatter.suffix"
                      size="small"
                      clearable
                      :placeholder="t('commons.input_content')"
                      @change="changeAxisStyle('axisLabelFormatter')"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item>
                <el-checkbox
                  :effect="props.themes"
                  v-model="state.axisForm.axisLabelFormatter.thousandSeparator"
                  @change="changeAxisStyle('axisLabelFormatter')"
                  :label="t('chart.value_formatter_thousand_separator')"
                />
              </el-form-item>
            </template>
          </div>
        </div>
      </el-form>
    </el-col>
  </div>
</template>

<style lang="less" scoped>
:deep(.ed-color-picker.is-custom .ed-color-picker__trigger) {
  height: 24px;
}
.custom-form-item-label {
  margin-bottom: 4px;
  line-height: 20px;
  color: #a6a6a6;
  font-size: 12px;
  padding: 2px 12px 0 0;
}
.form-item-checkbox {
  margin-bottom: 10px !important;
}
</style>
