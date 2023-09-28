<template>
  <div style="width: 100%" ref="containerRef">
    <el-form
      ref="colorFormRef"
      :model="colorForm"
      size="small"
      style="width: 100%; padding-bottom: 8px"
    >
      <div style="width: 100%; padding: 16px 8px 0">
        <custom-color-style-select
          v-if="colorAreaInit"
          class="custom-color-pick"
          v-model="state"
          :themes="themes"
          @change-basic-style="changeColorOption('value')"
        />

        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-checkbox
            size="small"
            :effect="themes"
            v-model="colorForm.basicStyle.gradient"
            @change="changeColorCase('gradient')"
          >
            {{ $t('chart.gradient') }}{{ $t('chart.color') }}
          </el-checkbox>
        </el-form-item>

        <div class="alpha-setting">
          <label class="alpha-label" :class="{ dark: 'dark' === themes }">
            {{ t('chart.not_alpha') }}
          </label>
          <el-row style="flex: 1" :gutter="8">
            <el-col :span="13">
              <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
                <el-slider
                  :effect="themes"
                  v-model="colorForm.basicStyle.alpha"
                  @change="changeColorCase('alpha')"
                />
              </el-form-item>
            </el-col>
            <el-col :span="11">
              <el-form-item
                style="padding-top: 4px"
                class="form-item"
                :class="'form-item-' + themes"
              >
                <el-input
                  type="number"
                  :effect="themes"
                  v-model="colorForm.basicStyle.alpha"
                  :min="0"
                  :max="100"
                  class="alpha-input-number"
                  :controls="false"
                  @change="changeColorCase('alpha')"
                >
                  <template #suffix> % </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        <el-divider class="m-divider" :class="'m-divider-' + themes"></el-divider>
      </div>

      <el-collapse-item
        :title="t('visualization.table_color_matching')"
        name="table_color_matching"
        class="inner-collapse"
      >
        <div style="padding: 0 8px 8px">
          <el-row :gutter="8">
            <el-col :span="12">
              <el-form-item :label="t('chart.table_header_bg')" class="form-item">
                <el-color-picker
                  :trigger-width="colorPickerWidth"
                  v-model="colorForm.tableHeader.tableHeaderBgColor"
                  size="small"
                  :predefine="predefineColors"
                  is-custom
                  @change="changeColorCase('tableHeaderBgColor')"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="t('chart.table_item_bg')" class="form-item">
                <el-color-picker
                  :trigger-width="colorPickerWidth"
                  v-model="colorForm.tableCell.tableItemBgColor"
                  size="small"
                  :predefine="predefineColors"
                  is-custom
                  @change="changeColorCase('tableItemBgColor')"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="8">
            <el-col :span="12">
              <el-form-item :label="t('chart.table_header_font_color')" class="form-item">
                <el-color-picker
                  :trigger-width="colorPickerWidth"
                  v-model="colorForm.tableHeader.tableHeaderFontColor"
                  size="small"
                  :predefine="predefineColors"
                  is-custom
                  @change="changeColorCase('tableHeaderFontColor')"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="t('chart.table_item_font_color')" class="form-item">
                <el-color-picker
                  :trigger-width="colorPickerWidth"
                  v-model="colorForm.tableCell.tableFontColor"
                  size="small"
                  :predefine="predefineColors"
                  is-custom
                  @change="changeColorCase('tableFontColor')"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="8">
            <el-col :span="12">
              <el-form-item :label="t('chart.table_border_color')" class="form-item">
                <el-color-picker
                  :trigger-width="colorPickerWidth"
                  v-model="colorForm.basicStyle.tableBorderColor"
                  size="small"
                  :predefine="predefineColors"
                  is-custom
                  @change="changeColorCase('tableBorderColor')"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="t('chart.table_scroll_bar_color')" class="form-item">
                <el-color-picker
                  :trigger-width="colorPickerWidth"
                  v-model="colorForm.basicStyle.tableScrollBarColor"
                  size="small"
                  :predefine="predefineColors"
                  color-format="rgb"
                  show-alpha
                  is-custom
                  @change="changeColorCase('tableScrollBarColor')"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-collapse-item>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, onMounted, reactive, ref, toRefs } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import {
  COLOR_CASES,
  COLOR_PANEL,
  DEFAULT_BASIC_STYLE
} from '@/views/chart/components/editor/util/chart'
import { useI18n } from '@/hooks/web/useI18n'
import eventBus from '@/utils/eventBus'
import { storeToRefs } from 'pinia'
import CustomColorStyleSelect from '@/views/chart/components/editor/editor-style/components/CustomColorStyleSelect.vue'
import elementResizeDetectorMaker from 'element-resize-detector'
const { t } = useI18n()
const props = defineProps({
  themes: {
    type: String,
    default: 'light'
  }
})

let colorAreaInit = ref(false)

const { themes } = toRefs(props)
const emits = defineEmits(['onColorChange'])
const colorFormRef = ref(null)

const colorForm = computed(
  () => canvasStyleData.value.component.chartColor as DeepPartial<ChartAttr>
)
const colorCases = COLOR_CASES

const predefineColors = COLOR_PANEL

