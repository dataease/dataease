<script lang="tsx" setup>
import { computed, onMounted, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_MISC_STYLE } from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    chart: any
    themes?: EditorTheme
    propertyInner: Array<string>
  }>(),
  { themes: 'dark' }
)

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

onMounted(() => {
  init()
})
</script>

<template>
  <el-form ref="miscForm" :model="state.miscForm" label-width="80px" size="small">
    <el-form-item v-if="showProperty('showName')" class="form-item" :class="'form-item-' + themes">
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.miscForm.showName"
        @change="changeMiscStyle('showName')"
      >
        {{ t('chart.show') }} {{ t('chart.name') }}
      </el-checkbox>
    </el-form-item>
    <el-form-item
      v-if="showProperty('color')"
      :label="t('chart.color')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-color-picker
        v-model="state.miscForm.color"
        class="color-picker-style"
        :predefine="predefineColors"
        is-custom
        :effect="themes"
        @change="changeMiscStyle('color')"
      />
    </el-form-item>
    <el-form-item
      v-if="showProperty('axisColor')"
      :label="t('chart.axis_color')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-color-picker
        v-model="state.miscForm.axisColor"
        class="color-picker-style"
        :predefine="predefineColors"
        is-custom
        :effect="themes"
        @change="changeMiscStyle('axisColor')"
      />
    </el-form-item>
    <el-form-item
      v-if="showProperty('fontSize')"
      :label="t('chart.text_fontsize')"
      class="form-item form-item-slider"
    >
      <el-select
        style="width: 108px"
        v-model="state.miscForm.fontSize"
        :placeholder="t('chart.text_fontsize')"
        :effect="themes"
        @change="changeMiscStyle('fontSize')"
      >
        <el-option
          v-for="option in fontSizeList"
          :key="option.value"
          :label="option.name"
          :value="option.value"
        />
      </el-select>
    </el-form-item>
  </el-form>
</template>

<style lang="less" scoped></style>
