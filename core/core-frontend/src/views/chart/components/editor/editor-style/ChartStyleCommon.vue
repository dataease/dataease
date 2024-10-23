<script setup lang="ts">
import { PluginComponent } from '@/components/plugin'
import ChartStyle from '@/views/chart/components/editor/editor-style/ChartStyle.vue'
import { PropType } from 'vue'
const emit = defineEmits([
  'onColorChange',
  'onMiscChange',
  'onLabelChange',
  'onTooltipChange',
  'onChangeXAxisForm',
  'onChangeYAxisForm',
  'onChangeYAxisExtForm',
  'onTextChange',
  'onLegendChange',
  'onBasicStyleChange',
  'onBackgroundChange',
  'onStyleAttrChange',
  'onTableHeaderChange',
  'onTableCellChange',
  'onTableTotalChange',
  'onChangeMiscStyleForm',
  'onExtTooltipChange',
  'onIndicatorChange',
  'onIndicatorNameChange',
  'onChangeQuadrantForm',
  'onChangeFlowMapLineForm',
  'onChangeFlowMapPointForm'
])
const props = defineProps({
  commonBackgroundPop: {
    type: Object,
    required: false
  },
  commonBorderPop: {
    type: Object,
    required: false
  },
  eventInfo: {
    type: Object,
    required: false
  },
  view: {
    type: Object as PropType<ChartObj>,
    required: true
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  },
  dimension: {
    type: Array,
    required: true
  },
  quota: {
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
  },
  selectorSpec: {
    type: Object as PropType<EditorSelectorSpec>,
    required: false,
    default: () => {
      return {}
    }
  },
  allFields: {
    type: Array,
    required: true
  }
})
</script>

<template>
  <el-container direction="vertical" id="main-style-p">
    <el-scrollbar class="drag_main_area">
      <template v-if="view.plugin?.isPlugin">
        <plugin-component
          :jsname="view.plugin.staticMap['editor-style']"
          :view="view"
          :dimension="dimension"
          :quota="quota"
          :themes="themes"
          :emitter="emitter"
        />
      </template>
      <template v-else>
        <chart-style
          v-if="chartStyleShow"
          :properties="chartViewInstance.properties"
          :property-inner-all="chartViewInstance.propertyInner"
          :selector-spec="chartViewInstance.selectorSpec"
          :common-background-pop="curComponent?.commonBackground"
          :common-border-pop="curComponent?.style"
          :event-info="curComponent?.events"
          :chart="view"
          :themes="themes"
          :dimension-data="dimension"
          :quota-data="quota"
          :all-fields="allFields"
          @onColorChange="(val, prop) => emit('onColorChange', val, prop)"
          @onMiscChange="(val, prop) => emit('onMiscChange', val, prop)"
          @onLabelChange="(val, prop) => emit('onLabelChange', val, prop)"
          @onTooltipChange="(val, prop) => emit('onTooltipChange', val, prop)"
          @onChangeXAxisForm="(val, prop) => emit('onChangeXAxisForm', val, prop)"
          @onChangeYAxisForm="(val, prop) => emit('onChangeYAxisForm', val, prop)"
          @onChangeYAxisExtForm="(val, prop) => emit('onChangeYAxisExtForm', val, prop)"
          @onTextChange="(val, prop) => emit('onTextChange', val, prop)"
          @onIndicatorChange="(val, prop) => emit('onIndicatorChange', val, prop)"
          @onIndicatorNameChange="(val, prop) => emit('onIndicatorNameChange', val, prop)"
          @onLegendChange="(val, prop) => emit('onLegendChange', val, prop)"
          @onBackgroundChange="(val, prop) => emit('onBackgroundChange', val, prop)"
          @onStyleAttrChange="(val, prop) => emit('onStyleAttrChange', val, prop)"
          @onBasicStyleChange="(val, prop) => emit('onBasicStyleChange', val, prop)"
          @onTableHeaderChange="(val, prop) => emit('onTableHeaderChange', val, prop)"
          @onTableCellChange="(val, prop) => emit('onTableCellChange', val, prop)"
          @onTableTotalChange="(val, prop) => emit('onTableTotalChange', val, prop)"
          @onChangeMiscStyleForm="(val, prop) => emit('onChangeMiscStyleForm', val, prop)"
          @onExtTooltipChange="(val, prop) => emit('onExtTooltipChange', val, prop)"
          @onChangeQuadrantForm="(val, prop) => emit('onChangeQuadrantForm', val, prop)"
          @onChangeFlowMapLineForm="(val, prop) => emit('onChangeFlowMapLineForm', val, prop)"
          @onChangeFlowMapPointForm="(val, prop) => emit('onChangeFlowMapPointForm', val, prop)"
        />
      </template>
    </el-scrollbar>
  </el-container>
</template>

<style scoped lang="less"></style>
