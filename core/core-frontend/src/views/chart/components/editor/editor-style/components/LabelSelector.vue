<script lang="ts" setup>
import { PropType, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_LABEL } from '@/views/chart/components/editor/util/chart'
import { ElSpace } from 'element-plus-secondary'
import { formatterType, unitList } from '../../../js/formatter'
import { defaultsDeep, cloneDeep } from 'lodash-es'

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

watch(
  () => props.chart.customAttr.label,
  () => {
    init()
  },
  { deep: true }
)

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

const state = reactive({
  labelForm: JSON.parse(JSON.stringify(DEFAULT_LABEL)),
  fontSize: []
})

const emit = defineEmits(['onLabelChange'])

const changeLabelAttr = prop => {
  emit('onLabelChange', state.labelForm)
}

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
    }
  }
}
const showProperty = prop => props.propertyInner?.includes(prop)
initFontSize()
init()
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
            v-for="option in state.fontSize"
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
            v-for="type in unitList"
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
          :effect="themes"
          v-model="state.labelForm.labelFormatter.decimalCount"
          :precision="0"
          :min="0"
          :max="10"
          @change="changeLabelAttr('labelFormatter')"
        />
      </el-form-item>

      <el-row :gutter="8">
        <el-col :span="12">
          <el-form-item
            v-if="state.labelForm.labelFormatter.type !== 'percent'"
            :label="$t('chart.value_formatter_unit')"
            class="form-item"
            :class="'form-item-' + themes"
          >
            <el-select
              :effect="themes"
              v-model="state.labelForm.labelFormatter.unit"
              :placeholder="$t('chart.pls_select_field')"
              @change="changeLabelAttr('labelFormatter')"
            >
              <el-option
                v-for="item in unitList"
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
    <el-divider />
    <el-form-item class="form-item" v-show="showProperty('showDimension')">
      <el-checkbox
        @change="changeLabelAttr('showDimension')"
        v-model="state.labelForm.showDimension"
        label="dimension"
        >{{ t('chart.dimension') }}</el-checkbox
      >
    </el-form-item>
    <el-form-item class="form-item" v-show="showProperty('showQuota')">
      <el-checkbox
        @change="changeLabelAttr('showQuota')"
        v-model="state.labelForm.showQuota"
        label="quota"
        >{{ t('chart.quota') }}</el-checkbox
      >
    </el-form-item>
    <template v-if="showProperty('showQuota') && state.labelForm.showQuota">
      <el-form-item :label="t('chart.value_formatter_type')" class="form-item">
        <el-select
          style="width: 100%"
          :effect="props.themes"
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
      >
        <el-input-number
          style="width: 100%"
          :effect="props.themes"
          v-model="state.labelForm.quotaLabelFormatter.decimalCount"
          :precision="0"
          :min="0"
          :max="10"
          size="small"
          @change="changeLabelAttr('quotaLabelFormatter')"
        />
      </el-form-item>

      <el-row :gutter="8">
        <el-col :span="12">
          <el-form-item :label="t('chart.value_formatter_unit')" class="form-item">
            <el-select
              :disabled="state.labelForm.quotaLabelFormatter.type == 'percent'"
              :effect="props.themes"
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
          <el-form-item :label="t('chart.value_formatter_suffix')">
            <el-input
              :effect="props.themes"
              v-model="state.labelForm.quotaLabelFormatter.suffix"
              size="small"
              clearable
              :placeholder="t('commons.input_content')"
              @change="changeLabelAttr('quotaLabelFormatter')"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item class="form-item">
        <el-checkbox
          :effect="props.themes"
          v-model="state.labelForm.quotaLabelFormatter.thousandSeparator"
          @change="changeLabelAttr('quotaLabelFormatter')"
          :label="t('chart.value_formatter_thousand_separator')"
        />
      </el-form-item>
    </template>
    <el-form-item class="form-item" v-show="showProperty('showProportion')">
      <el-checkbox
        @change="changeLabelAttr('showProportion')"
        v-model="state.labelForm.showProportion"
        label="proportion"
        >{{ t('chart.proportion') }}</el-checkbox
      >
    </el-form-item>
    <el-form-item
      v-show="showProperty('showProportion') && state.labelForm.showProportion"
      :label="t('chart.label_reserve_decimal_count')"
      class="form-item"
    >
      <el-select
        v-model="state.labelForm.reserveDecimalCount"
        @change="changeLabelAttr('reserveDecimalCount')"
      >
        <el-option :label="t('chart.reserve_zero')" :value="0" />
        <el-option :label="t('chart.reserve_one')" :value="1" />
        <el-option :label="t('chart.reserve_two')" :value="2" />
      </el-select>
    </el-form-item>
    <el-form-item
      v-show="showProperty('reserveDecimalCount')"
      :label="t('chart.label_reserve_decimal_count')"
      class="form-item"
    >
      <el-select
        v-model="state.labelForm.reserveDecimalCount"
        @change="changeLabelAttr('reserveDecimalCount')"
      >
        <el-option :label="t('chart.reserve_zero')" :value="0" />
        <el-option :label="t('chart.reserve_one')" :value="1" />
        <el-option :label="t('chart.reserve_two')" :value="2" />
      </el-select>
    </el-form-item>
  </el-form>
</template>

<style lang="less" scoped></style>
