<script lang="tsx" setup>
import { computed, onMounted, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_MISC_STYLE } from '@/views/chart/components/editor/util/chart'
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    chart: any
    themes?: EditorTheme
    propertyInner: Array<string>
  }>(),
  { themes: 'dark' }
)

const toolTip = computed(() => {
  return props.themes === 'dark' ? 'light' : 'dark'
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

const fontSizeList = computed(() => {
  const arr = []
  for (let i = 10; i <= 40; i = i + 2) {
    arr.push({
      name: i + '',
      value: i
    })
  }
  for (let i = 50; i <= 200; i = i + 10) {
    arr.push({
      name: i + '',
      value: i
    })
  }
  return arr
})

const changeMiscStyle = prop => {
  emit('onChangeMiscStyleForm', state.miscForm, prop)
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
      if (!state.miscForm.axisValue) {
        state.miscForm.axisValue = JSON.parse(JSON.stringify(DEFAULT_MISC_STYLE)).axisValue
      }
    }
  }
}

const showProperty = prop => props.propertyInner?.includes(prop)

onMounted(() => {
  init()
})
</script>

<template>
  <el-form size="small" ref="miscForm" :model="state.miscForm">
    <el-form-item
      v-if="showProperty('showName')"
      class="form-item form-item-checkbox"
      :class="'form-item-' + themes"
    >
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.miscForm.showName"
        @change="changeMiscStyle('showName')"
      >
        {{ t('chart.show') }}{{ t('chart.name') }}
      </el-checkbox>
    </el-form-item>

    <el-space>
      <div style="width: 14px"></div>
      <el-form-item
        v-if="showProperty('color')"
        :label="t('chart.text')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-color-picker
          :disabled="!state.miscForm.showName"
          v-model="state.miscForm.color"
          class="color-picker-style"
          :predefine="predefineColors"
          is-custom
          :effect="themes"
          @change="changeMiscStyle('color')"
        />
      </el-form-item>
      <el-form-item v-if="showProperty('fontSize')" class="form-item form-item-slider">
        <template #label>&nbsp;</template>
        <el-select
          :disabled="!state.miscForm.showName"
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
    </el-space>
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
        :trigger-width="108"
        :effect="themes"
        @change="changeMiscStyle('axisColor')"
      />
    </el-form-item>
    <template v-if="showProperty('axisValue')">
      <div
        style="display: flex; flex-direction: row; justify-content: space-between; padding: 8px 0"
      >
        <label class="custom-form-item-label" :class="'custom-form-item-label--' + themes">
          {{ t('chart.axis_value') }}
          <el-tooltip class="item" :effect="toolTip" placement="top">
            <template #content><span v-html="t('chart.axis_tip')"></span></template>
            <span style="vertical-align: middle">
              <el-icon style="cursor: pointer">
                <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
              </el-icon>
            </span>
          </el-tooltip>
        </label>

        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-checkbox
            size="small"
            :effect="props.themes"
            v-model="state.miscForm.axisValue.auto"
            @change="changeMiscStyle('axisValue.auto')"
          >
            {{ t('chart.axis_auto') }}
          </el-checkbox>
        </el-form-item>
      </div>

      <template v-if="showProperty('axisValue') && !state.miscForm.axisValue.auto">
        <el-row :gutter="8">
          <el-col :span="12">
            <el-form-item
              class="form-item"
              :class="'form-item-' + themes"
              :label="t('chart.axis_value_min')"
            >
              <el-input-number
                :effect="props.themes"
                controls-position="right"
                v-model.number="state.miscForm.axisValue.min"
                @change="changeMiscStyle('axisValue.min')"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              class="form-item"
              :class="'form-item-' + themes"
              :label="t('chart.axis_value_max')"
            >
              <el-input-number
                controls-position="right"
                :effect="props.themes"
                v-model.number="state.miscForm.axisValue.max"
                @change="changeMiscStyle('axisValue.max')"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <label class="custom-form-item-label" :class="'custom-form-item-label--' + themes">
          {{ t('chart.axis_value_split_count') }}
          <el-tooltip class="item" :effect="toolTip" placement="top">
            <template #content>{{ t('chart.number_of_scales_tip') }}</template>
            <span style="vertical-align: middle">
              <el-icon style="cursor: pointer">
                <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
              </el-icon>
            </span>
          </el-tooltip>
        </label>

        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-input-number
            style="width: 100%"
            :effect="props.themes"
            :min="1"
            :step="1"
            :precision="0"
            controls-position="right"
            v-model.number="state.miscForm.axisValue.splitCount"
            @change="changeMiscStyle('axisValue.splitCount')"
          />
        </el-form-item>
      </template>
    </template>
  </el-form>
</template>

<style lang="less" scoped>
.form-item-checkbox {
  margin-bottom: 0 !important;
}
.custom-form-item-label {
  margin-bottom: 4px;
  line-height: 20px;
  color: #646a73;
  font-size: 12px;
  font-style: normal;
  font-weight: 400;
  padding: 2px 12px 0 0;

  &.custom-form-item-label--dark {
    color: #a6a6a6;
  }
}
</style>
