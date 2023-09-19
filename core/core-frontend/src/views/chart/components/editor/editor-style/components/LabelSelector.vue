<script lang="tsx" setup>
import { PropType, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_LABEL } from '@/views/chart/components/editor/util/chart'
import { formatterType } from '../../util/formatter'
import { unitList } from '../../../js/formatter'
import { ElSpace } from 'element-plus-secondary'

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

const predefineColors = COLOR_PANEL

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
const typeList = formatterType

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
      state.labelForm = customAttr.label
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
          :predefine="predefineColors"
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

    <template v-if="showProperty('gaugeLabelFormatter')">
      <el-form-item
        :label="$t('chart.value_formatter_type')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-select
          :effect="themes"
          v-model="state.labelForm.gaugeLabelFormatter.type"
          @change="changeLabelAttr('gaugeLabelFormatter')"
        >
          <el-option
            v-for="type in typeList"
            :key="type.value"
            :label="$t('chart.' + type.name)"
            :value="type.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="state.labelForm.gaugeLabelFormatter.type !== 'auto'"
        :label="$t('chart.value_formatter_decimal_count')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-input-number
          :effect="themes"
          v-model="state.labelForm.gaugeLabelFormatter.decimalCount"
          :precision="0"
          :min="0"
          :max="10"
          @change="changeLabelAttr('gaugeLabelFormatter')"
        />
      </el-form-item>

      <el-row :gutter="8">
        <el-col :span="12">
          <el-form-item
            v-if="state.labelForm.gaugeLabelFormatter.type !== 'percent'"
            :label="$t('chart.value_formatter_unit')"
            class="form-item"
            :class="'form-item-' + themes"
          >
            <el-select
              :effect="themes"
              v-model="state.labelForm.gaugeLabelFormatter.unit"
              :placeholder="$t('chart.pls_select_field')"
              @change="changeLabelAttr('gaugeLabelFormatter')"
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
              v-model="state.labelForm.gaugeLabelFormatter.suffix"
              clearable
              :placeholder="$t('commons.input_content')"
              @change="changeLabelAttr('gaugeLabelFormatter')"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item class="form-item" :class="'form-item-' + themes">
        <el-checkbox
          :effect="themes"
          v-model="state.labelForm.gaugeLabelFormatter.thousandSeparator"
          @change="changeLabelAttr('gaugeLabelFormatter')"
          :label="t('chart.value_formatter_thousand_separator')"
        />
      </el-form-item>
    </template>
  </el-form>
</template>

<style lang="less" scoped></style>
