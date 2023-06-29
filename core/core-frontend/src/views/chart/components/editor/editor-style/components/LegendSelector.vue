<script lang="tsx" setup>
import { reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_LEGEND_STYLE } from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['onLegendChange'])

watch(
  () => props.chart.customStyle,
  () => {
    init()
  },
  { deep: true }
)

const predefineColors = COLOR_PANEL
const iconSymbolOptions = [
  { name: t('chart.line_symbol_circle'), value: 'circle' },
  { name: t('chart.line_symbol_rect'), value: 'square' },
  { name: t('chart.line_symbol_triangle'), value: 'triangle' },
  { name: t('chart.line_symbol_diamond'), value: 'diamond' }
]

const state = reactive({
  legendForm: JSON.parse(JSON.stringify(DEFAULT_LEGEND_STYLE)),
  fontSize: []
})

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

const changeLegendStyle = () => {
  emit('onLegendChange', state.legendForm)
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
    if (customStyle.legend) {
      state.legendForm = customStyle.legend
    }
  }
}

initFontSize()
init()
</script>

<template>
  <div style="width: 100%">
    <el-col>
      <el-form
        ref="legendForm"
        :disabled="!state.legendForm.show"
        :model="state.legendForm"
        label-width="80px"
        size="small"
      >
        <el-form-item :label="t('chart.icon')" class="form-item">
          <el-select
            effect="dark"
            v-model="state.legendForm.icon"
            :placeholder="t('chart.icon')"
            @change="changeLegendStyle('icon')"
          >
            <el-option
              v-for="item in iconSymbolOptions"
              :key="item.value"
              :label="item.name"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('chart.orient')" class="form-item">
          <el-radio-group
            v-model="state.legendForm.orient"
            size="small"
            @change="changeLegendStyle('orient')"
          >
            <el-radio effect="dark" label="horizontal">{{ t('chart.horizontal') }}</el-radio>
            <el-radio effect="dark" label="vertical">{{ t('chart.vertical') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('chart.text_fontsize')" class="form-item">
          <el-select
            effect="dark"
            v-model="state.legendForm.textStyle.fontSize"
            :placeholder="t('chart.text_fontsize')"
            size="small"
            @change="changeLegendStyle('textStyle')"
          >
            <el-option
              v-for="option in state.fontSize"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('chart.text_color')" class="form-item">
          <el-color-picker
            v-model="state.legendForm.textStyle.color"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeLegendStyle('textStyle')"
          />
        </el-form-item>
        <el-form-item :label="t('chart.text_h_position')" class="form-item">
          <el-radio-group
            v-model="state.legendForm.hPosition"
            size="small"
            @change="changeLegendStyle('hPosition')"
          >
            <el-radio effect="dark" label="left">{{ t('chart.text_pos_left') }}</el-radio>
            <el-radio
              effect="dark"
              :disabled="state.legendForm.vPosition === 'center'"
              label="center"
              >{{ t('chart.text_pos_center') }}</el-radio
            >
            <el-radio effect="dark" label="right">{{ t('chart.text_pos_right') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('chart.text_v_position')" class="form-item">
          <el-radio-group
            v-model="state.legendForm.vPosition"
            size="small"
            @change="changeLegendStyle('vPosition')"
          >
            <el-radio effect="dark" label="top">{{ t('chart.text_pos_top') }}</el-radio>
            <el-radio
              effect="dark"
              :disabled="state.legendForm.hPosition === 'center'"
              label="center"
              >{{ t('chart.text_pos_center') }}</el-radio
            >
            <el-radio effect="dark" label="bottom">{{ t('chart.text_pos_bottom') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>

<style lang="less" scoped></style>
