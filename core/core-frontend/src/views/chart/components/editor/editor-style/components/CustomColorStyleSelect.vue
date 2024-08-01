<script setup lang="ts">
import { ElColorPicker, ElPopover } from 'element-plus-secondary'
import { computed, nextTick, onMounted, reactive, ref, toRefs, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_CASES, COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import GradientColorSelector from '@/views/chart/components/editor/editor-style/components/GradientColorSelector.vue'
import { getMapColorCases, stepsColor } from '@/views/chart/components/js/util'
import { useEmitt } from '@/hooks/web/useEmitt'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import chartViewManager from '../../../js/panel'
import { G2PlotChartView } from '../../../js/panel/types/impl/g2plot'
import { cloneDeep } from 'lodash-es'

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    themes?: EditorTheme
    modelValue: {
      basicStyleForm: ChartBasicStyle
      customColor: any
      colorIndex: number
    }
    propertyInner: Array<string>
    chart: ChartObj
    sub?: boolean
  }>(),
  {
    themes: 'light',
    sub: false
  }
)
const dvMainStore = dvMainStoreWithOut()
const { batchOptStatus } = storeToRefs(dvMainStore)
const emits = defineEmits(['update:modelValue', 'changeBasicStyle'])
const changeChartType = () => {
  if (isColorGradient.value) {
    state.value.basicStyleForm[colorSchemeName.value] = 'default'
    changeColorOption({ value: 'default' })
  }
}

const seriesColorPickerRef = ref<InstanceType<typeof ElColorPicker>>()
const seriesColorState = reactive({
  seriesColor: [],
  curSeriesColor: {
    id: '',
    name: '',
    color: ''
  } as any,
  curColorIndex: 0,
  seriesColorPickerId: 'body'
})

const instance = ref<G2PlotChartView | undefined>()

const colorsName = computed(() => {
  return props.sub ? 'subColors' : 'colors'
})
const colorSchemeName = computed(() => {
  return props.sub ? 'subColorScheme' : 'colorScheme'
})
const seriesColorName = computed(() => {
  return props.sub ? 'subSeriesColor' : 'seriesColor'
})

const needSetSeriesColor = computed(() => {
  return (
    instance.value?.propertyInner?.['basic-style-selector']?.includes('seriesColor') ||
    instance.value?.propertyInner?.['dual-basic-style-selector']?.includes('seriesColor')
  )
})

const needSetSubSeriesColor = computed(() => {
  return instance.value?.propertyInner?.['dual-basic-style-selector']?.includes('subSeriesColor')
})

const setupSeriesColor = () => {
  if (batchOptStatus.value || !props.chart) {
    return
  }

  instance.value = chartViewManager.getChartView(
    props.chart.render,
    props.chart.type
  ) as G2PlotChartView

  if (!props.sub) {
    if (!needSetSeriesColor.value) {
      return
    }
  } else {
    if (!needSetSubSeriesColor.value) {
      return
    }
  }

  let viewData = dvMainStore.getViewOriginData(props.chart.id)
  if (!viewData) {
    return
  }

  if (props.chart.type.includes('chart-mix')) {
    if (props.sub) {
      viewData = viewData.right?.data?.[0]
    } else {
      viewData = viewData.left?.data?.[0]
    }
  }
  if (!viewData) {
    return
  }

  const sFunction = props.sub
    ? instance.value?.setupSubSeriesColor
    : instance.value.setupSeriesColor
  if (!sFunction) {
    return
  }
  const newSeriesColor = sFunction(props.chart, viewData.data)
  const oldSeriesColor =
    props.chart.customAttr.basicStyle[seriesColorName.value]?.reduce((p, n) => {
      p[n.id] = n
      return p
    }, {}) || {}

  newSeriesColor?.forEach(item => {
    const oldColorItem = oldSeriesColor[item.id]
    if (oldColorItem) {
      item.color = oldColorItem.color
    }
  })
  seriesColorState.seriesColor.splice(0, seriesColorState.seriesColor.length, ...newSeriesColor)
  if (seriesColorState.seriesColor.length) {
    if (seriesColorState.curColorIndex > seriesColorState.seriesColor.length - 1) {
      seriesColorState.curColorIndex = 0
    }
    seriesColorState.curSeriesColor = seriesColorState.seriesColor[seriesColorState.curColorIndex]
    nextTick(() => {
      const targetId = 'series-color-picker-' + seriesColorState.curColorIndex
      const target = document.getElementById(targetId)
      if (target) {
        seriesColorState.seriesColorPickerId = `#${targetId}`
      }
    })
  }
}

const switchSeriesColor = (seriesColor, index) => {
  seriesColorPickerRef.value?.hide()
  seriesColorState.curSeriesColor = cloneDeep(seriesColor)
  seriesColorState.curColorIndex = index
  seriesColorState.seriesColorPickerId = '#series-color-picker-' + index
  nextTick(() => {
    seriesColorPickerRef.value?.show()
  })
}

