<script setup lang="ts">
import { ElColorPicker, ElPopover } from 'element-plus-secondary'
import { computed, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_CASES, COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import GradientColorSelector from '@/views/chart/components/editor/editor-style/components/GradientColorSelector.vue'
import { getMapColorCases, stepsColor } from '@/views/chart/components/js/util'

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
  }>(),
  {
    themes: 'light'
  }
)

const emits = defineEmits(['update:modelValue', 'changeBasicStyle'])

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
  state.value.basicStyleForm.colorScheme = option.value
  colorCaseSelectorRef.value?.hide()
  changeColorOption(option)
}

const changeColorOption = (option?) => {
  let isGradient = option?.value?.endsWith('_split_gradient') || isColorGradient.value
  const getColorItems = isGradient ? getMapColorCases(colorCases) : colorCases
  const items = getColorItems.filter(ele => ele.value === state.value.basicStyleForm.colorScheme)

  if (items.length > 0) {
    state.value.basicStyleForm.colors = [...items[0].colors]
    state.value.customColor = state.value.basicStyleForm.colors[0]
    state.value.colorIndex = 0
    changeBasicStyle()
  }
}
const resetCustomColor = () => {
  changeColorOption()
}

const switchColorCase = () => {
  const { colorIndex, customColor, basicStyleForm } = state.value
  const colors = basicStyleForm.colors

  if (isColorGradient.value) {
    let startColor = colorIndex === 0 ? customColor : colors[0]
    let endColor = colorIndex === 0 ? colors[8] : customColor
    basicStyleForm.colors = stepsColor(startColor, endColor, 9, 1)
  } else {
    colors[colorIndex] = customColor
  }
  changeBasicStyle()
}
const isColorGradient = computed(() =>
  state.value.basicStyleForm.colorScheme.endsWith('_split_gradient')
)
const showColorGradientIndex = index => {
  return index === 0 || index === state.value.basicStyleForm.colors.length - 1
}
const switchColor = (index, c) => {
  if (isColorGradient.value && !showColorGradientIndex(index)) {
    return
  }
  state.value.colorIndex = index
  state.value.customColor = c
  customColorPickerRef.value?.show()
}

function changeBasicStyle() {
  emits('changeBasicStyle')
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
        :label="$t('chart.color_case')"
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
                    v-for="(c, index) in state.basicStyleForm.colors"
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
                :class="{ active: state.basicStyleForm.colorScheme === option.value }"
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

      <div class="custom-color-extend-setting colors">
        <div
          v-for="(c, index) in state.basicStyleForm.colors"
          :key="index"
          @click="switchColor(index, c)"
          class="color-item"
          :class="{
            active: state.colorIndex === index,
            hover: isColorGradient ? showColorGradientIndex(index) : true
          }"
          :style="{
            'border-color': colorItemBorderColor(index, state)
          }"
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
    </template>
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
