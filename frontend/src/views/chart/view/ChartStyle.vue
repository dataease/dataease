<template>
  <el-row class="view-panel">
    <plugin-com
      v-if="view.isPlugin"
      style="overflow:auto;border-right: 1px solid #e6e6e6;height: 100%;width: 100%;"
      class="attr-style theme-border-class"
      :component-name="view.type + '-style'"
      :obj="{view, param, chart}"
    />
    <div
      v-else
      style="overflow:auto;border-right: 1px solid #e6e6e6;height: 100%;width: 100%;padding-right: 6px"
      class="attr-style theme-border-class"
    >
      <el-row class="padding-lr">
        <span class="title-text">{{ $t('chart.style_priority') }}</span>
        <el-row>
          <el-radio-group
            v-model="view.stylePriority"
            class="radio-span"
            size="mini"
            @change="calcStyle"
          >
            <el-radio label="view"><span>{{ $t('chart.chart') }}</span></el-radio>
            <el-radio label="panel"><span>{{ $t('chart.dashboard') }}</span></el-radio>
          </el-radio-group>
        </el-row>
      </el-row>
      <el-row>
        <span class="padding-lr">{{ $t('chart.shape_attr') }}</span>
        <el-collapse v-model="attrActiveNames" class="style-collapse">
          <el-collapse-item name="color" :title="$t('chart.color')">
            <color-selector
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onColorChange="onColorChange"
            />
          </el-collapse-item>
          <el-collapse-item
            v-show="view.render && view.render === 'echarts' && chart.type !== 'map' && chart.type !== 'waterfall' && chart.type !== 'word-cloud'"
            name="size"
            :title="$t('chart.size')"
          >
            <size-selector
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onSizeChange="onSizeChange"
            />
          </el-collapse-item>
          <el-collapse-item
            v-show="view.render && view.render === 'antv' && chart.type !== 'map' && chart.type !== 'waterfall' && chart.type !== 'word-cloud' && chart.type !== 'treemap' && chart.type !== 'funnel' && chart.type !== 'bar-stack'"
            name="size"
            :title="(chart.type && chart.type.includes('table')) ? $t('chart.table_config') : $t('chart.size')"
          >
            <size-selector-ant-v
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onSizeChange="onSizeChange"
            />
          </el-collapse-item>
          <el-collapse-item
            v-show="!view.type.includes('table') && !view.type.includes('text') && view.type !== 'word-cloud' && view.type !== 'label'"
            name="label"
            :title="$t('chart.label')"
          >
            <label-selector
              v-if="view.render && view.render === 'echarts'"
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onLabelChange="onLabelChange"
            />
            <label-selector-ant-v
              v-else-if="view.render && view.render === 'antv'"
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onLabelChange="onLabelChange"
            />
          </el-collapse-item>
          <el-collapse-item
            v-show="!view.type.includes('table') && !view.type.includes('text') && view.type !== 'liquid' && view.type !== 'gauge' && view.type !== 'label'"
            name="tooltip"
            :title="$t('chart.tooltip')"
          >
            <tooltip-selector
              v-if="view.render && view.render === 'echarts'"
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onTooltipChange="onTooltipChange"
            />
            <tooltip-selector-ant-v
              v-else-if="view.render && view.render === 'antv'"
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onTooltipChange="onTooltipChange"
            />
          </el-collapse-item>
          <el-collapse-item
            v-show="view.type === 'table-pivot'"
            name="totalCfg"
            :title="$t('chart.total_cfg')"
          >
            <total-cfg
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onTotalCfgChange="onTotalCfgChange"
            />
          </el-collapse-item>
        </el-collapse>
      </el-row>
      <el-row>
        <span class="padding-lr">{{ $t('chart.module_style') }}</span>
        <el-collapse v-model="styleActiveNames" class="style-collapse">
          <el-collapse-item
            v-show="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('scatter') || view.type === 'chart-mix' || view.type === 'waterfall')"
            name="xAxis"
            :title="$t('chart.xAxis')"
          >
            <x-axis-selector
              v-if="view.render && view.render === 'echarts'"
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onChangeXAxisForm="onChangeXAxisForm"
            />
            <x-axis-selector-ant-v
              v-else-if="view.render && view.render === 'antv'"
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onChangeXAxisForm="onChangeXAxisForm"
            />
          </el-collapse-item>
          <el-collapse-item
            v-show="view.type && (view.type.includes('bar') || view.type.includes('line') || view.type.includes('scatter') || view.type === 'chart-mix' || view.type === 'waterfall')"
            name="yAxis"
            :title="view.type === 'chart-mix' ? $t('chart.yAxis_main') : $t('chart.yAxis')"
          >
            <y-axis-selector
              v-if="view.render && view.render === 'echarts'"
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onChangeYAxisForm="onChangeYAxisForm"
            />
            <y-axis-selector-ant-v
              v-else-if="view.render && view.render === 'antv'"
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onChangeYAxisForm="onChangeYAxisForm"
            />
          </el-collapse-item>
          <el-collapse-item
            v-show="view.type && view.type === 'chart-mix'"
            name="yAxisExt"
            :title="$t('chart.yAxis_ext')"
          >
            <y-axis-ext-selector
              v-if="view.render && view.render === 'echarts'"
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onChangeYAxisForm="onChangeYAxisExtForm"
            />
            <y-axis-ext-selector-ant-v
              v-else-if="view.render && view.render === 'antv'"
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onChangeYAxisForm="onChangeYAxisExtForm"
            />
          </el-collapse-item>
          <el-collapse-item
            v-show="view.type && view.type.includes('radar')"
            name="split"
            :title="$t('chart.split')"
          >
            <split-selector
              v-if="view.render && view.render === 'echarts'"
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onChangeSplitForm="onChangeSplitForm"
            />
            <size-selector-ant-v
              v-else-if="view.render && view.render === 'antv'"
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onChangeSplitForm="onChangeSplitForm"
            />
          </el-collapse-item>
          <el-collapse-item v-show="view.type" name="title" :title="$t('chart.title')">
            <title-selector
              v-if="view.render && view.render === 'echarts'"
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onTextChange="onTextChange"
            />
            <title-selector-ant-v
              v-else-if="view.render && view.render === 'antv'"
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onTextChange="onTextChange"
            />
          </el-collapse-item>
          <el-collapse-item
            v-show="view.type && view.type !== 'map' && !view.type.includes('table') && !view.type.includes('text') && view.type !== 'label' && (chart.type !== 'treemap' || chart.render === 'antv') && view.type !== 'liquid' && view.type !== 'waterfall' && chart.type !== 'gauge' && chart.type !== 'word-cloud'"
            name="legend"
            :title="$t('chart.legend')"
          >
            <legend-selector
              v-if="view.render && view.render === 'echarts'"
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onLegendChange="onLegendChange"
            />
            <legend-selector-ant-v
              v-else-if="view.render && view.render === 'antv'"
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onLegendChange="onLegendChange"
            />
          </el-collapse-item>
          <el-collapse-item
            v-if="view.customStyle && view.customStyle.background"
            name="background"
            :title="$t('chart.background')"
          >
            <background-color-selector
              :param="param"
              class="attr-selector"
              :chart="chart"
              @onChangeBackgroundForm="onChangeBackgroundForm"
            />
          </el-collapse-item>
        </el-collapse>
      </el-row>
    </div>
  </el-row>