const changeSeriesColor = () => {
  let changed = false
  seriesColorState.seriesColor.forEach(c => {
    if (
      c.id === seriesColorState.curSeriesColor.id &&
      c.color !== seriesColorState.curSeriesColor.color
    ) {
      changed = true
      c.color = seriesColorState.curSeriesColor.color
    }
  })
  if (changed) {
    state.value.basicStyleForm[seriesColorName.value] = seriesColorState.seriesColor
    changeBasicStyle('seriesColor')
  }
}
watch(
  [
    () => props.chart,
    () => props.chart?.type,
    () => props.chart?.customAttr.basicStyle.calcTopN,
    () => props.chart?.customAttr.basicStyle.topN,
    () => props.chart?.customAttr.basicStyle.topNLabel
  ],
  setupSeriesColor,
  { deep: false }
)
onMounted(() => {
  useEmitt({ name: 'chart-type-change', callback: changeChartType })
  useEmitt({ name: 'chart-data-change', callback: setupSeriesColor })
  setupSeriesColor()
})
const state = computed({
  get() {
    return props.modelValue
  },
  set(v) {
    emits('update:modelValue', v)
  }
})

const colorCases = COLOR_CASES
const predefineColors = COLOR_PANEL

const customColorExtendSettingOpened = ref<boolean>(false)
const colorCaseSelectorRef = ref<InstanceType<typeof ElPopover>>()

const customColorPickerRef = ref<InstanceType<typeof ElColorPicker>>()

function selectColorCase(option) {
  state.value.basicStyleForm[colorSchemeName.value] = option.value
  colorCaseSelectorRef.value?.hide()
  changeColorOption(option)
}

const changeColorOption = (option?) => {
  let isGradient = option?.value?.endsWith('_split_gradient') || isColorGradient.value
  const getColorItems = isGradient ? getMapColorCases(colorCases) : colorCases
  const items = getColorItems.filter(
    ele => ele.value === state.value.basicStyleForm[colorSchemeName.value]
  )
  if (items.length > 0) {
    state.value.basicStyleForm[colorsName.value] = [...items[0].colors]
    state.value.customColor = state.value.basicStyleForm[colorsName.value][0]
    state.value.colorIndex = 0
    state.value.basicStyleForm[seriesColorName.value]?.forEach((c, i) => {
      const length = items[0].colors.length
      c.color = items[0].colors[i % length]
    })
    changeBasicStyle()
  }
}
const resetCustomColor = () => {
  changeColorOption()
}

const switchColorCase = () => {
  const { colorIndex, customColor, basicStyleForm } = state.value
  const colors = basicStyleForm[colorsName.value]

  if (isColorGradient.value) {
    let startColor = colorIndex === 0 ? customColor : colors[0]
    let endColor = colorIndex === 0 ? colors[8] : customColor
    basicStyleForm[colorsName.value] = stepsColor(startColor, endColor, 9, 1)
  } else {
    colors[colorIndex] = customColor
  }
  changeBasicStyle()
}
const isColorGradient = computed(() =>
  state.value.basicStyleForm[colorSchemeName.value].endsWith('_split_gradient')
)
const showColorGradientIndex = index => {
  return index === 0 || index === state.value.basicStyleForm[colorsName.value].length - 1
}
const switchColor = (index, c) => {
  if (isColorGradient.value && !showColorGradientIndex(index)) {
    return
  }
  state.value.colorIndex = index
  state.value.customColor = c
  customColorPickerRef.value?.show()
}

function changeBasicStyle(prop = 'colors') {
  emits('changeBasicStyle', prop)
}

const _popoverShow = ref(false)
function onPopoverShow() {
  _popoverShow.value = true
}
function onPopoverHide() {
  _popoverShow.value = false
}
const showProperty = prop => props.propertyInner?.includes(prop)
const colorItemBorderColor = (index, state) => {
  const isCurrentColorActive = state.colorIndex === index
  if (isColorGradient.value) {
    if (showColorGradientIndex(index)) {
      // 渐变色的第一个和最后一个颜色
      return isCurrentColorActive ? 'var(--ed-color-primary)' : 'rgb(230,230,230)'
    } else {
      // 渐变色中非边缘的颜色
      return 'rgb(230,230,230,0.01)'
    }
  }
  // 非渐变色情况
  return isCurrentColorActive ? 'var(--ed-color-primary)' : ''
}
</script>

