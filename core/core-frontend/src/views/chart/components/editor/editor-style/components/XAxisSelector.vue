<script lang="tsx" setup>
import { reactive, toRefs } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_XAXIS_STYLE } from '@/views/chart/components/editor/util/chart'
import { formatterType, unitType } from '@/views/chart/components/editor/util/formatter'
import { ElMessage } from 'element-plus-secondary'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object,
    required: true
  }
})

const predefineColors = COLOR_PANEL
const typeList = formatterType
const unitList = unitType

const state = reactive({
  axisForm: JSON.parse(JSON.stringify(DEFAULT_XAXIS_STYLE)),
  fontSize: []
})

const emit = defineEmits(['onChangeXAxisForm'])

const initFontSize = () => {
  const arr = []
  for (let i = 10; i <= 40; i = i + 2) {
    arr.push({
      name: i + '',
      value: i + ''
    })
  }
  state.fontSize = arr
}

const changeAxisStyle = () => {
  if (
    state.axisForm.axisValue.splitCount &&
    (parseInt(state.axisForm.axisValue.splitCount) > 100 ||
      parseInt(state.axisForm.axisValue.splitCount) < 0)
  ) {
    ElMessage.success(t('chart.splitCount_less_100'))
    return
  }
  emit('onChangeXAxisForm', state.axisForm)
}

initFontSize()
</script>