</template>
<script>
import PluginCom from '@/views/system/plugin/PluginCom'
import ColorSelector from '@/views/chart/components/shape-attr/ColorSelector'
import SizeSelector from '@/views/chart/components/shape-attr/SizeSelector'
import SizeSelectorAntV from '@/views/chart/components/shape-attr/SizeSelectorAntV'
import LabelSelector from '@/views/chart/components/shape-attr/LabelSelector'
import LabelSelectorAntV from '@/views/chart/components/shape-attr/LabelSelectorAntV'
import TooltipSelector from '@/views/chart/components/shape-attr/TooltipSelector'
import TooltipSelectorAntV from '@/views/chart/components/shape-attr/TooltipSelectorAntV'
import TotalCfg from '@/views/chart/components/shape-attr/TotalCfg'
import XAxisSelector from '@/views/chart/components/component-style/XAxisSelector'
import XAxisSelectorAntV from '@/views/chart/components/component-style/XAxisSelectorAntV'
import YAxisSelector from '@/views/chart/components/component-style/YAxisSelector'
import YAxisSelectorAntV from '@/views/chart/components/component-style/YAxisSelectorAntV'
import YAxisExtSelector from '@/views/chart/components/component-style/YAxisExtSelector'
import YAxisExtSelectorAntV from '@/views/chart/components/component-style/YAxisExtSelectorAntV'
import SplitSelector from '@/views/chart/components/component-style/SplitSelector'
import TitleSelector from '@/views/chart/components/component-style/TitleSelector'
import TitleSelectorAntV from '@/views/chart/components/component-style/TitleSelectorAntV'
import LegendSelector from '@/views/chart/components/component-style/LegendSelector'
import LegendSelectorAntV from '@/views/chart/components/component-style/LegendSelectorAntV'
import BackgroundColorSelector from '@/views/chart/components/component-style/BackgroundColorSelector'

