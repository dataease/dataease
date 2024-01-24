<template>
  <div class="batch-opt-main view-panel-row">
    <chart-style
      v-if="mixProperties && batchOptComponentInfo && batchOptComponentType === 'UserView'"
      class="chart-style-main"
      themes="light"
      :param="param"
      :common-background-pop="batchOptComponentInfo.commonBackground"
      :view="batchOptComponentInfo"
      :chart="batchOptComponentInfo"
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
      @onIndicatorChange="onIndicatorChange"
      @onIndicatorNameChange="onIndicatorNameChange"
    />
    <common-attr
      v-else-if="mixProperties && batchOptComponentInfo && batchOptComponentType !== 'UserView'"
      :element="batchOptComponentInfo"
      :show-style="mixProperties.includes('common-style')"
      @onAttrChange="onStyleAttrChange"
      themes="light"
    ></common-attr>
    <el-row v-else class="view-selected-message-class">
      <span class="select-view">请选择组件...</span>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import ChartStyle from '@/views/chart/components/editor/editor-style/ChartStyle.vue'
import { reactive } from 'vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()

const { batchOptComponentInfo, batchOptComponentType, mixProperties, mixPropertiesInner } =
  storeToRefs(dvMainStore)
const param = { id: 'mixId', optType: 'edit' }

const state = reactive({
  dimensionData: [],
  quotaData: []
})

const onMiscChange = (val, prop) => {
  batchOptChange('customAttr', 'misc', val.data, prop)
}

const onLabelChange = (val, prop) => {
  batchOptChange('customAttr', 'label', val, prop)
}
const onTooltipChange = (val, prop) => {
  batchOptChange('customAttr', 'tooltip', val.data, prop)
}

const onChangeXAxisForm = (val, prop) => {
  batchOptChange('customStyle', 'xAxis', val, prop)
}

const onChangeYAxisForm = (val, prop) => {
  batchOptChange('customStyle', 'yAxis', val, prop)
}

const onIndicatorChange = (val, prop) => {
  batchOptChange('customAttr', 'indicator', val, prop)
}

const onIndicatorNameChange = (val, prop) => {
  batchOptChange('customAttr', 'indicatorName', val, prop)
}

const onChangeMiscStyleForm = (val, prop) => {
  batchOptChange('customStyle', 'misc', val, prop)
}
const onTextChange = (val, prop) => {
  batchOptChange('customStyle', 'text', val, prop)
}
const onLegendChange = (val, prop) => {
  batchOptChange('customStyle', 'legend', val, prop)
}

const onBackgroundChange = val => {
  dvMainStore.setBatchChangeBackground(val)
  snapshotStore.recordSnapshotCache()
}
const onBasicStyleChange = (val, prop) => {
  //基础样式差异化处理
  batchOptChange('customAttr', 'basicStyle', val.data, prop)
}
const onTableHeaderChange = (val, prop) => {
  batchOptChange('customAttr', 'tableHeader', val, prop)
}
const onTableCellChange = (val, prop) => {
  batchOptChange('customAttr', 'tableCell', val, prop)
}
const onTableTotalChange = (val, prop) => {
  batchOptChange('customAttr', 'tableTotal', val.data, prop)
}
const batchOptChange = (custom, property, value, subProp?) => {
  dvMainStore.setChangeProperties({
    custom: custom,
    property: property,
    value: value,
    subProp: subProp
  })
}

const onStyleAttrChange = params => {
  batchOptChange(params.custom, params.property, params.value)
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