<template>
  <div
    style="width: 100%"
    :style="{ 'margin-bottom': customColorExtendSettingOpened ? '16px' : 0 }"
  >
    <el-row>
      <el-form-item
        v-if="showProperty('gradient-color')"
        :label="t('chart.color_case')"
        class="form-item"
        :class="'form-item-' + themes"
        style="flex: 1; padding-right: 8px; margin-bottom: 16px"
      >
        <gradient-color-selector
          v-model="state"
          :themes="themes"
          @select-color-case="selectColorCase"
        />
      </el-form-item>
      <el-form-item
        v-if="!showProperty('gradient-color')"
        :label="t('chart.color_case')"
        class="form-item"
        :class="'form-item-' + themes"
        style="flex: 1; padding-right: 8px; margin-bottom: 16px"
      >
        <el-popover
          placement="bottom-start"
          ref="colorCaseSelectorRef"
          width="268"
          :offset="4"
          trigger="click"
          :persistent="false"
          :show-arrow="false"
          @show="onPopoverShow"
          @hide="onPopoverHide"
          :popper-style="{ padding: 0 }"
          :effect="themes"
        >
          <template #reference>
            <el-input :effect="themes" readonly class="custom-color-selector">
              <template #prefix>
                <div class="custom-color-selector-container">
                  <div
                    v-for="(c, index) in state.basicStyleForm[colorsName]"
                    :key="index"
                    :style="{
                      flex: 1,
                      height: '100%',
                      backgroundColor: c
                    }"
                  ></div>
                </div>
              </template>
              <template #suffix>
                <el-icon class="input-arrow-icon" :class="{ reverse: _popoverShow }">
                  <ArrowDown />
                </el-icon>
              </template>
            </el-input>
          </template>
          <template #default>
            <el-scrollbar
              max-height="274px"
              class="cases-list"
              :class="{ dark: 'dark' === themes }"
            >
              <div
                v-for="option in colorCases"
                :key="option.value"
                class="select-color-item"
                :class="{ active: state.basicStyleForm[colorSchemeName] === option.value }"
                @click="selectColorCase(option)"
              >
                <div style="float: left">
                  <span
                    v-for="(c, index) in option.colors"
                    :key="index"
                    :style="{
                      width: '20px',
                      height: '20px',
                      float: 'left',
                      backgroundColor: c
                    }"
                  />
                </div>
                <span class="cases-list__text">{{ option.name }}</span>
              </div>
            </el-scrollbar>
          </template>
        </el-popover>
      </el-form-item>
      <div>
        <div
          class="custom-color-setting-btn"
          :class="{ active: customColorExtendSettingOpened, dark: 'dark' === themes }"
          @click="customColorExtendSettingOpened = !customColorExtendSettingOpened"
        >
          <el-icon style="font-size: 12px">
            <Icon name="icon_admin_outlined" />
          </el-icon>
        </div>
      </div>
    </el-row>
    <template v-if="customColorExtendSettingOpened">
      <div class="custom-color-extend-setting" :class="{ dark: 'dark' === themes }">
        {{ t('chart.custom_case') }}
        <span style="color: var(--ed-color-primary); cursor: pointer" @click="resetCustomColor">
          {{ t('chart.reset') }}
        </span>
      </div>

      <div
        v-if="!((!sub && showProperty('seriesColor')) || (sub && showProperty('subSeriesColor')))"
        class="custom-color-extend-setting colors"
      >
        <div
          v-for="(c, index) in state.basicStyleForm[colorsName]"
          :key="index"
          :class="{
            active: state.colorIndex === index,
            hover: isColorGradient ? showColorGradientIndex(index) : true
          }"
          class="color-item"
          :style="{
            'border-color': colorItemBorderColor(index, state)
          }"
          @click="switchColor(index, c)"
        >
          <div
            class="color-item__inner"
            :style="{
              backgroundColor: c
            }"
          >
            <el-icon
              v-if="isColorGradient && showColorGradientIndex(index)"
              class="input-arrow-icon"
              :style="{
                color: 'white',
                'font-size': 'x-small',
                left: '2px',
                bottom: '2px'
              }"
              :class="{ reverse: _popoverShow }"
            >
              <ArrowDown />
            </el-icon>
          </div>
        </div>
        <div class="inner-selector">
          <el-color-picker
            ref="customColorPickerRef"
            v-model="state.customColor"
            size="small"
            :predefine="predefineColors"
            @change="switchColorCase"
          />
        </div>
      </div>
      <div
        v-if="
          ((!sub && showProperty('seriesColor')) || (sub && showProperty('subSeriesColor'))) &&
          !batchOptStatus
        "
        class="series-color-setting colors"
      >
        <div
          v-for="(item, index) in seriesColorState.seriesColor"
          :key="item.id"
          class="color-list-item"
        >
          <div
            :class="{
              active: item.id === seriesColorState.curSeriesColor?.id
            }"
            class="color-item"
            @click="switchSeriesColor(item, index)"
          >
            <div
              class="color-item__inner"
              :style="{
                backgroundColor: item.color
              }"
            ></div>
          </div>
          <span
            :title="item.name"
            class="color-item-name"
            :class="themes === 'dark' ? 'dark' : ''"
            >{{ item.name }}</span
          >
          <div :id="'series-color-picker-' + index"></div>
        </div>
      </div>
    </template>
    <teleport :to="seriesColorState.seriesColorPickerId">
      <div style="position: absolute; width: 0; height: 0; overflow: hidden">
        <el-color-picker
          ref="seriesColorPickerRef"
          v-model="seriesColorState.curSeriesColor.color"
          size="small"
          :predefine="predefineColors"
          @change="changeSeriesColor"
        />
      </div>
    </teleport>
  </div>
