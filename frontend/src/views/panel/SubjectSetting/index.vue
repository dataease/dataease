<template>
  <el-row class="slider-container">
    <div
      style="height: 40px; padding-left: 15px; text-align: left; white-space: pre; text-overflow: ellipsis; left: 0px; right: 0px; top: 0px; font-weight: 700"
    >
      {{ $t('panel.dashboard_theme') }}
    </div>
    <div
      style="height: 1px; position: absolute; left: 15px; right: 15px; top: 40px; box-sizing:border-box;border-bottom: 1px solid #e8eaed"
    />
    <div>
      <slider v-if="sliderShow" @reload="sliderReload" />
    </div>
    <!--折叠面板-->
    <div v-if="collapseShow" style="margin: 10px;overflow-y: auto">
      <el-collapse v-model="activeNames" @change="handleChange">
        <el-collapse-item :title="$t('chart.style_priority')" name="priority">
          <el-row class="selector-div">
            <el-col>
              <el-radio-group
                v-model="stylePriority"
                size="mini"
                @change="onPriorityChange"
              >
                <el-radio label="view"><span>{{ $t('chart.chart') }}</span></el-radio>
                <el-radio label="panel"><span>{{ $t('chart.dashboard') }}</span></el-radio>
              </el-radio-group>
            </el-col>
          </el-row>
        </el-collapse-item>
        <el-collapse-item :title="$t('panel.panel')" name="panel">
          <el-row class="selector-div">
            <SetDrawSize class="attr-selector" />
            <SetDrawFont class="attr-selector" />
            <SetRuleColor class="attr-selector" />
            <background-selector class="attr-selector" />
            <component-gap class="attr-selector" />
            <panel-refresh-time class="attr-selector" />
            <panel-view-result class="attr-selector" />
          </el-row>
        </el-collapse-item>
        <el-collapse-item :title="$t('chart.module_style')" name="component">
          <el-row class="selector-div">
            <panel-background-color-selector
              v-if="chart"
              class="attr-selector"
              :chart="chart"
              @onChangeBackgroundForm="onChangeBackgroundForm"
            />
          </el-row>
          <el-row class="selector-div">
            <panel-title-selector
              v-if="chart"
              class="attr-selector"
              :chart="chart"
              @onTitleChange="onTitleChange"
            />
          </el-row>
        </el-collapse-item>
        <el-collapse-item :title="$t('chart.shape_attr')" name="graphical">
          <el-row class="selector-div">
            <panel-color-selector
              :source-type="'panelEchart'"
              class="attr-selector"
              :chart="chart"
              @onColorChange="onColorChange"
            />
          </el-row>
        </el-collapse-item>
        <el-collapse-item :title="$t('panel.table')" name="table">
          <el-row class="selector-div">
            <panel-color-selector
              index="10002"
              :source-type="'panelTable'"
              class="attr-selector"
              :chart="tableChart"
              @onColorChange="onTableColorChange"
            />
          </el-row>
        </el-collapse-item>
      </el-collapse>
    </div>
  </el-row>
</template>

<script>
import slider from './PreSubject/Slider'
import BackgroundSelector from './PanelStyle/BackgroundSelector'
import SetDrawSize from './PanelStyle/setDrawSize'
import SetRuleColor from './PanelStyle/setRuleColor'
import SetDrawFont from './PanelStyle/setDrawFont'
import PanelBackgroundColorSelector from './PanelStyle/PanelBackgroundColorSelector'
import PanelTitleSelector from './PanelStyle/PanelTitleSelector'
import PanelColorSelector from './PanelStyle/PanelColorSelector'
import ComponentGap from './PanelStyle/ComponentGap'
import PanelRefreshTime from './PanelStyle/PanelRefreshTime'

import { mapState } from 'vuex'
import { deepCopy } from '@/components/canvas/utils/utils'
import bus from '@/utils/bus'
import PanelViewResult from '@/views/panel/SubjectSetting/PanelStyle/PanelViewResult'