export default {
  name: 'ChartStyle',
  components: {
    BackgroundColorSelector,
    LegendSelectorAntV,
    LegendSelector,
    TitleSelectorAntV,
    TitleSelector,
    SplitSelector,
    YAxisExtSelectorAntV,
    YAxisExtSelector,
    YAxisSelectorAntV,
    YAxisSelector,
    XAxisSelectorAntV,
    XAxisSelector,
    TotalCfg,
    TooltipSelectorAntV,
    TooltipSelector,
    LabelSelectorAntV, LabelSelector, SizeSelectorAntV, SizeSelector, ColorSelector, PluginCom
  },
  props: {
    chart: {
      type: Object,
      required: true
    },
    param: {
      type: Object,
      required: true
    },
    view: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      attrActiveNames: [],
      styleActiveNames: []
    }
  },
  created() {

  },
  methods: {
    calcStyle() {
      this.$emit('calcStyle')
    },
    onColorChange(val) {
      this.$emit('onColorChange', val)
    },
    onSizeChange(val) {
      this.$emit('onSizeChange', val)
    },
    onLabelChange(val) {
      this.$emit('onLabelChange', val)
    },
    onTooltipChange(val) {
      this.$emit('onTooltipChange', val)
    },
    onTotalCfgChange(val) {
      this.$emit('onTotalCfgChange', val)
    },
    onChangeXAxisForm(val) {
      this.$emit('onChangeXAxisForm', val)
    },
    onChangeYAxisForm(val) {
      this.$emit('onChangeYAxisForm', val)
    },
    onChangeYAxisExtForm(val) {
      this.$emit('onChangeYAxisExtForm', val)
    },
    onChangeSplitForm(val) {
      this.$emit('onChangeSplitForm', val)
    },
    onTextChange(val) {
      this.$emit('onTextChange', val)
    },
    onLegendChange(val) {
      this.$emit('onLegendChange', val)
    },
    onChangeBackgroundForm(val) {
      this.$emit('onChangeBackgroundForm', val)
    }
  }
}
</script>

<style lang='scss' scoped>
  .padding-lr {
    padding: 0 6px;
  }
  .col {
    width: 40%;
    flex: 1;
    padding: 10px;
    border: solid 1px #eee;
    border-radius: 5px;
    float: left;
  }

  .col + .col {
    margin-left: 10px;
  }

  .view-panel {
    display: flex;
    height: 100%;
    background-color: #f7f8fa;
  }

  .blackTheme .view-panel {
    background-color: var(--MainBG);
  }

  .el-form-item {
    margin-bottom: 0;
  }

  span {
    font-size: 12px;
  }

  .tab-header > > > .el-tabs__header {
    border-top: solid 1px #eee;
    border-right: solid 1px #eee;
  }

  .tab-header > > > .el-tabs__item {
    font-size: 12px;
    padding: 0 20px !important;
  }

  .blackTheme .tab-header > > > .el-tabs__item {
    background-color: var(--MainBG);
  }

  .tab-header > > > .el-tabs__nav-scroll {
    padding-left: 0 !important;
  }

  .tab-header > > > .el-tabs__header {
    margin: 0 !important;
  }

  .tab-header > > > .el-tabs__content {
    height: calc(100% - 40px);
  }

  .chart-icon {
    width: 20px;
    height: 20px;
  }

  .el-radio {
    margin: 5px;
  }

  .el-radio > > > .el-radio__label {
    padding-left: 0;
  }

  .attr-style {
    height: calc(100vh - 56px - 60px - 40px - 40px);
  }

  .blackTheme .attr-style {
    color: var(--TextPrimary);
  }

  .attr-selector {
    width: 100%;
    height: 100%;
    margin: 6px 0;
    padding: 0 4px;
    display: flex;
    align-items: center;
    background-color: white
  }

  .blackTheme .attr-selector {

    background-color: var(--MainBG)
  }

  .dialog-css > > > .el-dialog__title {
    font-size: 14px;
  }

  .dialog-css > > > .el-dialog__header {
    padding: 20px 20px 0;
  }

  .dialog-css > > > .el-dialog__body {
    padding: 10px 20px 20px;
  }

  .blackTheme .theme-border-class {
    color: var(--TextPrimary) !important;
    background-color: var(--ContentBG);
  }

  .blackTheme .padding-lr {
    border-color: var(--TableBorderColor) !important;
  }

  .blackTheme .theme-item-class {
    background-color: var(--MainBG) !important;
    border-color: var(--TableBorderColor) !important;
  }

  .icon-class {
    color: #6c6c6c;
  }

  .blackTheme .icon-class {
    color: #cccccc;
  }

  .radio-span > > > .el-radio__label {
    margin-left: 4px;
  }

  .view-title-name {
    display: -moz-inline-box;
    display: inline-block;
    width: 130px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
    margin-left: 45px;
  }

  ::v-deep .item-axis {
    width: 128px !important;
  }

  ::v-deep .el-slider__input {
    width: 80px !important;
  }

  ::v-deep .el-input-number--mini {
    width: 100px !important;
  }

  ::v-deep .el-slider__runway.show-input{
    width: 80px!important;
  }

  .no-senior {
    width: 100%;
    text-align: center;
    font-size: 12px;
    padding-top: 40px;
    overflow: auto;
    border-right: 1px solid #e6e6e6;
    height: 100%;
  }

  .form-item-slider>>>.el-form-item__label{
    font-size: 12px;
    line-height: 38px;
  }
  .form-item>>>.el-form-item__label{
    font-size: 12px;
  }
  </style>