<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="axisForm" :model="state.axisForm" label-width="80px" size="small">
        <el-form-item :label="t('chart.show')" class="form-item">
          <el-checkbox v-model="state.axisForm.show" @change="changeAxisStyle('show')">{{
            t('chart.show')
          }}</el-checkbox>
        </el-form-item>
        <div v-show="state.axisForm.show">
          <el-form-item :label="t('chart.position')" class="form-item">
            <el-radio-group
              v-model="state.axisForm.position"
              size="small"
              @change="changeAxisStyle('position')"
            >
              <div v-if="!props.chart.type.includes('horizontal')">
                <el-radio-button label="top">{{ t('chart.text_pos_top') }}</el-radio-button>
                <el-radio-button label="bottom">{{ t('chart.text_pos_bottom') }}</el-radio-button>
              </div>
              <div v-else>
                <el-radio-button label="left">{{ t('chart.text_pos_left') }}</el-radio-button>
                <el-radio-button label="right">{{ t('chart.text_pos_center') }}</el-radio-button>
              </div>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="t('chart.name')" class="form-item">
            <el-input v-model="state.axisForm.name" size="small" @blur="changeAxisStyle('name')" />
          </el-form-item>
          <el-form-item :label="t('chart.axis_name_color')" class="form-item">
            <el-color-picker
              v-model="state.axisForm.nameTextStyle.color"
              class="color-picker-style"
              :predefine="predefineColors"
              @change="changeAxisStyle('nameTextStyle')"
            />
          </el-form-item>
          <el-form-item :label="t('chart.axis_name_fontsize')" class="form-item">
            <el-select
              v-model="state.axisForm.nameTextStyle.fontSize"
              :placeholder="t('chart.axis_name_fontsize')"
              @change="changeAxisStyle('nameTextStyle')"
            >
              <el-option
                v-for="option in state.fontSize"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <span>
            <el-divider />
            <el-form-item class="form-item">
              <span>
                <span class="span-box">
                  <span>{{ t('chart.axis_value') }}</span>
                  <el-tooltip class="item" effect="dark" placement="bottom">
                    <template #content>{{ t('chart.axis_tip') }}</template>
                    <i class="el-icon-info" style="cursor: pointer" />
                  </el-tooltip>
                </span>
              </span>
              <el-checkbox
                v-model="state.axisForm.axisValue.auto"
                @change="changeAxisStyle('axisValue')"
                >{{ t('chart.axis_auto') }}</el-checkbox
              >
            </el-form-item>
            <span v-show="!state.axisForm.axisValue.auto">
              <el-form-item :label="t('chart.axis_value_min')" class="form-item">
                <el-input
                  v-model="state.axisForm.axisValue.min"
                  @blur="changeAxisStyle('axisValue')"
                />
              </el-form-item>
              <el-form-item :label="t('chart.axis_value_max')" class="form-item">
                <el-input
                  v-model="state.axisForm.axisValue.max"
                  @blur="changeAxisStyle('axisValue')"
                />
              </el-form-item>
              <el-form-item :label="t('chart.axis_value_split_count')" class="form-item">
                <span>
                  <span class="span-box">
                    <span>{{ t('chart.axis_value_split_count') }}</span>
                    <el-tooltip class="item" effect="dark" placement="bottom">
                      <template #content>期望的坐标轴刻度数量，非最终结果。</template>
                      <i class="el-icon-info" style="cursor: pointer" />
                    </el-tooltip>
                  </span>
                </span>
                <el-input
                  v-model="state.axisForm.axisValue.splitCount"
                  @blur="changeAxisStyle('axisValue')"
                />
              </el-form-item>
            </span>
          </span>
          <el-divider />
          <el-form-item :label="t('chart.axis_show')" class="form-item">
            <el-checkbox
              v-model="state.axisForm.axisLine.show"
              @change="changeAxisStyle('axisLine')"
              >{{ t('chart.axis_show') }}</el-checkbox
            >
          </el-form-item>
          <el-form-item :label="t('chart.grid_show')" class="form-item">
            <el-checkbox
              v-model="state.axisForm.splitLine.show"
              @change="changeAxisStyle('splitLine')"
              >{{ t('chart.grid_show') }}</el-checkbox
            >
          </el-form-item>
          <span v-show="state.axisForm.splitLine.show">
            <el-form-item :label="t('chart.grid_color')" class="form-item">
              <el-color-picker
                v-model="state.axisForm.splitLine.lineStyle.color"
                class="el-color-picker"
                :predefine="predefineColors"
                @change="changeAxisStyle('splitLine')"
              />
            </el-form-item>
            <el-form-item :label="t('chart.grid_width')" class="form-item form-item-slider">
              <el-slider
                v-model="state.axisForm.splitLine.lineStyle.width"
                :min="1"
                :max="10"
                show-input
                :show-input-controls="false"
                input-size="small"
                @change="changeAxisStyle('splitLine')"
              />
            </el-form-item>
          </span>
          <el-divider />
          <el-form-item :label="t('chart.axis_label_show')" class="form-item">
            <el-checkbox
              v-model="state.axisForm.axisLabel.show"
              @change="changeAxisStyle('axisLabel')"
              >{{ t('chart.axis_label_show') }}</el-checkbox
            >
          </el-form-item>
          <span v-show="state.axisForm.axisLabel.show">
            <el-form-item :label="t('chart.axis_label_color')" class="form-item">
              <el-color-picker
                v-model="state.axisForm.axisLabel.color"
                class="el-color-picker"
                :predefine="predefineColors"
                @change="changeAxisStyle('axisLabel')"
              />
            </el-form-item>
            <el-form-item :label="t('chart.axis_label_rotate')" class="form-item form-item-slider">
              <el-slider
                v-model="state.axisForm.axisLabel.rotate"
                show-input
                :show-input-controls="false"
                :min="-90"
                :max="90"
                input-size="small"
                @change="changeAxisStyle('axisLabel')"
              />
            </el-form-item>
            <el-form-item :label="t('chart.axis_label_fontsize')" class="form-item">
              <el-select
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

            <span v-show="props.chart.type && props.chart.type.includes('horizontal')">
              <el-form-item :label="t('chart.value_formatter_type')" class="form-item">
                <el-select
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
                v-show="state.axisForm.axisLabelFormatter.type !== 'auto'"
                :label="t('chart.value_formatter_decimal_count')"
                class="form-item"
              >
                <el-input-number
                  v-model="state.axisForm.axisLabelFormatter.decimalCount"
                  :precision="0"
                  :min="0"
                  :max="10"
                  size="small"
                  @change="changeAxisStyle('axisLabelFormatter')"
                />
              </el-form-item>

              <el-form-item
                v-show="state.axisForm.axisLabelFormatter.type !== 'percent'"
                :label="t('chart.value_formatter_unit')"
                class="form-item"
              >
                <el-select
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

              <el-form-item :label="t('chart.value_formatter_suffix')" class="form-item">
                <el-input
                  v-model="state.axisForm.axisLabelFormatter.suffix"
                  size="small"
                  clearable
                  :placeholder="t('commons.input_content')"
                  @change="changeAxisStyle('axisLabelFormatter')"
                />
              </el-form-item>

              <el-form-item
                :label="t('chart.value_formatter_thousand_separator')"
                class="form-item"
              >
                <el-checkbox
                  v-model="state.axisForm.axisLabelFormatter.thousandSeparator"
                  @change="changeAxisStyle('axisLabelFormatter')"
                />
              </el-form-item>
            </span>
          </span>
        </div>
      </el-form>
    </el-col>
  </div>
</template>

<style lang="less" scoped></style>
