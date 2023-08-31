<script lang="tsx" setup>
import { reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_TOOLTIP } from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object,
    required: true
  },
  themes: {
    type: String,
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
  tooltipForm: JSON.parse(JSON.stringify(DEFAULT_TOOLTIP)),
  fontSize: []
})

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
      state.tooltipForm = customAttr.tooltip
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
        ref="tooltipForm"
        :disabled="!state.tooltipForm.show"
        :model="state.tooltipForm"
        size="small"
        label-position="top"
      >
        <div class="custom-form-item-label">{{ t('chart.text') }}</div>
        <div style="display: flex">
          <el-form-item class="form-item" v-show="showProperty('color')" style="padding-right: 4px">
            <el-color-picker
              v-model="state.tooltipForm.color"
              class="color-picker-style"
              :predefine="predefineColors"
              @change="changeTooltipAttr('textStyle')"
              is-custom
            />
          </el-form-item>

          <el-form-item
            class="form-item"
            v-show="showProperty('fontSize')"
            style="padding-left: 4px"
          >
            <el-select
              style="width: 108px"
              :effect="props.themes"
              v-model.number="state.tooltipForm.fontSize"
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
        </div>

        <el-form-item :label="t('chart.background')" class="form-item">
          <el-color-picker
            v-model="state.tooltipForm.backgroundColor"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeTooltipAttr('backgroundColor')"
            is-custom
          />
        </el-form-item>
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
