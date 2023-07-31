<template>
  <div class="batch-opt-main">
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
    />
    <el-row v-else class="view-selected-message-class">
      <span class="select-view">{{ $t('visualization.select_view') }}</span>
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

const onChangeSplitForm = val => {
  batchOptChange('customStyle', 'split', val)
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
  batchOptChange('customAttr', 'basicStyle', val)
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
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
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
</style>
