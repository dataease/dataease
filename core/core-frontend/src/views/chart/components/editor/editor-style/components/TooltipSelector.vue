<script lang="tsx" setup>
import { reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_TOOLTIP } from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object,
    required: true
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
  tooltipForm: JSON.parse(JSON.stringify(DEFAULT_TOOLTIP)),
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

const changeTooltipAttr = () => {
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
      state.tooltipForm = customAttr.tooltip
    }
  }
}

initFontSize()
init()
</script>

<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="tooltipForm" :model="state.tooltipForm" label-width="80px" size="small">
        <el-form-item :label="t('chart.show')" class="form-item">
          <el-checkbox
            effect="dark"
            v-model="state.tooltipForm.show"
            @change="changeTooltipAttr('show')"
            >{{ t('chart.show') }}</el-checkbox
          >
        </el-form-item>
        <div v-show="state.tooltipForm.show">
          <el-form-item :label="t('chart.text_fontsize')" class="form-item">
            <el-select
              effect="dark"
              v-model="state.tooltipForm.textStyle.fontSize"
              :placeholder="t('chart.text_fontsize')"
              size="small"
              @change="changeTooltipAttr('textStyle')"
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
              v-model="state.tooltipForm.textStyle.color"
              class="color-picker-style"
              :predefine="predefineColors"
              @change="changeTooltipAttr('textStyle')"
            />
          </el-form-item>
          <el-form-item :label="t('chart.background')" class="form-item">
            <el-color-picker
              v-model="state.tooltipForm.backgroundColor"
              class="color-picker-style"
              :predefine="predefineColors"
              @change="changeTooltipAttr('backgroundColor')"
            />
          </el-form-item>
        </div>
      </el-form>
    </el-col>
  </div>
</template>

<style lang="less" scoped></style>
