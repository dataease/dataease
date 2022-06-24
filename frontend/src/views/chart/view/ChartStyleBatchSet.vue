<template>
  <div class="batch-opt-main">
    <el-row style="height: 40px">
      <span class="title-text view-title-name" style="line-height: 40px;">{{ $t('panel.batch_opt') }}</span>
    </el-row>
    <chart-style
      v-if="mixProperties&&batchOptChartInfo"
      class="chart-style-main"
      :param="param"
      :view="batchOptChartInfo"
      :chart="batchOptChartInfo"
      :properties="mixProperties"
      :property-inner-all="mixPropertiesInner"
      @calcStyle="calcStyle"
      @onColorChange="onColorChange"
      @onSizeChange="onSizeChange"
      @onLabelChange="onLabelChange"
      @onTooltipChange="onTooltipChange"
      @onTotalCfgChange="onTotalCfgChange"
      @onChangeXAxisForm="onChangeXAxisForm"
      @onChangeYAxisForm="onChangeYAxisForm"
      @onChangeYAxisExtForm="onChangeYAxisExtForm"
      @onChangeSplitForm="onChangeSplitForm"
      @onTextChange="onTextChange"
      @onLegendChange="onLegendChange"
    />
    <el-row v-else>
      <div class="view-selected-message-class">
        <span style="font-size: 14px;margin-left: 10px;font-weight: bold;line-height: 20px">{{ $t('panel.select_view') }}</span>
      </div>
    </el-row>
  </div>

</template>

<script>
import ChartStyle from '@/views/chart/view/ChartStyle'
import { mapState } from 'vuex'
import bus from '@/utils/bus'
export default {
  name: 'ChartStyleBatchSet',
  components: { ChartStyle },
  props: {

  },
  data() {
    return {
      param: { 'id': 'mixId', 'optType': 'edit' }
    }
  },
  computed: {
    ...mapState([
      'batchOptChartInfo',
      'mixProperties',
      'mixPropertiesInner'
    ])
  },
  watch: {
  },
  mounted() {
  },
  methods: {
    calcStyle() {
      this.$emit('calcStyle')
    },
    onColorChange(val) {
      this.batchOptChange('customAttr', 'color', val)
    },
    onSizeChange(val) {
      this.batchOptChange('customAttr', 'size', val)
    },
    onLabelChange(val) {
      this.batchOptChange('customAttr', 'label', val)
    },
    onTooltipChange(val) {
      this.batchOptChange('customAttr', 'tooltip', val)
    },
    onTotalCfgChange(val) {
      this.batchOptChange('customAttr', 'totalCfg', val)
    },
    onChangeXAxisForm(val) {
      this.batchOptChange('customStyle', 'xAxis', val)
    },
    onChangeYAxisForm(val) {
      this.batchOptChange('customStyle', 'yAxis', val)
    },
    onChangeYAxisExtForm(val) {
      this.batchOptChange('customStyle', 'yAxisExt', val)
    },
    onChangeSplitForm(val) {
      this.batchOptChange('customStyle', 'split', val)
    },
    onTextChange(val) {
      this.batchOptChange('customStyle', 'text', val)
    },
    onLegendChange(val) {
      this.batchOptChange('customStyle', 'legend', val)
    },
    batchOptChange(custom, property, value) {
      this.$store.commit('setChangeProperties', {
        'custom': custom,
        'property': property,
        'value': value
      })
      bus.$emit('batch-opt-change', {
        'custom': custom,
        'property': property,
        'value': value
      })
    }
  }
}
</script>

<style scoped>

  .view-selected-message-class {
    font-size: 12px;
    color: #9ea6b2;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    height: calc(100vh - 56px);
  }

  .batch-opt-main{
    height:  calc(100vh - 56px);
    overflow-y: hidden;
    width: 100%;
    border-left: 1px solid #E6E6E6
  }

  .chart-style-main{
    height:  calc(100% - 40px)!important;
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
