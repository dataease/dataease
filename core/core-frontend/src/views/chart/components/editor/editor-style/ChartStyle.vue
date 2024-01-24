<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { PropType, toRefs, nextTick, watch } from 'vue'
import MiscSelector from '@/views/chart/components/editor/editor-style/components/MiscSelector.vue'
import LabelSelector from '@/views/chart/components/editor/editor-style/components/LabelSelector.vue'
import TooltipSelector from '@/views/chart/components/editor/editor-style/components/TooltipSelector.vue'
import XAxisSelector from '@/views/chart/components/editor/editor-style/components/XAxisSelector.vue'
import YAxisSelector from '@/views/chart/components/editor/editor-style/components/YAxisSelector.vue'
import TitleSelector from '@/views/chart/components/editor/editor-style/components/TitleSelector.vue'
import LegendSelector from '@/views/chart/components/editor/editor-style/components/LegendSelector.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import CollapseSwitchItem from '@/components/collapse-switch-item/src/CollapseSwitchItem.vue'
import { ElCollapseItem } from 'element-plus-secondary'
import BasicStyleSelector from '@/views/chart/components/editor/editor-style/components/BasicStyleSelector.vue'
import ComponentPosition from '@/components/visualization/common/ComponentPosition.vue'
import BackgroundOverallCommon from '@/components/visualization/component-background/BackgroundOverallCommon.vue'
import TableHeaderSelector from '@/views/chart/components/editor/editor-style/components/table/TableHeaderSelector.vue'
import TableCellSelector from '@/views/chart/components/editor/editor-style/components/table/TableCellSelector.vue'
import TableTotalSelector from '@/views/chart/components/editor/editor-style/components/table/TableTotalSelector.vue'
import MiscStyleSelector from '@/views/chart/components/editor/editor-style/components/MiscStyleSelector.vue'
import IndicatorValueSelector from '@/views/chart/components/editor/editor-style/components/IndicatorValueSelector.vue'
import IndicatorNameSelector from '@/views/chart/components/editor/editor-style/components/IndicatorNameSelector.vue'

const dvMainStore = dvMainStoreWithOut()
const { dvInfo } = storeToRefs(dvMainStore)
const { t } = useI18n()

const state = {
  attrActiveNames: [],
  styleActiveNames: [],
  initReady: true
}

const props = defineProps({
  commonBackgroundPop: {
    type: Object,
    required: false
  },
  chart: {
    type: Object as PropType<ChartObj>,
    required: true
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  },
  dimensionData: {
    type: Array,
    required: true
  },
  quotaData: {
    type: Array,
    required: true
  },
  properties: {
    type: Array as PropType<EditorProperty[]>,
    required: false,
    default: () => {
      return []
    }
  },
  propertyInnerAll: {
    type: Object as PropType<EditorPropertyInner>,
    required: false,
    default: () => {
      return {}
    }
  }
})

const { chart, themes, properties, propertyInnerAll, commonBackgroundPop } = toRefs(props)
const emit = defineEmits([
  'onColorChange',
  'onMiscChange',
  'onLabelChange',
  'onTooltipChange',
  'onChangeXAxisForm',
  'onChangeYAxisForm',
  'onTextChange',
  'onLegendChange',
  'onBasicStyleChange',
  'onBackgroundChange',
  'onTableHeaderChange',
  'onTableCellChange',
  'onTableTotalChange',
  'onChangeMiscStyleForm',
  'onExtTooltipChange',
  'onIndicatorChange',
  'onIndicatorNameChange'
])

const showProperties = (property: EditorProperty) => properties.value?.includes(property)

const onMiscChange = (val, prop) => {
  emit('onMiscChange', val, prop)
}

const onLabelChange = (val, prop) => {
  state.initReady && emit('onLabelChange', val, prop)
}

const onTooltipChange = (val, prop) => {
  state.initReady && emit('onTooltipChange', val, prop)
}

const onChangeXAxisForm = (val, prop) => {
  state.initReady && emit('onChangeXAxisForm', val, prop)
}

const onChangeYAxisForm = (val, prop) => {
  state.initReady && emit('onChangeYAxisForm', val, prop)
}

const onTextChange = (val, prop) => {
  state.initReady && emit('onTextChange', val, prop)
}

const onIndicatorChange = (val, prop) => {
  state.initReady && emit('onIndicatorChange', val, prop)
}

const onIndicatorNameChange = (val, prop) => {
  state.initReady && emit('onIndicatorNameChange', val, prop)
}

const onLegendChange = (val, prop) => {
  state.initReady && emit('onLegendChange', val, prop)
}
const onBasicStyleChange = (val, prop) => {
  state.initReady && emit('onBasicStyleChange', val, prop)
}

