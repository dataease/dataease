<template>
  <el-row>
    <el-form
      ref="colorFormRef"
      :model="colorForm"
      label-width="80px"
      size="small"
      style="width: 100%; padding-bottom: 8px"
    >
      <div style="padding: 16px 8px 0">
        <custom-color-style-select
          class="custom-color-pick"
          v-model="state"
          :themes="'light'"
          @change-basic-style="changeColorOption('value')"
        ></custom-color-style-select>

        <el-form-item class="form-item" style="margin-left: -80px">
          <el-checkbox
            size="middle"
            v-model="colorForm.basicStyle.gradient"
            @change="changeColorCase('gradient')"
          >
            {{ $t('chart.gradient') }}{{ $t('chart.color') }}
          </el-checkbox>
        </el-form-item>

        <el-form-item :label="t('chart.not_alpha')" class="form-item form-item-slider">
          <el-slider
            v-model="colorForm.basicStyle.alpha"
            show-input
            :show-input-controls="false"
            input-size="small"
            @change="changeColorCase('alpha')"
          />
        </el-form-item>

        <el-divider class="m-divider"></el-divider>
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
  </el-row>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
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
const { t } = useI18n()
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
  state.basicStyleForm = canvasStyleData.value.component.chartColor.basicStyle
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

onMounted(() => {
  initForm()
  eventBus.on('onThemeColorChange', initForm)
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
.form-item-slider ::v-deep .el-form-item__label {
  font-size: 12px;
  line-height: 38px;
}
.form-item ::v-deep .el-form-item__label {
  font-size: 12px;
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
  margin: 16px 0 8px;
}
.inner-collapse {
  :deep(.ed-collapse-item__header) {
    background-color: transparent !important;
  }
  :deep(.ed-collapse-item__header) {
    border: none;
  }
  :deep(.ed-collapse-item__wrap) {
    border: none;
  }
}

.custom-color-pick {
  width: 224px !important;
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
</style>