export default {
  components: {
    PanelViewResult,
    SetDrawSize,
    SetRuleColor,
    slider,
    BackgroundSelector,
    ComponentGap,
    PanelColorSelector,
    PanelBackgroundColorSelector,
    PanelTitleSelector,
    PanelRefreshTime,
    SetDrawFont
  },
  data() {
    return {
      sliderShow: true,
      panelInfo: this.$store.state.panel.panelInfo,
      activeNames: ['panel'],
      chart: null,
      tableChart: null,
      collapseShow: true,
      stylePriority: 'view'
    }
  },
  computed: mapState([
    'canvasStyleData'
  ]),

  watch: {},

  mounted() {
    bus.$on('onSubjectChange', () => {
      this.collapseShow = false
      this.$nextTick(() => {
        this.init()
        this.collapseShow = true
      })
    })
  },
  created() {
    this.init()
  },

  methods: {
    sliderReload() {
      this.sliderShow = false
      this.$nextTick(() => {
        this.sliderShow = true
      })
    },
    init() {
      // 初始化赋值
      const chart = deepCopy(this.canvasStyleData.chart)
      if (chart.xaxis) {
        chart.xaxis = JSON.parse(chart.xaxis)
      }
      if (chart.yaxis) {
        chart.yaxis = JSON.parse(chart.yaxis)
      }
      chart.customAttr = JSON.parse(chart.customAttr)
      chart.customStyle = JSON.parse(chart.customStyle)
      console.log('对过滤值进行处理-------------------------------------------------------------------------------', chart.customFilter)
      chart.customFilter = JSON.parse(chart.customFilter)
      this.chart = chart
      console.log('subject-setting,,init:::::', this.chart)

      // 样式优先级
      this.stylePriority = chart.stylePriority

      // 因为 table 的color 设置和view的共用 所以单独设置一个对象
      this.tableChart = deepCopy(this.chart)
      this.tableChart.customAttr.color = this.tableChart.customAttr.tableColor
    },
    handleChange(val) {
    },
    onChangePanelStyle(parma) {
    },
    onColorChange(val) {
      console.log('colors', val)
      this.chart.customAttr.color = val
      this.save()
    },
    onTableColorChange(val) {
      console.log('table', val)
      this.chart.customAttr.tableColor = val
      this.save()
    },
    onTextChange(val) {
      this.chart.customStyle.text = val
      this.save()
    },
    onChangeBackgroundForm(val) {
      this.chart.customStyle.background = val
      this.save()
    },
    onTitleChange(val) {
      // console.log('测试标题',val)
      this.chart.customStyle.text = val
      this.save()
    },
    onPriorityChange(val) {
      console.log(val)
      this.chart.stylePriority = this.stylePriority
      this.save()
    },
    save() {
      const canvasStyleData = deepCopy(this.canvasStyleData)
      const chart = deepCopy(this.chart)
      chart.xaxis = JSON.stringify(this.chart.xaxis)
      chart.yaxis = JSON.stringify(this.chart.yaxis)
      chart.customAttr = JSON.stringify(this.chart.customAttr)
      chart.customStyle = JSON.stringify(this.chart.customStyle)
      chart.customFilter = JSON.stringify(this.chart.customFilter)
      canvasStyleData.chart = chart
      console.log('改变的', canvasStyleData)
      this.$store.commit('setCanvasStyle', canvasStyleData)
      this.$store.commit('recordSnapshot', 'save')
      //  判断是否使用 仪表板的样式
      if (chart.stylePriority === 'panel') {
        this.$store.commit('setTemplateStatus', true)
        this.$store.commit('setPriorityStatus', false)
      } else {
        this.$store.commit('setTemplateStatus', false)
        this.$store.commit('setPriorityStatus', true)
      }

      bus.$emit('plugins-calc-style')
    },
    styleChange() {
      this.$store.state.styleChangeTimes++
    }
  }
}
</script>

<style lang="scss" scoped>
  .slider-container {
    width: 100%;
    overflow: hidden auto;
    min-height: 24px;
    padding-top: 0px;
    padding-bottom: 0px;
    position: relative;
    max-height: 976px;
    color: #3d4d66;
    font-size: 12px;
  }

  .attr-selector {
    background-color: white;
    height: 32px;
    margin: 5px 5px 5px 5px;
    padding: 0 4px;
    display: flex;
    align-items: center;
    z-index: 10001;
  }

  .blackTheme .attr-selector {
    background-color: var(--MainBG)
  }

  .selector-div {
    background-color: var(--MainBG, #f7f8fa);
    margin: 5px
  }

</style>
