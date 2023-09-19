<script lang="tsx" setup>
import { PropType, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_MISC_STYLE } from '@/views/chart/components/editor/util/chart'

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

const state = reactive({
  miscForm: JSON.parse(JSON.stringify(DEFAULT_MISC_STYLE)),
  fontSize: []
})

const emit = defineEmits(['onChangeMiscStyleForm'])

watch(
  () => props.chart.customStyle.xAxis,
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

const changeMiscStyle = prop => {
  emit('onChangeMiscStyleForm', state.miscForm)
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customStyle) {
    let customStyle: ChartStyle = null
    if (Object.prototype.toString.call(chart.customStyle) === '[object Object]') {
      customStyle = JSON.parse(JSON.stringify(chart.customStyle))
    } else {
      customStyle = JSON.parse(chart.customStyle)
    }
    if (customStyle.xAxis) {
      state.miscForm = customStyle.misc
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
      <el-form ref="miscForm" :model="state.miscForm" label-width="80px" size="small">
        <el-form-item v-show="showProperty('showName')" :label="t('chart.name')" class="form-item">
          <el-checkbox
            size="small"
            v-model="state.miscForm.showName"
            @change="changeMiscStyle('showName')"
            >{{ t('chart.show') }}</el-checkbox
          >
        </el-form-item>
        <el-form-item v-show="showProperty('color')" :label="t('chart.color')" class="form-item">
          <el-color-picker
            v-model="state.miscForm.color"
            class="color-picker-style"
            :predefine="predefineColors"
            :effect="props.themes"
            @change="changeMiscStyle('color')"
          />
        </el-form-item>
        <el-form-item
          v-show="showProperty('fontSize')"
          :label="t('chart.text_fontsize')"
          class="form-item form-item-slider"
        >
          <el-select
            v-model="state.miscForm.fontSize"
            :placeholder="t('chart.text_fontsize')"
            :effect="props.themes"
            @change="changeMiscStyle('fontSize')"
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
          v-show="showProperty('axisColor')"
          :label="t('chart.axis_color')"
          class="form-item"
        >
          <el-color-picker
            v-model="state.miscForm.axisColor"
            class="color-picker-style"
            :predefine="predefineColors"
            :effect="props.themes"
            @change="changeMiscStyle('axisColor')"
          />
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>

<style lang="less" scoped></style>
