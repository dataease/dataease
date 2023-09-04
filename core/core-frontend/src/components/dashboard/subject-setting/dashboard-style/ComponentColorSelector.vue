<template>
  <el-row>
    <el-form
      ref="colorFormRef"
      :model="colorForm"
      label-width="80px"
      size="small"
      style="width: 100%"
    >
      <div>
        <el-form-item :label="t('chart.color_case')" class="form-item">
          <el-popover placement="bottom" width="400" trigger="click" :persistent="false">
            <template #reference>
              <div :style="{ cursor: 'pointer', marginTop: '2px', width: '180px' }">
                <span
                  v-for="(c, index) in colorForm.basicStyle?.colors"
                  :key="index"
                  :style="{
                    width: '20px',
                    height: '20px',
                    display: 'inline-block',
                    backgroundColor: c
                  }"
                />
              </div>
            </template>

            <div style="padding: 6px 10px">
              <div>
                <span class="color-label">{{ t('chart.system_case') }}</span>
                <el-select
                  v-model="colorForm.basicStyle.colorScheme"
                  :placeholder="t('chart.pls_slc_color_case')"
                  size="small"
                  @change="changeColorOption('value')"
                >
                  <el-option
                    v-for="option in colorCases"
                    :key="option.value"
                    :label="option.name"
                    :value="option.value"
                    style="display: flex; align-items: center"
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
                    <span style="margin-left: 4px">{{ option.name }}</span>
                  </el-option>
                </el-select>
                <el-button
                  size="small"
                  type="text"
                  style="margin-left: 2px"
                  @click="resetCustomColor"
                  >{{ t('chart.reset') }}
                </el-button>
              </div>
              <!--自定义配色方案-->
              <div>
                <div style="display: flex; align-items: center; margin-top: 10px">
                  <span class="color-label">{{ t('chart.custom_case') }}</span>
                  <span>
                    <el-radio-group v-model="state.colorIndex" class="color-type">
                      <el-radio
                        v-for="(c, index) in colorForm.basicStyle.colors"
                        :key="index"
                        :label="index"
                        style="height: 26px; padding: 2px; border-radius: 3px"
                        @change="switchColor(index)"
                      >
                        <span
                          :style="{
                            width: '20px',
                            height: '20px',
                            display: 'inline-block',
                            'border-radius': '3px',
                            backgroundColor: c
                          }"
                        />
                      </el-radio>
                    </el-radio-group>
                  </span>
                </div>
                <div style="display: flex; align-items: center; margin-top: 10px">
                  <span class="color-label" />
                  <span>
                    <el-color-picker
                      v-model="state.customColor"
                      size="small"
                      class="color-picker-style"
                      :predefine="predefineColors"
                      is-custom
                      @change="switchColorCase"
                    />
                  </span>
                </div>
              </div>
            </div>
          </el-popover>
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
        <el-divider></el-divider>
        <el-row class="color-type-text">
          {{ t('visualization.card_color_matching') }}
        </el-row>
        <el-form-item :label="t('chart.quota_color')" class="form-item">
          <el-color-picker
            v-model="colorForm.misc.valueFontColor"
            class="color-picker-style"
            size="small"
            :predefine="predefineColors"
            is-custom
            @change="changeColorCase('quotaColor')"
          />
        </el-form-item>
        <el-form-item :label="t('chart.dimension_color')" class="form-item">
          <el-color-picker
            v-model="colorForm.misc.nameFontColor"
            class="color-picker-style"
            size="small"
            :predefine="predefineColors"
            is-custom
            @change="changeColorCase('dimensionColor')"
          />
        </el-form-item>
      </div>
      <el-divider></el-divider>
      <el-row class="color-type-text">
        {{ t('visualization.table_color_matching') }}
      </el-row>
      <div>
        <el-form-item :label="t('chart.table_header_bg')" class="form-item">
          <el-color-picker
            v-model="colorForm.tableHeader.tableHeaderBgColor"
            class="color-picker-style"
            size="small"
            :predefine="predefineColors"
            is-custom
            @change="changeColorCase('tableHeaderBgColor')"
          />
        </el-form-item>
        <el-form-item :label="t('chart.table_item_bg')" class="form-item">
          <el-color-picker
            v-model="colorForm.tableCell.tableItemBgColor"
            class="color-picker-style"
            size="small"
            :predefine="predefineColors"
            is-custom
            @change="changeColorCase('tableItemBgColor')"
          />
        </el-form-item>
        <el-form-item :label="t('chart.table_header_font_color')" class="form-item">
          <el-color-picker
            v-model="colorForm.tableHeader.tableHeaderFontColor"
            class="color-picker-style"
            size="small"
            :predefine="predefineColors"
            is-custom
            @change="changeColorCase('tableHeaderFontColor')"
          />
        </el-form-item>
        <el-form-item :label="t('chart.table_item_font_color')" class="form-item">
          <el-color-picker
            v-model="colorForm.tableCell.tableFontColor"
            class="color-picker-style"
            size="small"
            :predefine="predefineColors"
            is-custom
            @change="changeColorCase('tableFontColor')"
          />
        </el-form-item>
        <el-form-item :label="t('chart.table_border_color')" class="form-item">
          <el-color-picker
            v-model="colorForm.basicStyle.tableBorderColor"
            class="color-picker-style"
            size="small"
            :predefine="predefineColors"
            is-custom
            @change="changeColorCase('tableBorderColor')"
          />
        </el-form-item>
        <el-form-item :label="t('chart.table_scroll_bar_color')" class="form-item">
          <el-color-picker
            v-model="colorForm.basicStyle.tableScrollBarColor"
            class="color-picker-style"
            size="small"
            :predefine="predefineColors"
            color-format="rgb"
            show-alpha
            is-custom
            @change="changeColorCase('tableScrollBarColor')"
          />
        </el-form-item>
      </div>
    </el-form>
  </el-row>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { COLOR_CASES, COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { useI18n } from '@/hooks/web/useI18n'
import eventBus from '@/utils/eventBus'
import { storeToRefs } from 'pinia'
const { t } = useI18n()
const emits = defineEmits(['onColorChange'])
const colorFormRef = ref(null)
const colorForm = computed(
  () => canvasStyleData.value.component.chartColor as DeepPartial<ChartAttr>
)
const colorCases = COLOR_CASES

const predefineColors = COLOR_PANEL

const state = reactive({
  customColor: null,
  colorIndex: 0
})
const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData } = storeToRefs(dvMainStore)
const initForm = () => {
  state.customColor = colorForm.value.basicStyle.colors[0]
  const tableHeader = colorForm.value.tableHeader
  const tableCell = colorForm.value.tableCell
  tableHeader.tableHeaderFontColor = tableHeader.tableHeaderFontColor ?? tableCell.tableFontColor
}
const changeColorOption = (modifyName = 'value') => {
  const items = colorCases.filter(ele => {
    return ele.value === colorForm.value.basicStyle.colorScheme
  })
  colorForm.value.basicStyle.colors = JSON.parse(JSON.stringify(items[0].colors))

  state.customColor = colorForm.value.basicStyle.colors[0]
  state.colorIndex = 0

  // reset custom color
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
</style>