</template>

<style scoped lang="less">
.form-item {
  flex-direction: column;
  :deep(.ed-input) {
    --ed-input-height: 28px;
  }
}
.custom-color-selector {
  :deep(.ed-input__prefix) {
    width: calc(100% - 22px);
    .ed-input__prefix-inner {
      width: 100%;
    }
  }
  :deep(.ed-input__wrapper) {
    cursor: pointer;
  }
  .custom-color-selector-container {
    border-radius: 2px;
    overflow: hidden;
    width: 100%;
    height: 16px;
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    align-items: center;
    justify-content: space-evenly;
  }
}

.custom-color-setting-btn {
  margin-top: 30px;
  line-height: 28px;

  border-radius: 4px;
  border: 1px solid #bbbfc4;
  padding: 0 7px;
  width: 28px;
  height: 28px;

  cursor: pointer;
  color: #1f2329;

  &.dark {
    border-color: #5f5f5f;
    color: #ebebeb;
  }

  &.active,
  &:hover {
    border-color: var(--ed-color-primary);
    color: var(--ed-color-primary);
  }
}
.custom-color-extend-setting {
  width: 100%;
  color: @canvas-main-font-color;
  font-size: 12px;
  font-style: normal;
  font-weight: 400;

  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;

  &.dark {
    color: @canvas-main-font-color-dark;
  }

  &.colors {
    margin-top: 8px;
    justify-content: flex-start;

    .color-item {
      width: 20px;
      height: 20px;
      border-radius: 3px;
      margin-right: 4px;
      cursor: pointer;
      padding: 2px;
      border: solid 1px transparent;

      .color-item__inner {
        width: 14px;
        height: 14px;
        border-radius: 1px;
      }
      &:not(.hover) {
        cursor: initial;
      }
      &:hover {
        border-color: var(--ed-color-primary-99, rgba(51, 112, 255, 0.6));
      }
      &.active {
        border-color: var(--ed-color-primary);
      }
    }

    .inner-selector {
      position: absolute;
      width: 0;
      height: 0;
      left: 50%;
      overflow: hidden;
    }
  }
}
.series-color-setting {
  max-height: 200px;
  overflow-y: auto;
  overflow-x: hidden;
  width: 100%;
  color: @canvas-main-font-color;
  font-size: 12px;
  font-style: normal;
  font-weight: 400;

  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-start;
  &.dark {
    color: @canvas-main-font-color-dark;
  }

  &.colors {
    margin-top: 8px;
    justify-content: flex-start;

    .color-list-item {
      display: flex;
      flex-direction: row;
      justify-content: start;
      align-items: flex-start;
      .color-item-name {
        max-width: 120px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        &.dark {
          color: white;
        }
      }
    }
    .color-item {
      width: 20px;
      height: 20px;
      border-radius: 3px;
      margin-right: 4px;
      margin-bottom: 4px;
      cursor: pointer;
      padding: 2px;
      border: solid 1px transparent;

      .color-item__inner {
        width: 14px;
        height: 14px;
        border-radius: 1px;
      }
      &:hover {
        border-color: var(--ed-color-primary-99, rgba(51, 112, 255, 0.6));
      }
      &.active {
        border-color: var(--ed-color-primary);
      }
    }
  }
}
.cases-list {
  margin: 6px 0;

  .select-color-item {
    width: 100%;

    font-size: var(--ed-font-size-base);
    padding: 0 32px 0 20px;
    position: relative;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    color: var(--ed-text-color-regular);
    height: 34px;
    line-height: 34px;
    box-sizing: border-box;
    cursor: pointer;

    display: flex;
    align-items: center;

    &:hover {
      background-color: var(--ed-fill-color-light);
    }

    &.active {
      color: var(--ed-color-primary);
      font-weight: 500;
    }
  }

  &.dark {
    .select-color-item {
      color: #ebebeb;
      &:hover {
        background-color: rgba(235, 235, 235, 0.1);
      }
    }
  }

  .cases-list__text {
    margin-left: 4px;
  }
}
</style>
