<script setup lang="ts">
import { ElColorPicker, ElPopover } from 'element-plus-secondary'
import { computed, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_CASES, COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    themes?: EditorTheme
    modelValue: {
      basicStyleForm: ChartBasicStyle
      customColor: any
      colorIndex: number
    }
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
  changeColorOption()
}
const changeColorOption = () => {
  const items = colorCases.filter(ele => {
    return ele.value === state.value.basicStyleForm.colorScheme
  })
  state.value.basicStyleForm.colors = [...items[0].colors]

  state.value.customColor = state.value.basicStyleForm.colors[0]
  state.value.colorIndex = 0

  changeBasicStyle()
}

const resetCustomColor = () => {
  changeColorOption()
}

const switchColorCase = () => {
  state.value.basicStyleForm.colors[state.value.colorIndex] = state.value.customColor
  changeBasicStyle()
}

const switchColor = (index, c) => {
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
</script>

<template>
  <div
    style="width: 100%"
    :style="{ 'margin-bottom': customColorExtendSettingOpened ? '16px' : 0 }"
  >
    <el-row>
      <el-form-item
        :label="t('chart.color_case')"
        class="form-item"
        :class="'form-item-' + themes"
        style="flex: 1; padding-right: 8px; margin-bottom: 8px"
      >
        <el-popover
          placement="bottom"
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
        <span style="color: #3370ff; cursor: pointer" @click="resetCustomColor">
          {{ t('chart.reset') }}
        </span>
      </div>

      <div class="custom-color-extend-setting colors">
        <div
          v-for="(c, index) in state.basicStyleForm.colors"
          :key="index"
          @click="switchColor(index, c)"
          class="color-item"
          :class="{ active: state.colorIndex === index }"
        >
          <div
            class="color-item__inner"
            :style="{
              backgroundColor: c
            }"
          ></div>
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
    width: 100%;
    .ed-input__prefix-inner {
      width: 100%;
    }
  }
  :deep(.ed-input__wrapper) {
    cursor: pointer;
  }
  .custom-color-selector-container {
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
  margin-top: 32px;
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
    border-color: #3370ff;
    color: #3370ff;
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

      &:hover {
        border-color: rgba(51, 112, 255, 0.6);
      }
      &.active {
        border-color: #3370ff;
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
