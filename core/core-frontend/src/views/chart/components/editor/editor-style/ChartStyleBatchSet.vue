<template>
  <div class="batch-opt-main view-panel-row">
    <chart-style
      v-if="mixProperties && batchOptChartInfo"
      class="chart-style-main"
      themes="light"
      :param="param"
      :view="batchOptChartInfo"
      :chart="batchOptChartInfo"
      :properties="mixProperties"
      :property-inner-all="mixPropertiesInner"
      :dimension-data="state.dimensionData"
      :quota-data="state.quotaData"
      @onMiscChange="onMiscChange"
      @onLabelChange="onLabelChange"
      @onTooltipChange="onTooltipChange"
      @onChangeXAxisForm="onChangeXAxisForm"
      @onChangeYAxisForm="onChangeYAxisForm"
      @onTextChange="onTextChange"
      @onLegendChange="onLegendChange"
      @onBackgroundChange="onBackgroundChange"
      @onBasicStyleChange="onBasicStyleChange"
      @onTableHeaderChange="onTableHeaderChange"
      @onTableCellChange="onTableCellChange"
      @onTableTotalChange="onTableTotalChange"
      @onChangeMiscStyleForm="onChangeMiscStyleForm"
    />
    <el-row v-else class="view-selected-message-class">
      <span class="select-view">请选择组件...</span>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import eventBus from '@/utils/eventBus'
import ChartStyle from '@/views/chart/components/editor/editor-style/ChartStyle.vue'
import { reactive } from 'vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()

const emits = defineEmits(['calcStyle'])
const { batchOptChartInfo, mixProperties, mixPropertiesInner } = storeToRefs(dvMainStore)
const param = { id: 'mixId', optType: 'edit' }

const state = reactive({
  dimensionData: [],
  quotaData: []
})

const calcStyle = () => {
  emits('calcStyle')
}
const onColorChange = val => {
  batchOptChange('customAttr', 'color', val)
}
const onMiscChange = val => {
  batchOptChange('customAttr', 'misc', val.data)
}

const onLabelChange = val => {
  batchOptChange('customAttr', 'label', val)
}
const onTooltipChange = val => {
  batchOptChange('customAttr', 'tooltip', val)
}

const onChangeXAxisForm = val => {
  batchOptChange('customStyle', 'xAxis', val)
}

const onChangeYAxisForm = val => {
  batchOptChange('customStyle', 'yAxis', val)
}
const onChangeYAxisExtForm = val => {
  batchOptChange('customStyle', 'yAxisExt', val)
}

const onChangeMiscStyleForm = val => {
  batchOptChange('customStyle', 'misc', val)
}
const onTextChange = val => {
  batchOptChange('customStyle', 'text', val)
}
const onLegendChange = val => {
  batchOptChange('customStyle', 'legend', val)
}
const onMarginChange = val => {
  batchOptChange('customStyle', 'margin', val)
}
const onSuspensionChange = val => {
  batchOptChange('customAttr', 'suspension', val)
}

const onBackgroundChange = val => {
  dvMainStore.setBatchChangeBackground(val)
  snapshotStore.recordSnapshot('onBackgroundChange')
}
const onBasicStyleChange = val => {
  //基础样式差异化处理
  batchOptChange('customAttr', 'basicStyle', val.data)
}
const onTableHeaderChange = val => {
  batchOptChange('customAttr', 'tableHeader', val)
}
const onTableCellChange = val => {
  batchOptChange('customAttr', 'tableCell', val)
}
const onTableTotalChange = val => {
  batchOptChange('customAttr', 'tableTotal', val)
}
const batchOptChange = (custom, property, value) => {
  dvMainStore.setChangeProperties({
    custom: custom,
    property: property,
    value: value
  })
}
</script>

<style scoped lang="less">
.select-view {
  font-size: 14px;
  margin-left: 10px;
  font-weight: bold;
  line-height: 20px;
}
.view-selected-message-class {
  width: 100%;
  font-size: 12px;
  color: #9ea6b2;
  display: flex;
  align-items: center;
  justify-content: center;
  height: calc(100vh - 110px);
}

.batch-opt-main {
  height: 100%;
  overflow-y: hidden;
  width: 100%;
}

.chart-style-main {
  height: 100% !important;
  border-top: 0;
}

.view-title-name {
  display: -moz-inline-box;
  display: inline-block;
  width: 130px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  margin-left: 10px;
  font-size: 14px;
  font-weight: bold;
  color: #9ea6b2;
}

:deep(.ed-form-item) {
  .ed-input__inner {
    font-size: 12px;
    font-weight: 400;
  }
  .ed-input {
    --ed-input-height: 28px;
  }
  .ed-input-number {
    width: 100%;

    .ed-input-number__decrease {
      --ed-input-number-controls-height: 13px;
    }
    .ed-input-number__increase {
      --ed-input-number-controls-height: 13px;
    }

    .ed-input__inner {
      text-align: start;
    }
  }
  .ed-select {
    width: 100%;
    .ed-input__inner {
      height: 26px !important;
    }
  }
  .ed-checkbox {
    .ed-checkbox__label {
      font-size: 12px;
    }
  }
  .ed-color-picker {
    .ed-color-picker__mask {
      height: 26px;
      width: calc(100% - 2px) !important;
    }
  }
  .ed-radio {
    height: 20px;
    .ed-radio__label {
      font-size: 12px;
      font-style: normal;
      font-weight: 400;
      line-height: 20px;
    }
  }
}
:deep(.ed-form-item__label) {
  color: @canvas-main-font-color;
  font-size: 12px;
  font-style: normal;
  font-weight: 400;
}
:deep(.form-item-dark) {
  .ed-form-item__label {
    color: @canvas-main-font-color-dark;
  }
}

.view-panel-row {
  overflow-y: auto;
  overflow-x: hidden;
  height: 100%;

  :deep(.ed-collapse-item__header) {
    height: 36px !important;
    line-height: 36px !important;
    font-size: 12px !important;
    padding: 0 !important;
    font-weight: 500 !important;

    &.is-active {
      color: #1f2329;
    }

    .ed-collapse-item__arrow {
      margin: 0 6px 0 8px;

      &.is-active {
        color: #646a73;
      }
    }
  }

  :deep(.ed-collapse-item__content) {
    padding: 16px 8px 0;
    border: none;
  }

  :deep(.style-dark) {
    .ed-collapse-item__header {
      &.is-active {
        color: #fff;
      }

      .ed-collapse-item__arrow {
        &.is-active {
          color: #a6a6a6;
        }
      }
    }
  }
  :deep(.ed-collapse-item.ed-collapse--dark .ed-collapse-item__header) {
    &.is-active {
      color: #fff;
    }
    .ed-collapse-item__arrow {
      &.is-active {
        color: #a6a6a6;
      }
    }
  }
}
</style>
