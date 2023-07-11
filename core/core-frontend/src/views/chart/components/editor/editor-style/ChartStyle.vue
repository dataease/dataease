<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { toRefs } from 'vue'
import ColorSelector from '@/views/chart/components/editor/editor-style/components/ColorSelector.vue'
import SizeSelector from '@/views/chart/components/editor/editor-style/components/SizeSelector.vue'
import LabelSelector from '@/views/chart/components/editor/editor-style/components/LabelSelector.vue'
import TooltipSelector from '@/views/chart/components/editor/editor-style/components/TooltipSelector.vue'
import XAxisSelector from '@/views/chart/components/editor/editor-style/components/XAxisSelector.vue'
import YAxisSelector from '@/views/chart/components/editor/editor-style/components/YAxisSelector.vue'
import TitleSelector from '@/views/chart/components/editor/editor-style/components/TitleSelector.vue'
import LegendSelector from '@/views/chart/components/editor/editor-style/components/LegendSelector.vue'
import BackgroundOverallComponent from '@/components/visualization/component-background/BackgroundOverallComponent.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import CollapseSwitchItem from '@/components/collapse-switch-item/src/CollapseSwitchItem.vue'
import { ElCollapseItem } from 'element-plus-secondary'
import BasicStyleSelector from '@/views/chart/components/editor/editor-style/components/BasicStyleSelector.vue'
import ComponentPosition from '@/components/visualization/common/ComponentPosition.vue'
const dvMainStore = dvMainStoreWithOut()
const { curComponent, dvInfo } = storeToRefs(dvMainStore)
const { t } = useI18n()

const state = {
  attrActiveNames: [],
  styleActiveNames: []
}

const props = defineProps({
  chart: {
    type: Object,
    required: true
  },
  themes: {
    type: String,
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
    type: Array,
    required: false,
    default: () => {
      return []
    }
  },
  propertyInnerAll: {
    type: Object,
    required: false,
    default: () => {
      return []
    }
  }
})

const { chart, themes, properties, propertyInnerAll } = toRefs(props)
const emit = defineEmits([
  'onColorChange',
  'onSizeChange',
  'onLabelChange',
  'onTooltipChange',
  'onChangeXAxisForm',
  'onChangeYAxisForm',
  'onTextChange',
  'onLegendChange',
  'onBasicStyleChange'
])

const showProperties = property => {
  return properties.value && properties.value && properties.value.includes(property)
}

const showPropertiesCollapse = propertiesInfo => {
  let includeCount = 0
  propertiesInfo.forEach(property => {
    properties.value.includes(property) && includeCount++
  })
  return includeCount > 0
}

const onColorChange = val => {
  emit('onColorChange', val)
}

const onSizeChange = val => {
  emit('onSizeChange', val)
}

const onLabelChange = val => {
  emit('onLabelChange', val)
}

const onTooltipChange = val => {
  emit('onTooltipChange', val)
}

const onChangeXAxisForm = val => {
  emit('onChangeXAxisForm', val)
}

const onChangeYAxisForm = val => {
  emit('onChangeYAxisForm', val)
}

const onTextChange = val => {
  emit('onTextChange', val)
}

const onLegendChange = val => {
  emit('onLegendChange', val)
}
const onBasicStyleChange = val => {
  emit('onBasicStyleChange', val)
}
</script>

