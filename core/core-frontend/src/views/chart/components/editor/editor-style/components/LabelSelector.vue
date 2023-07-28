<script lang="tsx" setup>
import { reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_LABEL } from '@/views/chart/components/editor/util/chart'

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

watch(
  () => props.chart.customAttr.label,
  () => {
    init()
  },
  { deep: true }
)

const predefineColors = COLOR_PANEL

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

const changeLabelAttr = () => {
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
  <div style="width: 100%">
    <el-col>
      <el-form
        ref="labelForm"
        :disabled="!state.labelForm.show"
        :model="state.labelForm"
        label-width="80px"
        size="small"
      >
        <el-form-item
          :label="t('chart.text_fontsize')"
          class="form-item"
          v-show="showProperty('fontSize')"
        >
          <el-select
            :effect="props.themes"
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
        <el-form-item
          :label="t('chart.text_color')"
          class="form-item"
          v-show="showProperty('color')"
        >
          <el-color-picker
            v-model="state.labelForm.color"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeLabelAttr('color')"
          />
        </el-form-item>
        <el-form-item :label="t('chart.label_position')" class="form-item">
          <el-select
            :effect="props.themes"
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
      </el-form>
    </el-col>
  </div>
</template>

<style lang="less" scoped></style>
