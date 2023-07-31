<script lang="ts" setup>
import { PropType, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_TABLE_HEADER } from '@/views/chart/components/editor/util/chart'
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
  () => props.chart.customAttr.tableHeader,
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
  tableHeaderForm: JSON.parse(JSON.stringify(DEFAULT_TABLE_HEADER)),
  fontSize: []
})

const emit = defineEmits(['onTableHeaderChange'])

const changeTableHeader = val => {
  emit('onTableHeaderChange', state.tableHeaderForm)
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customAttr) {
    if (chart.customAttr.label) {
      state.tableHeaderForm = chart.customAttr.tableHeader
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
      <el-form ref="tableHeaderForm" :model="state.tableHeaderForm" label-width="80px" size="small">
        <el-form-item
          :label="t('chart.backgroundColor')"
          class="form-item"
          v-show="showProperty('tableHeaderBgColor')"
        >
          <el-color-picker
            v-model="state.tableHeaderForm.tableHeaderBgColor"
            :predefine="predefineColors"
            @change="changeTableHeader('tableHeaderBgColor')"
          />
        </el-form-item>
        <el-form-item
          :label="t('chart.text_fontsize')"
          class="form-item"
          v-show="showProperty('tableTitleFontSize')"
        >
          <el-select
            :effect="props.themes"
            v-model="state.tableHeaderForm.tableTitleFontSize"
            @change="changeTableHeader('tableTitleFontSize')"
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
          :label="t('chart.text_color')"
          class="form-item"
          v-show="showProperty('tableHeaderFontColor')"
        >
          <el-color-picker
            v-model="state.tableHeaderForm.tableHeaderFontColor"
            :predefine="predefineColors"
            @change="changeTableHeader('tableHeaderFontColor')"
          />
        </el-form-item>
        <el-form-item
          :label="t('visualization.lineHeight')"
          class="form-item"
          v-show="showProperty('tableTitleHeight')"
        >
          <el-slider
            v-model="state.tableHeaderForm.tableTitleHeight"
            :min="20"
            :max="100"
            @change="changeTableHeader('tableTitleHeight')"
          />
        </el-form-item>
        <el-form-item
          :label="t('chart.align')"
          class="form-item"
          v-show="showProperty('tableHeaderAlign')"
        >
          <el-select
            v-model="state.tableHeaderForm.tableHeaderAlign"
            @change="changeTableHeader('tableHeaderAlign')"
          >
            <el-option value="left" :label="t('chart.table_align_left')" />
            <el-option value="center" :label="t('chart.table_align_center')" />
            <el-option value="right" :label="t('chart.table_align_right')" />
          </el-select>
        </el-form-item>
        <el-form-item
          :label="t('chart.table_show_index')"
          class="form-item"
          v-show="showProperty('showIndex')"
        >
          <el-checkbox
            v-model="state.tableHeaderForm.showIndex"
            @change="changeTableHeader('showIndex')"
          />
        </el-form-item>
        <el-form-item
          :label="t('chart.table_index_desc')"
          class="form-item"
          v-show="showProperty('showIndex') && state.tableHeaderForm.showIndex"
        >
          <el-input
            v-model="state.tableHeaderForm.indexLabel"
            @blur="changeTableHeader('indexLabel')"
          />
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>

<style lang="less" scoped>
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
