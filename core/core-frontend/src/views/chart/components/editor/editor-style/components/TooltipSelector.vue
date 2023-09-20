<script lang="tsx" setup>
import { PropType, computed, onMounted, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_TOOLTIP } from '@/views/chart/components/editor/util/chart'
import { ElSpace } from 'element-plus-secondary'
import cloneDeep from 'lodash-es/cloneDeep'
import defaultsDeep from 'lodash-es/defaultsDeep'
import { formatterType, unitList } from '../../../js/formatter'

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

const emit = defineEmits(['onTooltipChange'])

watch(
  () => props.chart.customAttr.tooltip,
  () => {
    init()
  },
  { deep: true }
)

const state = reactive({
  tooltipForm: JSON.parse(JSON.stringify(DEFAULT_TOOLTIP))
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

const changeTooltipAttr = val => {
  emit('onTooltipChange', state.tooltipForm)
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
    if (customAttr.tooltip) {
      state.tooltipForm = defaultsDeep(customAttr.tooltip, cloneDeep(DEFAULT_TOOLTIP))
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
        v-show="showProperty('color')"
        :label="t('chart.text')"
      >
        <el-color-picker
          :effect="themes"
          v-model="state.tooltipForm.color"
          class="color-picker-style"
          :predefine="predefineColors"
          @change="changeTooltipAttr('textStyle')"
          is-custom
        />
      </el-form-item>

      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        v-show="showProperty('fontSize')"
      >
        <template #label>&nbsp;</template>
        <el-select
          style="width: 108px"
          :effect="themes"
          v-model.number="state.tooltipForm.fontSize"
          :placeholder="t('chart.text_fontsize')"
          @change="changeTooltipAttr('textStyle')"
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
      <el-row :gutter="8">
        <el-col :span="state.tooltipForm.tooltipFormatter.type !== 'auto' ? 12 : 24">
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
        </el-col>
        <el-col :span="12" v-if="state.tooltipForm.tooltipFormatter.type !== 'auto'">
          <el-form-item
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
        </el-col>
      </el-row>

      <el-row :gutter="8">
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
  </el-form>
</template>

<style lang="less" scoped></style>