<template>
  <el-row class="view-panel" :class="'style-' + themes">
    <div class="attr-style">
      <el-row class="de-collapse-style">
        <el-collapse v-model="state.attrActiveNames" class="style-collapse">
          <el-collapse-item name="position" :title="'位置'" v-if="dvInfo.type !== 'dashboard'">
            <component-position></component-position>
          </el-collapse-item>
          <el-collapse-item name="background" :title="'背景'">
            <background-overall-component
              v-if="curComponent"
              :themes="themes"
              position="component"
            ></background-overall-component>
          </el-collapse-item>
          <el-collapse-item
            name="basicStyle"
            title="基础样式"
            v-if="showPropertiesCollapse(['basic-style-selector'])"
          >
            <basic-style-selector
              :property-inner="propertyInnerAll['basic-style-selector']"
              :themes="themes"
              :chart="chart"
              @onBasicStyleChange="onBasicStyleChange"
            />
          </el-collapse-item>
          <el-collapse-item
            name="color"
            :title="t('chart.color')"
            v-if="showPropertiesCollapse(['color-selector'])"
          >
            <color-selector
              :property-inner="propertyInnerAll['color-selector']"
              :themes="themes"
              class="attr-selector"
              :chart="chart"
              @onColorChange="onColorChange"
            />
          </el-collapse-item>

          <el-collapse-item
            v-if="showPropertiesCollapse(['size-selector']) && chart.type !== 'word-cloud'"
            name="size"
            :title="
              chart.type && chart.type.includes('table') ? t('chart.table_config') : t('chart.size')
            "
          >
            <size-selector
              :property-inner="propertyInnerAll['size-selector']"
              :themes="themes"
              class="attr-selector"
              :chart="chart"
              :quota-fields="props.quotaData"
              @onSizeChange="onSizeChange"
            />
          </el-collapse-item>
          <collapse-switch-item
            v-if="
              showPropertiesCollapse(['label-selector']) &&
              chart.type !== 'word-cloud' &&
              !chart.type.includes('table')
            "
            v-model="chart.customAttr.label.show"
            :change-model="chart.customAttr.label"
            @modelChange="onLabelChange"
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
            v-if="
              showPropertiesCollapse(['tooltip-selector']) &&
              chart.type !== 'gauge' &&
              chart.type !== 'liquid' &&
              !chart.type.includes('table')
            "
            v-model="chart.customAttr.tooltip.show"
            :change-model="chart.customAttr.tooltip"
            @modelChange="onTooltipChange"
            name="tooltip"
            :title="$t('chart.tooltip')"
          >
            <tooltip-selector
              class="attr-selector"
              :property-inner="propertyInnerAll['tooltip-selector']"
              :themes="themes"
              :chart="chart"
              @onTooltipChange="onTooltipChange"
            />
          </collapse-switch-item>
        </el-collapse>
      </el-row>

      <el-row class="de-collapse-style">
        <el-collapse v-model="state.styleActiveNames" class="style-collapse">
          <collapse-switch-item
            v-if="
              showPropertiesCollapse(['x-axis-selector']) &&
              (chart.type.includes('bar') || chart.type.includes('line'))
            "
            v-model="chart.customStyle.xAxis.show"
            :change-model="chart.customStyle.xAxis"
            @modelChange="onChangeXAxisForm"
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
            v-if="
              showPropertiesCollapse(['y-axis-selector']) &&
              (chart.type.includes('bar') || chart.type.includes('line'))
            "
            v-model="chart.customStyle.yAxis.show"
            :change-model="chart.customStyle.yAxis"
            @modelChange="onChangeYAxisForm"
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

          <collapse-switch-item
            v-model="chart.customStyle.text.show"
            v-if="showPropertiesCollapse(['title-selector'])"
            :change-model="chart.customStyle.text"
            @modelChange="onTextChange"
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
            v-if="
              showPropertiesCollapse(['legend-selector']) &&
              chart.type !== 'word-cloud' &&
              chart.type !== 'gauge' &&
              chart.type !== 'liquid' &&
              chart.type !== 'map' &&
              !chart.type.includes('table')
            "
            v-model="chart.customStyle.legend.show"
            :change-model="chart.customStyle.legend"
            @modelChange="onLegendChange"
            name="legend"
            :title="$t('chart.legend')"
          >
            <legend-selector
              class="attr-selector"
              :property-inner="propertyInnerAll['legend-selecto']"
              :themes="themes"
              :chart="chart"
              @onLegendChange="onLegendChange"
            />
          </collapse-switch-item>
        </el-collapse>
      </el-row>
    </div>
  </el-row>
</template>

<style lang="less" scoped>
.style-light {
  border-top: 1px solid @side-outline-border-color-light;
}

.style-dark {
  border-top: 1px solid @side-outline-border-color;
}

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
  :deep(.ed-collapse-item__header) {
    height: 34px !important;
    line-height: 34px !important;
    padding: 0 0 0 6px !important;
    font-size: 12px !important;
    font-weight: 400 !important;
  }
  :deep(.ed-collapse-item__content) {
    padding: 16px !important;
  }
  :deep(.ed-form-item) {
    display: block;
    margin-bottom: 16px;
  }
  :deep(.ed-form-item__label) {
    justify-content: flex-start;
  }
  :deep(.ed-checkbox__inner) {
    width: 14px;
    height: 14px;
  }
}
</style>