const onBackgroundChange = (val, prop) => {
  state.initReady && emit('onBackgroundChange', val, prop)
}

const onTableHeaderChange = (val, prop) => {
  emit('onTableHeaderChange', val, prop)
}
const onTableCellChange = (val, prop) => {
  emit('onTableCellChange', val, prop)
}
const onTableTotalChange = (val, prop) => {
  emit('onTableTotalChange', val, prop)
}
const onChangeMiscStyleForm = (val, prop) => {
  emit('onChangeMiscStyleForm', val, prop)
}

const onExtTooltipChange = val => {
  emit('onExtTooltipChange', val)
}
watch(
  () => props.chart.id,
  () => {
    state.initReady = false
    nextTick(() => {
      state.initReady = true
    })
  },
  { deep: true }
)
</script>

<template>
  <el-row class="view-panel" :class="'style-' + themes">
    <div class="attr-style">
      <el-row class="de-collapse-style">
        <el-collapse v-model="state.attrActiveNames" class="style-collapse">
          <el-collapse-item
            :effect="themes"
            name="position"
            :title="'位置'"
            v-if="dvInfo.type !== 'dashboard'"
          >
            <component-position :themes="themes" />
          </el-collapse-item>
          <el-collapse-item
            :effect="themes"
            name="basicStyle"
            :title="t('chart.basic_style')"
            v-if="showProperties('basic-style-selector')"
          >
            <basic-style-selector
              :property-inner="propertyInnerAll['basic-style-selector']"
              :themes="themes"
              :chart="chart"
              @onBasicStyleChange="onBasicStyleChange"
            />
          </el-collapse-item>
          <collapse-switch-item
            :themes="themes"
            v-model="chart.customStyle.text.show"
            v-if="showProperties('title-selector')"
            :change-model="chart.customStyle.text"
            @modelChange="val => onTextChange(val, 'show')"
            name="title"
            :title="$t('chart.title')"
          >
            <title-selector
              :property-inner="propertyInnerAll['title-selector']"
              :themes="themes"
              class="attr-selector"
              :chart="chart"
              @onTextChange="onTextChange"
            />
          </collapse-switch-item>
          <collapse-switch-item
            :themes="themes"
            v-if="showProperties('legend-selector')"
            v-model="chart.customStyle.legend.show"
            :change-model="chart.customStyle.legend"
            @modelChange="val => onLegendChange(val, 'show')"
            name="legend"
            :title="$t('chart.legend')"
          >
            <legend-selector
              class="attr-selector"
              :property-inner="propertyInnerAll['legend-selector']"
              :themes="themes"
              :chart="chart"
              @onLegendChange="onLegendChange"
            />
          </collapse-switch-item>
          <el-collapse-item
            :effect="themes"
            name="background"
            title="背景"
            v-if="showProperties('background-overall-component') && commonBackgroundPop"
          >
            <background-overall-common
              :common-background-pop="commonBackgroundPop"
              :themes="themes"
              @onBackgroundChange="onBackgroundChange"
              component-position="component"
            />
          </el-collapse-item>
          <el-collapse-item
            :effect="themes"
            v-if="showProperties('indicator-value-selector')"
            name="indicator-value"
            title="指标值"
          >
            <indicator-value-selector
              :property-inner="propertyInnerAll['indicator-value-selector']"
              :themes="themes"
              class="attr-selector"
              :chart="chart"
              :quota-fields="props.quotaData"
              @onIndicatorChange="onIndicatorChange"
            />
          </el-collapse-item>
          <collapse-switch-item
            :themes="themes"
            v-model="chart.customAttr.indicatorName.show"
            v-if="showProperties('indicator-name-selector')"
            :change-model="chart.customAttr.indicatorName"
            @modelChange="val => onIndicatorNameChange(val, 'show')"
            title="指标名称"
            name="indicator-name"
          >
            <indicator-name-selector
              :property-inner="propertyInnerAll['indicator-name-selector']"
              :themes="themes"
              class="attr-selector"
              :chart="chart"
              :quota-fields="props.quotaData"
              @onIndicatorNameChange="onIndicatorNameChange"
            />
          </collapse-switch-item>
          <el-collapse-item
            :effect="themes"
            v-if="showProperties('misc-selector') && !chart.type.includes('mix')"
            name="size"
            title="大小"
          >
            <misc-selector
              :property-inner="propertyInnerAll['misc-selector']"
              :themes="themes"
              class="attr-selector"
              :chart="chart"
              :quota-fields="props.quotaData"
              @onMiscChange="onMiscChange"
            />
          </el-collapse-item>
          <el-collapse-item
            :effect="themes"
            v-if="showProperties('misc-style-selector')"
            name="size"
            title="大小"
          >
            <misc-style-selector
              :property-inner="propertyInnerAll['misc-style-selector']"
              :themes="themes"
              class="attr-selector"
              :chart="chart"
              :quota-fields="props.quotaData"
              @onChangeMiscStyleForm="onChangeMiscStyleForm"
            />
          </el-collapse-item>
          <collapse-switch-item
            :themes="themes"
            v-if="showProperties('label-selector')"
            v-model="chart.customAttr.label.show"
            :change-model="chart.customAttr.label"
            @modelChange="val => onLabelChange(val, 'show')"
            :title="$t('chart.label')"
            name="label"
          >
            <label-selector
              :property-inner="propertyInnerAll['label-selector']"
              :themes="themes"
              class="attr-selector"
              :chart="chart"
              @onLabelChange="onLabelChange"
            />
          </collapse-switch-item>
          <collapse-switch-item
            :themes="themes"
            v-if="showProperties('tooltip-selector')"
            v-model="chart.customAttr.tooltip.show"
            :change-model="chart.customAttr.tooltip"
            @modelChange="val => onTooltipChange({ data: val }, 'show')"
            name="tooltip"
            :title="$t('chart.tooltip')"
          >
            <tooltip-selector
              class="attr-selector"
              :property-inner="propertyInnerAll['tooltip-selector']"
              :themes="themes"
              :chart="chart"
              @onTooltipChange="onTooltipChange"
              @onExtTooltipChange="onExtTooltipChange"
            />
          </collapse-switch-item>
          <el-collapse-item
            :effect="themes"
            name="tableHeader"
            :title="t('chart.table_header')"
            v-if="showProperties('table-header-selector')"
          >
            <table-header-selector
              :property-inner="propertyInnerAll['table-header-selector']"
              :themes="themes"
              :chart="chart"
              @onTableHeaderChange="onTableHeaderChange"
            />
          </el-collapse-item>
          <el-collapse-item
            :effect="themes"
            name="tableCell"
            :title="t('chart.table_cell')"
            v-if="showProperties('table-cell-selector')"
          >
            <table-cell-selector
              :property-inner="propertyInnerAll['table-cell-selector']"
              :themes="themes"
              :chart="chart"
              @onTableCellChange="onTableCellChange"
            />
          </el-collapse-item>
          <el-collapse-item
            :effect="themes"
            name="tableTotal"
            :title="t('chart.table_total')"
            v-if="showProperties('table-total-selector')"
          >
            <table-total-selector
              :property-inner="propertyInnerAll['table-total-selector']"
              :themes="themes"
              :chart="chart"
              @onTableTotalChange="onTableTotalChange"
            />
          </el-collapse-item>
        </el-collapse>

        <el-collapse v-model="state.styleActiveNames" class="style-collapse">
          <collapse-switch-item
            :themes="themes"
            v-if="showProperties('x-axis-selector')"
            v-model="chart.customStyle.xAxis.show"
            :change-model="chart.customStyle.xAxis"
            @modelChange="val => onChangeXAxisForm(val, 'show')"
            name="xAxis"
            :title="t('chart.xAxis')"
          >
            <x-axis-selector
              class="attr-selector"
              :property-inner="propertyInnerAll['x-axis-selector']"
              :themes="themes"
              :chart="chart"
              @onChangeXAxisForm="onChangeXAxisForm"
            />
          </collapse-switch-item>
          <collapse-switch-item
            :themes="themes"
            v-if="showProperties('y-axis-selector')"
            v-model="chart.customStyle.yAxis.show"
            :change-model="chart.customStyle.yAxis"
            @modelChange="val => onChangeYAxisForm(val, 'show')"
            name="yAxis"
            :title="$t('chart.yAxis')"
          >
            <y-axis-selector
              class="attr-selector"
              :property-inner="propertyInnerAll['y-axis-selector']"
              :themes="themes"
              :chart="chart"
              @onChangeYAxisForm="onChangeYAxisForm"
            />
          </collapse-switch-item>
        </el-collapse>
      </el-row>
    </div>
  </el-row>
</template>

<style lang="less" scoped>
.ed-row {
  display: block;
}

.prop {
  border-bottom: 1px solid @side-outline-border-color;
}
.prop-top {
  border-top: 1px solid @side-outline-border-color;
}

span {
  font-size: 14px;
}

.view-panel {
  display: flex;
  height: 100%;
  width: 100%;
}

.attr-style {
  overflow-y: auto;
  height: 100%;
  width: 100%;
}

.de-collapse-style {
  :deep(.ed-form-item) {
    display: block;
    margin-bottom: 8px;
  }
  :deep(.ed-form-item__label) {
    justify-content: flex-start;
  }
}
:deep(.ed-collapse-item) {
  &:first-child {
    .ed-collapse-item__header {
      border-top: none;
    }
  }
}
.style-collapse:empty {
  border-bottom: none;
}
</style>
