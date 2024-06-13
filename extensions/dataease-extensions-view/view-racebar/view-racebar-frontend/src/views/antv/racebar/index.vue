<template>
  <div ref="chartContainer"
       style="padding: 0; width: 100%; height: 100%; overflow: hidden; display: flex; flex-direction: column;"
       :style="bg_class">

    <view-track-bar
      ref="viewTrack"
      :track-menu="trackMenu"
      class="track-bar"
      :style="trackBarStyleTime"
      @trackClick="trackClick"
    />
    <div
      :id="chartId"
      style="width: 100%;height: 100%;overflow: hidden;"
      :style="{ borderRadius: borderRadius}"
    />
    <!--    <div style="padding: 0 25px; margin: 0 20px 20px"
             v-if="sliderShow">
          <el-slider
            v-model="currentIndex"
            show-stops
            :min="0"
            :max="maxIndex"
            :show-tooltip="false"
            :format-tooltip="formatSliderTooltip"
            :marks="sliderMarks"
            @change="onSliderChange"
          />
        </div>-->

  </div>
</template>

<script>
import ViewTrackBar from '../../../components/views/ViewTrackBar'
import {getRemark} from "../../../components/views/utils";
import {
  DEFAULT_TITLE_STYLE,
  DEFAULT_TOOLTIP,
  BASE_ECHARTS_SELECT,
  HORIZONTAL_BAR,
  uuid,
  hexColorToRGBA,
  reverseColor,
  componentStyle,
  seniorCfg,
  DEFAULT_SLIDER,
  DEFAULT_Graphic, getMarginUnit,
} from '../../../utils/map';
import ChartTitleUpdate from '../../../components/views/ChartTitleUpdate';
import {mapState} from 'vuex'
import _ from 'lodash';
import {valueFormatter} from '../../../utils/formatter'


