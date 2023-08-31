<script lang="ts" setup>
import { PropType, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_TABLE_CELL } from '@/views/chart/components/editor/util/chart'
import { ElColorPicker, ElFormItem, ElSelect, ElSlider } from 'element-plus-secondary'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object as PropType<ChartObj>,
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
  () => props.chart.customAttr.tableCell,
  () => {
    init()
  },
  { deep: true }
)

const predefineColors = COLOR_PANEL

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
  tableCellForm: JSON.parse(JSON.stringify(DEFAULT_TABLE_CELL)),
  fontSize: []
})

const emit = defineEmits(['onTableCellChange'])

const changeTableCell = val => {
  emit('onTableCellChange', state.tableCellForm)
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customAttr) {
    if (chart.customAttr.label) {
      state.tableCellForm = chart.customAttr.tableCell
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
      <el-form ref="tableCellForm" :model="state.tableCellForm" size="small" label-position="top">
        <el-form-item
          :label="t('chart.backgroundColor')"
          class="form-item"
          v-if="showProperty('tableItemBgColor')"
        >
          <el-color-picker
            v-model="state.tableCellForm.tableItemBgColor"
            :predefine="predefineColors"
            @change="changeTableCell('tableItemBgColor')"
            is-custom
          />
        </el-form-item>
        <div class="custom-form-item-label">{{ t('chart.text') }}</div>
        <div style="display: flex">
          <el-form-item
            class="form-item"
            v-if="showProperty('tableFontColor')"
            style="padding-right: 4px"
          >
            <el-color-picker
              v-model="state.tableCellForm.tableFontColor"
              :predefine="predefineColors"
              @change="changeTableCell('tableFontColor')"
              is-custom
            />
          </el-form-item>
          <el-form-item
            class="form-item"
            v-if="showProperty('tableItemFontSize')"
            style="padding-left: 4px"
          >
            <el-select
              style="width: 108px"
              :effect="props.themes"
              v-model="state.tableCellForm.tableItemFontSize"
              @change="changeTableCell('tableItemFontSize')"
            >
              <el-option
                v-for="option in state.fontSize"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item
          :label="t('chart.align')"
          class="form-item"
          v-if="showProperty('tableItemAlign')"
        >
          <el-select
            style="width: 100%"
            v-model="state.tableCellForm.tableItemAlign"
            :effect="props.themes"
            @change="changeTableCell('tableHeaderAlign')"
          >
            <el-option value="left" :label="t('chart.table_align_left')" />
            <el-option value="center" :label="t('chart.table_align_center')" />
            <el-option value="right" :label="t('chart.table_align_right')" />
          </el-select>
        </el-form-item>
        <el-form-item
          :label="t('visualization.lineHeight')"
          class="form-item"
          v-if="showProperty('tableItemHeight')"
        >
          <el-slider
            v-model="state.tableCellForm.tableItemHeight"
            :min="20"
            :max="100"
            @change="changeTableCell('tableItemHeight')"
          />
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>

<style lang="less" scoped>
:deep(.ed-color-picker.is-custom .ed-color-picker__trigger) {
  height: 24px;
}
.custom-form-item-label {
  margin-bottom: 4px;
  line-height: 20px;
  color: #a6a6a6;
  font-size: 12px;
  padding: 2px 12px 0 0;
}
.form-item-checkbox {
  margin-bottom: 10px !important;
}

.form-item-slider :deep(.ed-form-item__label) {
  font-size: 12px;
  line-height: 38px;
}

.form-item :deep(.ed-form-item__label) {
  font-size: 12px;
}

.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}

.color-label {
  display: inline-block;
  width: 60px;
}

.color-type {
  &:deep(.ed-radio__input) {
    display: none;
  }
  .ed-radio {
    margin: 0 2px 0 0 !important;
    border: 1px solid transparent;
  }
}

.ed-radio :deep(.ed-radio__label) {
  padding-left: 0;
}

.ed-radio.is-checked {
  border: 1px solid #0a7be0;
}
</style>