const state = reactive({
  basicStyleForm: JSON.parse(JSON.stringify(DEFAULT_BASIC_STYLE)) as ChartBasicStyle,
  customColor: null,
  colorIndex: 0
})
const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData } = storeToRefs(dvMainStore)
const initForm = () => {
  state.customColor = colorForm.value.basicStyle.colors[0]
  setTimeout(() => {
    state.basicStyleForm = canvasStyleData.value.component.chartColor.basicStyle
    colorAreaInit.value = true
  }, 1000)
  const tableHeader = colorForm.value.tableHeader
  const tableCell = colorForm.value.tableCell
  tableHeader.tableHeaderFontColor = tableHeader.tableHeaderFontColor ?? tableCell.tableFontColor
}
const changeColorOption = (modifyName = 'value') => {
  colorForm.value.basicStyle = state.basicStyleForm
  changeColorCase(modifyName)
}

const changeColorCase = modifyName => {
  colorForm.value['modifyName'] = modifyName
  emits('onColorChange', colorForm.value)
}

const switchColor = index => {
  state.colorIndex = index
  state.customColor = colorForm.value.basicStyle.colors[state.colorIndex]
}
const switchColorCase = () => {
  colorForm.value.basicStyle.colors[state.colorIndex] = state.customColor
  colorForm.value['modifyName'] = 'value'
  emits('onColorChange', colorForm.value)
}

const resetCustomColor = () => {
  changeColorOption()
}

const switchCustomColor = index => {
  colorForm.value['seriesColors'][index].isCustom = true
  switchColorCase()
}

const containerRef = ref()
const containerWidth = ref()

const colorPickerWidth = computed(() => {
  if (containerWidth.value <= 240) {
    return 108
  } else {
    return 197
  }
})

onMounted(() => {
  initForm()
  eventBus.on('onThemeColorChange', initForm)

  const erd = elementResizeDetectorMaker()
  containerWidth.value = containerRef.value?.offsetWidth
  erd.listenTo(containerRef.value, element => {
    nextTick(() => {
      containerWidth.value = containerRef.value?.offsetWidth
    })
  })
})
</script>

<style scoped lang="less">
.form-item-slider :deep(.ed-form-item__label) {
  font-size: 12px;
  line-height: 38px;
  justify-content: flex-start;
}

.form-item :deep(.ed-form-item__label) {
  font-size: 12px;
  justify-content: flex-start;
}

.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}
.fill-width {
  width: 100%;
}

.color-label {
  display: inline-block;
  width: 60px;
}

.color-type :deep(.ed-radio__input) {
  display: none;
}

.ed-radio {
  margin: 0 2px 0 0 !important;
  border: 1px solid transparent;
}

.ed-radio :deep(.ed-radio__label) {
  padding-left: 0;
  padding-top: 3px;
}

.ed-radio.is-checked {
  border: 1px solid #0a7be0;
}

.custom-color-style {
  height: 300px;
  overflow-y: auto;
  padding: 4px 12px;
  border: 1px solid #e6e6e6;
}

.shape-item {
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-select-dropdown__item {
  padding: 0 20px;
}
span {
  font-size: 12px;
}

.color-type ::v-deep .el-radio__input {
  display: none;
}
.ed-radio {
  margin: 0 2px 0 0 !important;
  border: 1px solid transparent;
}
.el-radio ::v-deep .el-radio__label {
  padding-left: 0;
}

.ed-radio.is-checked {
  border: 1px solid #0a7be0;
}

.span-label {
  width: 300px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: inline-block;
  padding: 0 8px;
}

.custom-color-style {
  height: 300px;
  overflow-y: auto;
  padding: 4px 12px;
  border: 1px solid #e6e6e6;
}

.ed-divider__text {
  font-size: 8px;
  font-weight: 400;
  color: rgb(144, 147, 153);
}

.ed-form-item {
  flex-direction: column;
  margin-bottom: 8px;
}

:deep(.ed-form-item__label) {
  color: #646a73;
}

.color-type-text {
  font-weight: 500;
  font-size: 12px;
  margin-bottom: 8px;
}
.ed-divider--horizontal {
  margin: 16px 0;
}
.m-divider {
  border-color: rgba(31, 35, 41, 0.15);
  margin: 0 0 8px;
}

.m-divider-dark {
  border-color: rgba(233, 236, 241, 0.15) !important;
}
.inner-collapse {
  :deep(.ed-collapse-item__header) {
    background-color: transparent !important;
  }
  :deep(.ed-collapse-item__header) {
    border: none !important;
  }
  :deep(.ed-collapse-item__wrap) {
    border: none;
  }
}

.custom-color-pick {
  :deep(.ed-form-item__label) {
    justify-content: flex-start;
  }
  :deep(.ed-input__wrapper) {
    padding: 0 16px;
  }
  :deep(.custom-color-setting-btn) {
    margin-top: 23px;
  }
}

.alpha-setting {
  display: flex;
  width: 100%;

  .alpha-slider {
    padding: 4px 8px 0 8px;
    :deep(.ed-slider__button-wrapper) {
      --ed-slider-button-wrapper-size: 36px;
      --ed-slider-button-size: 16px;
    }
  }

  .alpha-label {
    padding-right: 8px;
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
    height: 32px;
    line-height: 32px;
    display: inline-flex;
    align-items: flex-start;

    min-width: 56px;

    &.dark {
      color: #a6a6a6;
    }
  }
  .alpha-input-number {
    :deep(input) {
      -webkit-appearance: none;
      -moz-appearance: textfield;

      &::-webkit-inner-spin-button,
      &::-webkit-outer-spin-button {
        -webkit-appearance: none;
      }
    }
  }
}
</style>