export default {
  name: 'ChartComponent',
  components: {ViewTrackBar, ChartTitleUpdate},
  props: {
    chart: {
      type: Object,
      required: true
    },
    filter: {
      type: Object,
      required: false,
      default: function () {
        return {}
      }
    },
    trackMenu: {
      type: Array,
      required: false,
      default: function () {
        return ['drill']
      }
    },
    inScreen: {
      type: Boolean,
      required: false,
      default: true
    },
    searchCount: {
      type: Number,
      required: false,
      default: 0
    },
    scale: {
      type: Number,
      required: false,
      default: 1
    },
    themeStyle: {
      type: Object,
      required: false,
      default: null
    },
    bus: {
      type: Object,
      required: false,
      default: null
    },
    axiosRequest: {
      type: Function | Object,
      required: false,
      default: null
    },
  },
  data() {
    return {
      myOptions: undefined,
      intervalID: undefined,
      currentIndex: 0,
      myChart: null,
      chartId: uuid(),
      showTrackBar: true,
      trackBarStyle: {
        position: 'absolute',
        left: '0px',
        top: '0px',
        zIndex: 99
      },
      pointParam: null,
      dynamicAreaCode: null,
      borderRadius: '0px',
      title_show: true,
      antVRenderStatus: false,
      remarkCfg: {
        show: false,
        content: ''
      },
      buttonTextColor: null,
      titleClass: {
        margin: '0 0',
        zIndex: 1,
        width: '100%',
        fontSize: '18px',
        color: '#303133',
        textAlign: 'left',
        fontStyle: 'normal',
        fontWeight: 'normal',
        background: hexColorToRGBA('#ffffff', 0)
      },
      title_class: {
        margin: '0 0',
        width: '100%',
        fontSize: '18px',
        color: '#303133',
        textAlign: 'left',
        fontStyle: 'normal',
        fontWeight: 'normal',
        background: ''
      },
      linkageActiveParam: null,
      linkageActiveHistory: false,
    }
  },

  computed: {
    trackBarStyleTime() {
      return this.trackBarStyle
    },
    bg_class() {
      return {
        borderRadius: this.borderRadius
      }
    },
    chartInfo() {
      const {id, title} = this.chart
      return {id, title}
    },
    titleHeight() {
      if (this.$refs.title) {
        return this.$refs.title.offsetHeight + 'px';
      } else {
        return '0px';
      }
    },
    chartStyle() {
      return {height: `calc(100% - ${this.titleHeight})`};
    },
    maxIndex() {
      if (this.chart && this.chart.data && this.chart.data.extXs && this.chart.data.extXs.length > 0) {
        return this.chart.data.extXs.length - 1;
      } else {
        return 0;
      }
    },
    graphicShow() {
      if (this.chart && this.chart.customAttr) {
        const customAttr = JSON.parse(this.chart.customAttr)
        if (customAttr.graphic) {
          return customAttr.graphic.show;
        }
      }
      return DEFAULT_Graphic.show;
    },
    sliderShow() {
      if (this.chart && this.chart.customAttr) {
        const customAttr = JSON.parse(this.chart.customAttr)
        if (customAttr.slider) {
          return customAttr.slider.show;
        }
      }
      return DEFAULT_SLIDER.show;
    },
    sliderAuto() {
      if (this.chart && this.chart.customAttr) {
        const customAttr = JSON.parse(this.chart.customAttr)
        if (customAttr.slider) {
          return customAttr.slider.auto;
        }
      }
      return DEFAULT_SLIDER.auto;

    },
    sliderRepeat() {
      if (this.chart && this.chart.customAttr) {
        const customAttr = JSON.parse(this.chart.customAttr)
        if (customAttr.slider) {
          return customAttr.slider.repeat;
        }
      }
      return DEFAULT_SLIDER.repeat;

    },
    sliderTimeout() {
      if (this.chart && this.chart.customAttr) {
        const customAttr = JSON.parse(this.chart.customAttr)
        if (customAttr.slider && customAttr.slider.timeout && customAttr.slider.timeout > 0) {
          return customAttr.slider.timeout * 1000;
        }
      }
      return DEFAULT_SLIDER.timeout * 1000;
    },
    sliderMax() {
      if (this.chart && this.chart.customAttr) {
        const customAttr = JSON.parse(this.chart.customAttr)
        if (customAttr.slider && customAttr.slider.max && customAttr.slider.max >= 3) {
          return customAttr.slider.max - 1;
        }
      }
      return DEFAULT_SLIDER.max - 1;
    },
    sliderMarks() {
      const _list = this.chart.data ? this.chart.data.extXs : [];
      const _setting = {fontSize: DEFAULT_SLIDER.fontSize, color: DEFAULT_SLIDER.color};
      if (this.chart && this.chart.customAttr) {
        const customAttr = JSON.parse(this.chart.customAttr)
        if (customAttr.slider && customAttr.slider.show) {
          _setting['font-size'] = customAttr.slider.fontSize + 'px';
          _setting.color = customAttr.slider.color;
        }
      }
      const _result = {};
      for (let i = 0; i < _list.length; i++) {
        _result[i] = {
          style: _setting,
          label: this.$createElement('span', _list[i]),
        }
      }
      return _result;
    },
    ...mapState([
      'canvasStyleData'
    ])
  },
  watch: {
    chart: {
      handler(newVal, oldVal) {
        if (newVal && oldVal && JSON.stringify(newVal) === JSON.stringify(oldVal)) {
          return
        }
        this.preDraw()
      },
      deep: true
    },
    resize() {
      this.drawEcharts()
    },
  },
  mounted() {
    this.preDraw()
  },
  destroyed() {
    if (this.intervalID) {
      clearInterval(this.intervalID)
    }
    this.myChart.dispose()
  },
  created() {
    this.loadThemeStyle()
  },
  methods: {
    onSliderChange(val) {
      this.updateX(true, val);
    },
    formatSliderTooltip(val) {
      if (this.chart && this.chart.data && this.chart.data.extXs) {
        return this.chart.data.extXs[val];
      } else {
        return undefined;
      }
    },
    scrollStatusChange() {
      if (this.haveScrollType.includes(this.chart.type)) {
        const opt = this.myChart.getOption()
        this.adaptorOpt(opt)
        if (this.chart.type === 'treemap') {
          this.myChart.dispose()
          this.myChart = null
          this.preDraw()
        } else {
          this.myChart.setOption(opt)
        }
      }
    },
    adaptorOpt(opt) {
      const disabledStatus = !this.active
      if (opt.dataZoom) {
        opt.dataZoom.forEach(function (s) {
          if (s.type === 'inside') {
            s.disabled = disabledStatus
          }
        })
      }
      if (opt.geo) {
        if (opt.geo instanceof Array) {
          opt.geo[0].roam = this.active
        } else {
          opt.geo.roam = this.active
        }
      }
      if (this.chart.type === 'treemap' && opt.series) {
        opt.series[0].roam = this.active
      }
    },

    reDrawView() {
      if (this.linkageActiveParam) {
        this.myChart.dispatchAction({
          type: 'unselect',
          seriesIndex: this.linkageActiveParam.seriesIndex,
          name: this.linkageActiveParam.name
        })
        this.myChart.dispatchAction({
          type: 'downplay',
          seriesIndex: this.linkageActiveParam.seriesIndex,
          name: this.linkageActiveParam.name
        })
      }
      this.linkageActiveParam = null
    },
    linkageActive() {
      if (this.linkageActiveParam) {
        this.myChart.dispatchAction({
          type: 'select',
          seriesIndex: this.linkageActiveParam.seriesIndex,
          name: this.linkageActiveParam.name
        })
        this.myChart.dispatchAction({
          type: 'highlight',
          seriesIndex: this.linkageActiveParam.seriesIndex,
          name: this.linkageActiveParam.name
        })
      }
    },

    preDraw() {
      this.loading = true
      // 基于准备好的dom，初始化echarts实例
      // 渲染echart等待dom加载完毕,渲染之前先尝试销毁具有相同id的echart 放置多次切换仪表板有重复id情况
      const that = this
      new Promise((resolve) => {
        resolve()
      }).then(() => {
        //	此dom为echarts图标展示dom
        this.myChart = this.$echarts.getInstanceByDom(document.getElementById(this.chartId))
        if (!this.myChart) {
          this.myChart = this.$echarts.init(document.getElementById(this.chartId))
        }
        this.drawEcharts()
        this.myChart.on('click', function (param) {
          that.pointParam = param
          if (that.linkageActiveParam) {
            that.reDrawView()
          }
          if (that.trackMenu.length < 2) { // 只有一个事件直接调用
            that.trackClick(that.trackMenu[0])
          } else { // 视图关联多个事件
            that.trackBarStyle.left = param.event.offsetX + 'px'
            that.trackBarStyle.top = (param.event.offsetY - 15) + 'px'
            that.$refs.viewTrack.trackButtonClick()
          }
        })
        this.myChart.off('finished')
        this.myChart.on('finished', () => {
          this.loading = false
        })

        this.currentIndex = 0;
      })
    },
    loadThemeStyle() {
      let themeStyle = null
      if (this.themeStyle) {
        themeStyle = JSON.parse(JSON.stringify(this.themeStyle))

        if (themeStyle && themeStyle.backgroundColorSelect) {
          const panelColor = themeStyle.color
          if (panelColor !== '#FFFFFF') {
            const reverseValue = reverseColor(panelColor)
            this.buttonTextColor = reverseValue
          } else {
            this.buttonTextColor = null
          }
        } else if (this.canvasStyleData.openCommonStyle && this.canvasStyleData.panel.backgroundType === 'color') {
          const panelColor = this.canvasStyleData.panel.color
          if (panelColor !== '#FFFFFF') {
            const reverseValue = reverseColor(panelColor)
            this.buttonTextColor = reverseValue
          } else {
            this.buttonTextColor = null
          }
        } else {
          this.buttonTextColor = null
        }
      }
    },

    drawEcharts() {
      const chart = this.chart
      let chart_option = {}

      const extX = chart.data && chart.data.extXs ? chart.data.extXs[0] : undefined;

      chart_option = this.horizontalBarOption(JSON.parse(JSON.stringify(HORIZONTAL_BAR)), chart, extX);

      this.myEcharts(chart_option)
      this.$nextTick(() => (this.linkageActive()))

      if (this.intervalID) {
        clearInterval(this.intervalID)
      }
      this.intervalID = setInterval(() => {
        this.updateX();
      }, this.sliderTimeout);


    },

    updateX(skipAdd, _index) {
      const chart = this.chart
      const _chart = this.myChart;
      const chart_option = this.myOptions;
      let _currentIndex = this.currentIndex;
      if (chart.data && _chart) {
        if (!skipAdd && this.sliderAuto) {
          if (!(!this.sliderRepeat && _currentIndex === chart.data.extXs.length - 1)) {
            _currentIndex++;
          } else if (this.sliderRepeat && _currentIndex === chart.data.extXs.length - 1) {
            _currentIndex = 0;
          }
        } else if (_index !== undefined) {
          if (_index >= chart.data.extXs.length || _index < 0) {
            _currentIndex = 0;
          } else {
            _currentIndex = _index;
          }
        }
        if (_currentIndex === undefined || _currentIndex >= chart.data.extXs.length || _currentIndex < 0) {
          _currentIndex = 0;
        }
        chart_option.series[0].data = chart.data.groupData[chart.data.extXs[_currentIndex]];

        if (this.graphicShow) {
          chart_option.graphic.elements[0].style.text = chart.data.extXs[_currentIndex];
        } else {
          chart_option.graphic.elements[0].style.text = "";
        }

        chart_option.yAxis.data = chart.data.groupXs[_currentIndex];

        this.currentIndex = _currentIndex;

        _chart.setOption(chart_option);
      }
    },

    horizontalBarOption(chart_option, chart, extX) {
      // 处理shape attr
      let customAttr = {}
      let yaxisList = this.chart.yaxis ? JSON.parse(this.chart.yaxis) : [];

      if (chart.customAttr) {
        customAttr = JSON.parse(chart.customAttr)
        if (customAttr.color) {
          chart_option.color = customAttr.color.colors
        }
        if (customAttr.tooltip) {
          const tooltip = JSON.parse(JSON.stringify(customAttr.tooltip))
          //const reg = new RegExp('\n', 'g')
          //tooltip.formatter = tooltip.formatter.replace(reg, '<br/>')

          tooltip.formatter = function (v, s) {
            return v.marker + v.name + ":&nbsp;&nbsp;&nbsp;" + valueFormatter(v.value[chart.data.encode.x], yaxisList[0].formatterCfg);
          }

          chart_option.tooltip = tooltip

          const bgColor = tooltip.backgroundColor ? tooltip.backgroundColor : DEFAULT_TOOLTIP.backgroundColor
          chart_option.tooltip.backgroundColor = bgColor
          chart_option.tooltip.borderColor = bgColor
        }
      }
      // 处理data
      if (chart.data) {
        chart_option.title.text = chart.title

        chart_option.series[0].encode = chart.data.encode;

        // label
        if (customAttr.label) {
          chart_option.series[0].label = customAttr.label;
          if (chart_option.series[0].label.show) {
            chart_option.series[0].label.position = 'right';
            chart_option.series[0].label.valueAnimation = true;
            chart_option.series[0].label.precision = 1;
            chart_option.series[0].label.formatter = function (v) {
              return valueFormatter(v.value[chart.data.encode.x], yaxisList[0].formatterCfg);
            }
          }
        }

        const colors = {};
        for (let i = 0; i < chart.data.xs.length; i++) {
          colors[chart.data.xs[i] + ''] = hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha);
        }

        chart_option.series[0].itemStyle = {
          color: function (param) {
            return colors[param.value[chart.data.encode.y]]
          }
        }
        chart_option.series[0].selectedMode = true;
        chart_option.series[0].select = BASE_ECHARTS_SELECT;

        // size
        if (customAttr.size) {
          if (customAttr.size.barDefault) {
            chart_option.series[0].barWidth = null
            chart_option.series[0].barGap = null
          } else {
            chart_option.series[0].barWidth = customAttr.size.barWidth
            chart_option.series[0].barGap = customAttr.size.barGap
          }
        }

        chart_option.dataset.source = chart.data.groupData[extX];

        if (this.graphicShow) {
          chart_option.graphic.elements[0].style.text = extX;
          chart_option.graphic.elements[0].style.fill = hexColorToRGBA(DEFAULT_Graphic.color, DEFAULT_Graphic.alpha);
          chart_option.graphic.elements[0].style.font = 'bolder ' + DEFAULT_Graphic.fontSize + 'px monospace';
          chart_option.graphic.elements[0].right = DEFAULT_Graphic.right + 'px';
          chart_option.graphic.elements[0].bottom = DEFAULT_Graphic.bottom + 'px';

          if (customAttr.graphic) {
            chart_option.graphic.elements[0].style.fill = hexColorToRGBA(customAttr.graphic.color, customAttr.graphic.alpha);
            chart_option.graphic.elements[0].style.font = 'bolder ' + customAttr.graphic.fontSize + 'px monospace';

            const unit = _.defaultTo(getMarginUnit(customAttr.graphic), '');

            if (customAttr.graphic.right !== undefined) {
              chart_option.graphic.elements[0].right = customAttr.graphic.right + unit;
            }
            if (customAttr.graphic.bottom !== undefined) {
              chart_option.graphic.elements[0].bottom = customAttr.graphic.bottom + unit;
            }
          }
        } else {
          chart_option.graphic.elements[0].style.text = "";
        }
      }

      chart_option.yAxis.max = this.sliderMax;
      //chart_option.yAxis.data = chart.data.xs;

      chart_option.animationDurationUpdate = this.sliderTimeout;

      componentStyle(chart_option, chart);
      seniorCfg(chart_option, chart);

      if (this.myChart && this.searchCount > 0) {
        chart_option.animation = false
      }
      if (chart_option.legend) {
        if (this.canvasStyleData.panel.themeColor === 'dark') {
          chart_option.legend['pageIconColor'] = '#ffffff'
          chart_option.legend['pageIconInactiveColor'] = '#8c8c8c'
        } else {
          chart_option.legend['pageIconColor'] = '#000000'
          chart_option.legend['pageIconInactiveColor'] = '#8c8c8c'
        }
      }
      if (chart_option.tooltip && this.inScreen) {
        chart_option.tooltip.appendToBody = true
      }

      return chart_option;
    },

    myEcharts(option) {
      this.adaptorOpt(option)
      // 指定图表的配置项和数据
      const chart = this.myChart
      this.setBackGroundBorder()

      this.myOptions = option;

      setTimeout(chart.setOption(option, true), 500)
      window.removeEventListener('resize', chart.resize)
    },

    setBackGroundBorder() {
      if (this.chart.customStyle) {
        const customStyle = JSON.parse(this.chart.customStyle)
        if (customStyle.background) {
          this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'
        }
      }
    },

    chartResize() {
      // 指定图表的配置项和数据
      const chart = this.myChart
      chart.resize()
    },


    linkageActivePre() {
      if (this.linkageActiveHistory) {
        this.reDrawView()
      }
      this.$nextTick(() => {
        this.linkageActive()
      })
    },


    trackClick(trackAction) {
      const param = this.pointParam
      if (!param || !param.data || !param.data.dimensionList) {
        // 地图提示没有关联字段 其他没有维度信息的 直接返回
        if (this.chart.type === 'map') {
          this.$warning(this.$t('panel.no_drill_field'))
        }
        return
      }
      const quotaList = this.pointParam.data.quotaList
      quotaList[0]['value'] = this.pointParam.data.value
      const linkageParam = {
        option: 'linkage',
        name: this.pointParam.data.name,
        viewId: this.chart.id,
        dimensionList: this.pointParam.data.dimensionList,
        quotaList: quotaList
      }
      const jumpParam = {
        option: 'jump',
        name: this.pointParam.data.name,
        viewId: this.chart.id,
        dimensionList: this.pointParam.data.dimensionList,
        quotaList: quotaList
      }
      switch (trackAction) {
        case 'drill':
          this.$emit('onChartClick', this.pointParam)
          break
        case 'linkage':
          this.linkageActivePre()
          this.$store.commit('addViewTrackFilter', linkageParam)
          break
        case 'jump':
          this.$emit('onJumpClick', jumpParam)
          break
        default:
          break
      }
    },


  }
}
</script>

<style scoped lang="scss">

.track-bar > > > ul {
  width: 80px !important;
}
</style>
