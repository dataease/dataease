<template>
  <div ref="chartContainer"
       style="padding: 0; width: 100%; height: 100%; overflow: hidden; display: flex; flex-direction: column;"
       :style="bg_class">
    <view-track-bar ref="viewTrack" :track-menu="trackMenu" class="track-bar" :style="trackBarStyleTime"
                    @trackClick="trackClick"/>

    <span
      v-if="chart.type && antVRenderStatus"
      v-show="title_show"
      ref="title"
      :style="titleClass"
      style="cursor: default;display: block;"
    >
          <div style="padding:4px 4px 0;margin: 0;">
            <chart-title-update
              :title-class="titleClass"
              :chart-info="chartInfo"
              :bus="bus"
              :axios-request="axiosRequest"
            />
            <title-remark
              v-if="remarkCfg.show"
              style="text-shadow: none!important;margin-left: 4px;"
              :remark-cfg="remarkCfg"
            />
          </div>
        </span>
    <div :id="chartId" style="width: 100%; overflow: hidden; flex: 1"/>

  </div>
</template>

<script>
import {uuid, hexColorToRGBA, setGradientColor} from '../../../utils/chartmix'
import ViewTrackBar from '../../../components/views/ViewTrackBar'
import {getRemark} from "../../../components/views/utils";
import {
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_STYLE,
  transAxisPosition,
  getLineDash, DEFAULT_COLOR_CASE, formatterItem, DEFAULT_YAXIS_EXT_STYLE
} from '../../../utils/map';
import ChartTitleUpdate from '../../../components/views/ChartTitleUpdate';
import {map, filter, join, flatten, cloneDeep} from 'lodash-es';
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
      linkageActiveParam: null,
      linkageActiveHistory: false,
      CHART_CONT_FAMILY_MAP: {
        'Microsoft YaHei': 'Microsoft YaHei',
        'SimSun': 'SimSun, "Songti SC", STSong',
        'SimHei': 'SimHei, Helvetica',
        'KaiTi': 'KaiTi, "Kaiti SC", STKaiti'
      }
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
    }
  },
  watch: {
    chart: {
      handler(newVal, oldVla) {
        new Promise((resolve) => {
          resolve()
        }).then(() => {
          this.updateViewData()
          this.initTitle();
        })
      },
      deep: true
    }
  },
  created() {
    !this.$chartmix && (this.$chartmix = this.$Mix)
  },
  mounted() {
    this.preDraw()
  },
  destroyed() {
    this.myChart.destroy()
  },
  methods: {
    preDraw() {
      const _params = this.getParam();

      this.myChart = new this.$chartmix(this.chartId, _params)

      this.myChart.off('point:click')
      this.myChart.on('point:click', this.antVAction)
      this.myChart.off('interval:click')
      this.myChart.on('interval:click', this.antVAction)

      this.myChart.render();

      this.initTitle();

      this.myChart.update(_params);

    },

    antVAction(param) {
      switch (this.chart.type) {
        case 'treemap':
          this.pointParam = param.data.data
          break
        case 'word-cloud':
          this.pointParam = {
            data: param.data.data.datum
          }
          break
        default:
          this.pointParam = param.data
          break
      }
      this.linkageActiveParam = {
        category: this.pointParam.data.category ? this.pointParam.data.category : 'NO_DATA',
        name: this.pointParam.data.name ? this.pointParam.data.name : 'NO_DATA'
      }
      if (this.trackMenu.length < 2) { // 只有一个事件直接调用
        this.trackClick(this.trackMenu[0])
      } else { // 视图关联多个事件
        this.trackBarStyle.left = param.x + 'px'
        this.trackBarStyle.top = (param.y + 10) + 'px'
        this.$refs.viewTrack.trackButtonClick()
      }
    },

    initTitle() {
      this.antVRenderStatus = true;

      if (this.chart.customStyle) {
        const customStyle = JSON.parse(this.chart.customStyle)

        if (customStyle.text) {
          this.title_show = customStyle.text.show
          this.titleClass.fontSize = customStyle.text.fontSize + 'px'
          this.titleClass.color = customStyle.text.color
          this.titleClass.textAlign = customStyle.text.hPosition
          this.titleClass.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
          this.titleClass.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'

          this.titleClass.fontFamily = customStyle.text.fontFamily ? this.CHART_CONT_FAMILY_MAP[customStyle.text.fontFamily] : 'Microsoft YaHei'
          this.titleClass.letterSpacing = (customStyle.text.letterSpace ? customStyle.text.letterSpace : '0') + 'px'
          this.titleClass.textShadow = customStyle.text.fontShadow ? '2px 2px 4px' : 'none'
        }
        if (customStyle.background) {
          this.titleClass.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
          this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'
        }

      }
      this.initRemark()
    },
    initRemark() {
      this.remarkCfg = getRemark(this.chart)
    },

    getChartType(type) {
      if (type === 'line') {
        return 'line';
      } else if (type === 'scatter') {
        return 'scatter';
      } else {
        return 'column';
      }
    },

    getParam() {
      let yaxisList = this.chart.yaxis ? JSON.parse(this.chart.yaxis) : [];
      let yaxisExtList = this.chart.yaxisExt ? JSON.parse(this.chart.yaxisExt) : [];

      let yaxisCount = yaxisList.length;

      let customAttr = undefined;
      let gradient = false;
      let colors = undefined;
      let alpha = DEFAULT_COLOR_CASE.alpha;
      let labelSetting = undefined;
      let labelPosition = 'middle';
      if (this.chart.customAttr) {
        customAttr = JSON.parse(this.chart.customAttr);
        if (customAttr) {
          if (customAttr.color) {
            colors = customAttr.color.colors;
            alpha = customAttr.color.alpha;
            gradient = customAttr.color.gradient;
          }
          if (customAttr.label) {
            labelSetting = customAttr.label.show ? {
              style: {
                fill: customAttr.label.color,
                fontSize: parseInt(customAttr.label.fontSize),
              },
            } : false
            labelPosition = customAttr.label.position;
          }
        }
      }

      const xAxis = this.getXAxis(this.chart);

      const yAxis = this.getYAxis(this.chart);

      const yAxisExt = this.getYAxisExt(this.chart);

      const names = [];

      const yChartData = this.chart.data && this.chart.data.data && this.chart.data.data.length > 0 ? map(filter(this.chart.data.data, (c, _index) => {
          return _index < yaxisCount;
        }), (t, _index) => {
          names.push(t.name);

          return map(t.data, (v) => {
            return {
              quotaList: v.quotaList,
              dimensionList: v.dimensionList,
              key: join(map(v.dimensionList, (d) => d.value), "\n"),
              value: v.value,
              name: t.name,
              i: _index,
              t: 'yaxis'
            }
          });
        }
      ) : [];

      const yData = [this.getYData(flatten(yChartData), labelSetting, labelPosition, yaxisList, colors, gradient, alpha, xAxis, yAxis, yaxisExtList.length)];

      const yExtChartData = this.chart.data && this.chart.data.data && this.chart.data.data.length > 0 ? map(filter(this.chart.data.data, (c, _index) => {
          return _index >= yaxisCount;
        }), (t, _index) => {
          names.push(t.name);

          return map(t.data, (v) => {
            return {
              quotaList: v.quotaList,
              dimensionList: v.dimensionList,
              key: join(map(v.dimensionList, (d) => d.value), "\n"),
              value: v.value,
              name: t.name,
              i: _index,
              t: 'yaxisExt'
            }
          })
        }
      ) : [];

      const yExtData = [this.getYExtData(flatten(yExtChartData), labelSetting, labelPosition, yaxisExtList, colors, gradient, alpha, xAxis, yAxisExt, yaxisCount)];

      const params = {
        tooltip: false,
        syncViewPadding: true,
        plots: [
          ...yData,
          ...yExtData
        ]
      };


      if (customAttr) {

        if (customAttr.tooltip) {
          params.tooltip = customAttr.tooltip.show ? {
            showTitle: false,
            showMarkers: false,
            shared: true,
            // 内置：node 不显示 tooltip，edge 显示 tooltip
            showContent: function (items) {
              //return !Object(_antv_util__WEBPACK_IMPORTED_MODULE_1__["get"])(items, [0, 'data', 'isNode']);
              return customAttr.tooltip.show && items.length > 0 && items[0].value !== undefined;
            },
            domStyles: {
              'g2-tooltip': {
                fontSize: customAttr.tooltip.textStyle.fontSize + 'px',
                color: customAttr.tooltip.textStyle.color,
                backgroundColor: customAttr.tooltip.backgroundColor,
              }
            },
            customItems: (originalItems) => {
              // process originalItems,
              originalItems.forEach(item => {
                if (item.data.t === 'yaxis' && item.data.i !== undefined && item.data.i < yaxisList.length && yaxisList[item.data.i].formatterCfg) {
                  item.value = valueFormatter(item.data.value, yaxisList[item.data.i].formatterCfg)
                } else if (item.data.t === 'yaxisExt' && item.data.i !== undefined && item.data.i < yaxisExtList.length && yaxisExtList[item.data.i].formatterCfg) {
                  item.value = valueFormatter(item.data.value, yaxisExtList[item.data.i].formatterCfg)
                }
              })

              // 由于只会触发一个scatter，所以针对scatter的进行一次过滤，只保留一个scatter的值
              let hasScatter = false;

              const list = filter(originalItems, (item) => {
                if (item.data.chartType === 'scatter') {
                  if (!hasScatter) {
                    hasScatter = true;
                    item.name = item.data.name;
                  } else {
                    return false;
                  }
                }

                return true;
              })
              return list;

            }
          } : false;
        }

      }

      params.slider = this.getSlider(this.chart);

      params.annotations = this.getAnalyse(this.chart);

      //两个轴只能展示一个轴的图例，所以隐藏
      //params.legend = this.getLegend(this.chart);

      return params;
    },

    getXAxis(chart) {
      let axis = {}
      let customStyle
      if (chart.customStyle) {
        customStyle = JSON.parse(chart.customStyle)
        // legend
        if (customStyle.xAxis) {
          const a = JSON.parse(JSON.stringify(customStyle.xAxis))
          if (a.show) {
            const title = (a.name && a.name !== '') ? {
              text: a.name,
              style: {
                fill: a.nameTextStyle.color,
                fontSize: parseInt(a.nameTextStyle.fontSize)
              },
              spacing: 8
            } : null
            const gridCfg = a.splitLine ? a.splitLine : DEFAULT_XAXIS_STYLE.splitLine
            if (!gridCfg.dashStyle) {
              gridCfg.dashStyle = DEFAULT_XAXIS_STYLE.splitLine.dashStyle
            }
            const grid = a.splitLine.show ? {
              line: {
                style: {
                  stroke: a.splitLine.lineStyle.color,
                  lineWidth: parseInt(a.splitLine.lineStyle.width),
                  lineDash: gridCfg.enableDash ? [gridCfg.dashStyle.width, gridCfg.dashStyle.offset] : undefined
                }
              }
            } : null
            const axisCfg = a.axisLine ? a.axisLine : DEFAULT_XAXIS_STYLE.axisLine
            const axisLine = axisCfg.show ? {
              style: {
                stroke: axisCfg.lineStyle.color,
                lineWidth: parseInt(axisCfg.lineStyle.width)
              }
            } : null
            const tickLine = axisCfg.show ? {
              style: {
                stroke: axisCfg.lineStyle.color
              }
            } : null
            const rotate = parseInt(a.axisLabel.rotate)
            const label = a.axisLabel.show ? {
              rotate: rotate * Math.PI / 180,
              style: {
                textAlign: rotate > 20 ? 'start' : rotate < -20 ? 'end' : 'center',
                fill: a.axisLabel.color,
                fontSize: parseInt(a.axisLabel.fontSize)
              },
              formatter: function (value) {
                if (chart.type.includes('horizontal')) {
                  if (!a.axisLabelFormatter) {
                    return valueFormatter(value, formatterItem)
                  } else {
                    return valueFormatter(value, a.axisLabelFormatter)
                  }
                } else {
                  return value
                }
              }
            } : null

            axis = {
              position: transAxisPosition(chart, a),
              title: title,
              grid: grid,
              label: label,
              line: axisLine,
              tickLine: tickLine
            }

            // 轴值设置
            delete axis.minLimit
            delete axis.maxLimit
            delete axis.tickCount
          } else {
            axis = false
          }
        }
      }
      return axis
    },

    getYAxis(chart) {
      let axis = {}
      let customStyle
      if (chart.customStyle) {
        customStyle = JSON.parse(chart.customStyle)
        // legend
        if (customStyle.yAxis) {
          const a = JSON.parse(JSON.stringify(customStyle.yAxis))
          if (a.show) {
            const title = (a.name && a.name !== '') ? {
              text: a.name,
              style: {
                fill: a.nameTextStyle.color,
                fontSize: parseInt(a.nameTextStyle.fontSize)
              },
              spacing: 8
            } : null
            const gridCfg = a.splitLine ? a.splitLine : DEFAULT_YAXIS_STYLE.splitLine
            if (!gridCfg.dashStyle) {
              gridCfg.dashStyle = DEFAULT_YAXIS_STYLE.splitLine.dashStyle
            }
            const grid = a.splitLine.show ? {
              line: {
                style: {
                  stroke: a.splitLine.lineStyle.color,
                  lineWidth: parseInt(a.splitLine.lineStyle.width),
                  lineDash: gridCfg.enableDash ? [gridCfg.dashStyle.width, gridCfg.dashStyle.offset] : undefined
                }
              }
            } : null
            const axisCfg = a.axisLine ? a.axisLine : DEFAULT_YAXIS_STYLE.axisLine
            const axisLine = axisCfg.show ? {
              style: {
                stroke: axisCfg.lineStyle.color,
                lineWidth: parseInt(axisCfg.lineStyle.width)
              }
            } : null
            const tickLine = axisCfg.show ? {
              style: {
                stroke: axisCfg.lineStyle.color
              }
            } : null
            const label = a.axisLabel.show ? {
              rotate: parseInt(a.axisLabel.rotate) * Math.PI / 180,
              style: {
                fill: a.axisLabel.color,
                fontSize: parseInt(a.axisLabel.fontSize)
              },
              formatter: function (value) {
                if (chart.type === 'waterfall') {
                  return value
                } else {
                  if (!chart.type.includes('horizontal')) {
                    if (!a.axisLabelFormatter) {
                      return valueFormatter(value, formatterItem)
                    } else {
                      return valueFormatter(value, a.axisLabelFormatter)
                    }
                  } else {
                    return value
                  }
                }
              }
            } : null

            axis = {
              position: 'left',
              title: title,
              grid: grid,
              label: label,
              line: axisLine,
              tickLine: tickLine
            }

            // 轴值设置
            delete axis.minLimit
            delete axis.maxLimit
            delete axis.tickCount
            const axisValue = a.axisValue

            if (axisValue && !axisValue.auto) {
              axisValue.min && (axis.minLimit = parseFloat(axisValue.min))
              axisValue.max && (axis.maxLimit = parseFloat(axisValue.max))
              axisValue.splitCount && (axis.tickCount = parseFloat(axisValue.splitCount))
            }

          } else {
            axis = false
          }
        }
      } else {
        return {position: 'left'}
      }
      return axis
    },
// yAxisExt
    getYAxisExt(chart) {
      let axis = {}
      let customStyle
      if (chart.customStyle) {
        customStyle = JSON.parse(chart.customStyle)
        // legend
        if (customStyle.yAxisExt) {
          const a = JSON.parse(JSON.stringify(customStyle.yAxisExt))
          if (a.show) {
            const title = (a.name && a.name !== '') ? {
              text: a.name,
              style: {
                fill: a.nameTextStyle.color,
                fontSize: parseInt(a.nameTextStyle.fontSize)
              },
              spacing: 8
            } : null
            const gridCfg = a.splitLine ? a.splitLine : DEFAULT_YAXIS_EXT_STYLE.splitLine
            if (!gridCfg.dashStyle) {
              gridCfg.dashStyle = DEFAULT_YAXIS_EXT_STYLE.splitLine.dashStyle
            }
            const grid = a.splitLine.show ? {
              line: {
                style: {
                  stroke: a.splitLine.lineStyle.color,
                  lineWidth: parseInt(a.splitLine.lineStyle.width),
                  lineDash: gridCfg.enableDash ? [gridCfg.dashStyle.width, gridCfg.dashStyle.offset] : undefined
                }
              }
            } : null
            const axisCfg = a.axisLine ? a.axisLine : DEFAULT_YAXIS_EXT_STYLE.axisLine
            const axisLine = axisCfg.show ? {
              style: {
                stroke: axisCfg.lineStyle.color,
                lineWidth: parseInt(axisCfg.lineStyle.width)
              }
            } : null
            const tickLine = axisCfg.show ? {
              style: {
                stroke: axisCfg.lineStyle.color
              }
            } : null
            const label = a.axisLabel.show ? {
              rotate: parseInt(a.axisLabel.rotate) * Math.PI / 180,
              style: {
                fill: a.axisLabel.color,
                fontSize: parseInt(a.axisLabel.fontSize)
              },
              formatter: function (value) {
                if (chart.type === 'waterfall') {
                  return value
                } else {
                  if (!chart.type.includes('horizontal')) {
                    if (!a.axisLabelFormatter) {
                      return valueFormatter(value, formatterItem)
                    } else {
                      return valueFormatter(value, a.axisLabelFormatter)
                    }
                  } else {
                    return value
                  }
                }
              }
            } : null

            axis = {
              position: 'right',
              title: title,
              grid: grid,
              label: label,
              line: axisLine,
              tickLine: tickLine
            }

            // 轴值设置
            delete axis.minLimit
            delete axis.maxLimit
            delete axis.tickCount
            const axisValue = a.axisValue

            if (axisValue && !axisValue.auto) {
              axisValue.min && (axis.minLimit = parseFloat(axisValue.min))
              axisValue.max && (axis.maxLimit = parseFloat(axisValue.max))
              axisValue.splitCount && (axis.tickCount = parseFloat(axisValue.splitCount))
            }

          } else {
            axis = false
          }
        }
      } else {
        return {position: 'right'}
      }
      return axis
    },

    getYData(data, labelSetting, labelPosition, yaxisList, colors, gradient, alpha, xAxis, yAxis, yaxisExtCount) {

      const _labelSetting = cloneDeep(labelSetting);
      if (_labelSetting) {
        _labelSetting.formatter = function (x) {
          for (let i = 0; i < yaxisList.length; i++) {
            if (i === x.i && yaxisList[i].formatterCfg) {
              return valueFormatter(x.value, yaxisList[i].formatterCfg);
            }
          }
          return x.value;
        }
      }

      const _chartType = this.getChartType(yaxisList && yaxisList.length > 0 ? yaxisList[0].chartType : undefined);

      if (_labelSetting) {
        if (_chartType === "column") {
          _labelSetting.position = labelPosition;
        } else {
          _labelSetting.position = undefined;
        }
      }

      const color = [];
      for (let i = 0; i < yaxisList.length; i++) {
        if (gradient) {
          color.push(setGradientColor(hexColorToRGBA(colors[i % colors.length], alpha), true, 270))
        } else {
          color.push(hexColorToRGBA(colors[i % colors.length], alpha))
        }
      }

      const setting = {
        type: _chartType,
        options: {
          data: map(data, (d) => {
            d.chartType = _chartType
            return d
          }),
          xField: 'key',
          yField: 'value',
          seriesField: 'name',
          colorField: 'name',
          isGroup: _chartType === "column" ? true : undefined,
          meta: {
            key: {
              sync: true,
            },
          },
          color: color,
          label: _labelSetting,
          xAxis: yaxisList.length > 0 || yaxisExtCount === 0 ? xAxis : false,
          yAxis: yAxis,
        }
      }
      return this.setSizeSetting(setting);
    },

    getYExtData(data, labelSetting, labelPosition, yaxisExtList, colors, gradient, alpha, xAxis, yAxisExt, yaxisCount) {
      const _labelSetting = cloneDeep(labelSetting);
      if (_labelSetting) {
        _labelSetting.formatter = function (x) {
          for (let i = 0; i < yaxisExtList.length; i++) {
            if (i === x.i && yaxisExtList[i].formatterCfg) {
              return valueFormatter(x.value, yaxisExtList[i].formatterCfg);
            }
          }
          return x.value;
        }
      }

      const _chartType = this.getChartType(yaxisExtList && yaxisExtList.length > 0 ? yaxisExtList[0].chartType : undefined);

      if (_labelSetting) {
        if (_chartType === "column") {
          _labelSetting.position = labelPosition;
        } else {
          _labelSetting.position = undefined;
        }
      }

      const color = [];
      for (let i = yaxisCount; i < yaxisExtList.length + yaxisCount; i++) {
        if (gradient) {
          color.push(setGradientColor(hexColorToRGBA(colors[i % colors.length], alpha), true, 270))
        } else {
          color.push(hexColorToRGBA(colors[i % colors.length], alpha))
        }
      }

      const setting = {
        type: _chartType,
        options: {
          data: map(data, (d) => {
            d.chartType = _chartType
            return d
          }),
          xField: 'key',
          yField: 'value',
          seriesField: 'name',
          colorField: 'name',
          isGroup: _chartType === "column" ? true : undefined,
          meta: {
            key: {
              sync: true,
            },
          },
          color: color,
          label: _labelSetting,
          xAxis: yaxisCount > 0 || yaxisExtList.length === 0 ? false : xAxis,
          yAxis: yAxisExt,
        }
      }
      return this.setSizeSetting(setting);
    },

    setSizeSetting(setting) {
      let customAttr = undefined;
      if (this.chart.customAttr) {
        customAttr = JSON.parse(this.chart.customAttr);
      }
      if (customAttr && customAttr.size) {
        setting.options.columnWidthRatio = undefined;
        setting.options.smooth = undefined;
        setting.options.point = undefined;
        setting.options.lineStyle = undefined;
        setting.options.size = undefined;
        setting.options.shape = undefined;

        if (setting.type === 'column' && !customAttr.size.barDefault) {
          setting.options.columnWidthRatio = customAttr.size.barWidthPercent / 100.0
        }
        if (setting.type === 'line') {
          setting.options.smooth = customAttr.size.lineSmooth
          setting.options.point = {
            size: parseInt(customAttr.size.lineSymbolSize),
            shape: customAttr.size.lineSymbol
          }
          setting.options.lineStyle = {
            lineWidth: parseInt(customAttr.size.lineWidth)
          }
        }
        if (setting.type === 'scatter') {
          setting.options.size = parseInt(customAttr.size.scatterSymbolSize)
          setting.options.shape = customAttr.size.scatterSymbol
        }
      }

      return setting;
    },

    getAnalyse(chart) {
      let senior = {}
      const assistLine = []
      if (chart.senior && chart.type && (chart.type.includes('bar') || chart.type.includes('line') || chart.type.includes('mix') || chart.type.includes('area'))) {
        senior = JSON.parse(chart.senior)
        if (senior.assistLine && senior.assistLine.length > 0) {
          const customStyle = JSON.parse(chart.customStyle)
          let xAxisPosition, yAxisPosition, axisFormatterCfg
          if (customStyle.xAxis) {
            const a = JSON.parse(JSON.stringify(customStyle.xAxis))
            xAxisPosition = transAxisPosition(chart, a)
            if (chart.type.includes('horizontal')) {
              axisFormatterCfg = a.axisLabelFormatter ? a.axisLabelFormatter : DEFAULT_XAXIS_STYLE.axisLabelFormatter
            }
          }
          if (customStyle.yAxis) {
            const a = JSON.parse(JSON.stringify(customStyle.yAxis))
            yAxisPosition = transAxisPosition(chart, a)
            if (!chart.type.includes('horizontal')) {
              axisFormatterCfg = a.axisLabelFormatter ? a.axisLabelFormatter : DEFAULT_YAXIS_STYLE.axisLabelFormatter
            }
          }

          const fixedLines = senior.assistLine.filter(ele => ele.field === '0')
          const dynamicLines = chart.data.dynamicAssistLines
          const lines = fixedLines.concat(dynamicLines)

          lines.forEach(ele => {
            const value = parseFloat(ele.value)
            const content = ele.name + ' : ' + valueFormatter(value, axisFormatterCfg)
            assistLine.push({
              type: 'line',
              start: ['start', value],
              end: ['end', value],
              style: {
                stroke: ele.color,
                lineDash: getLineDash(ele.lineType)
              }
            })
            if (!chart.type.includes('horizontal')) {
              assistLine.push({
                type: 'text',
                position: [yAxisPosition === 'left' ? 'start' : 'end', value],
                content: content,
                offsetY: -2,
                offsetX: yAxisPosition === 'left' ? 2 : -10 * (content.length - 2),
                style: {
                  textBaseline: 'bottom',
                  fill: ele.color,
                  fontSize: ele.fontSize ? parseInt(ele.fontSize) : 10
                }
              })
            } else {
              assistLine.push({
                type: 'text',
                position: [xAxisPosition === 'left' ? 'start' : 'end', value],
                content: content,
                offsetY: xAxisPosition === 'left' ? -2 : -10 * (content.length - 2),
                offsetX: 2,
                rotate: Math.PI / 2,
                style: {
                  textBaseline: 'bottom',
                  fill: ele.color,
                  fontSize: ele.fontSize ? parseInt(ele.fontSize) : 10
                }
              })
            }
          })
        }
      }
      return assistLine
    },

    getLegend(chart) {
      let legend = {}
      let customStyle
      if (chart.customStyle) {
        customStyle = JSON.parse(chart.customStyle)
        // legend
        if (customStyle.legend) {
          const l = JSON.parse(JSON.stringify(customStyle.legend))
          if (l.show) {
            let offsetX, offsetY, position
            const orient = l.orient
            const legendSymbol = l.icon
            // fix position
            if (l.hPosition === 'center') {
              position = l.vPosition === 'center' ? 'top' : l.vPosition
            } else if (l.vPosition === 'center') {
              position = l.hPosition === 'center' ? 'left' : l.hPosition
            } else {
              if (orient === 'horizontal') {
                position = l.vPosition + '-' + l.hPosition
              } else {
                position = l.hPosition + '-' + l.vPosition
              }
            }
            // fix offset
            if (orient === 'horizontal') {
              if (l.hPosition === 'left') {
                offsetX = 16
              } else if (l.hPosition === 'right') {
                offsetX = -16
              } else {
                offsetX = 0
              }
              if (l.vPosition === 'top') {
                offsetY = 0
              } else if (l.vPosition === 'bottom') {
                if (chart.drill) {
                  offsetY = -16
                } else {
                  offsetY = -4
                }
              } else {
                offsetY = 0
              }
            } else {
              if (l.hPosition === 'left') {
                offsetX = 10
              } else if (l.hPosition === 'right') {
                offsetX = -10
              } else {
                offsetX = 0
              }
              if (l.vPosition === 'top') {
                offsetY = 0
              } else if (l.vPosition === 'bottom') {
                if (chart.drill) {
                  offsetY = -22
                } else {
                  offsetY = -10
                }
              } else {
                offsetY = 0
              }
            }

            legend = {
              layout: orient,
              position: position,
              offsetX: offsetX,
              offsetY: offsetY,
              marker: {
                symbol: legendSymbol
              },
              radio: false, // 柱状图图例的聚焦功能，默认先关掉
              itemName: {
                formatter: (text, item, index) => {
                  if (chart.type !== 'bidirectional-bar') {
                    return text
                  }
                  const yaxis = JSON.parse(chart.yaxis)[0]
                  const yaxisExt = JSON.parse(chart.yaxisExt)[0]
                  if (index === 0) {
                    return yaxis.name
                  }
                  return yaxisExt.name
                }
              }
            }
          } else {
            legend = false
          }
        }
      }
      return legend
    },

    getSlider(chart) {
      let senior = {}
      let cfg = false
      if (chart.senior && chart.type) {
        senior = JSON.parse(chart.senior)
        if (senior.functionCfg) {
          if (senior.functionCfg.sliderShow) {
            cfg = {
              start: parseInt(senior.functionCfg.sliderRange[0]) / 100,
              end: parseInt(senior.functionCfg.sliderRange[1]) / 100
            }

            if (senior.functionCfg.sliderBg) {
              cfg.backgroundStyle = {
                fill: senior.functionCfg.sliderBg,
                stroke: senior.functionCfg.sliderBg,
                lineWidth: 1,
                strokeOpacity: 0.5
              }
            }
            if (senior.functionCfg.sliderFillBg) {
              cfg.foregroundStyle = {
                'fill': senior.functionCfg.sliderFillBg,
                'fillOpacity': 0.5
              }
            }
            if (senior.functionCfg.sliderTextClolor) {
              cfg.textStyle = {
                'fill': senior.functionCfg.sliderTextClolor
              }
              cfg.handlerStyle = {
                'fill': senior.functionCfg.sliderTextClolor,
                'fillOpacity': 0.5,
                'highLightFill': senior.functionCfg.sliderTextClolor
              }
            }
          }
        }
      }
      return cfg;
    },

    updateViewData() {

      const param = this.getParam();

      this.myChart.update(param);

    },

    reDrawView() {
      this.linkageActiveHistory = false
      this.myChart.render()
    },
    linkageActivePre() {
      if (this.linkageActiveHistory) {
        this.reDrawView()
      }
      this.$nextTick(() => {
        this.linkageActive()
      })
    },
    linkageActive() {
      this.linkageActiveHistory = true
      this.myChart.setState('active', (param) => {
        if (Array.isArray(param)) {
          return false
        } else {
          if (this.checkSelected(param)) {
            return true
          }
        }
      })
      this.myChart.setState('inactive', (param) => {
        if (Array.isArray(param)) {
          return false
        } else {
          if (!this.checkSelected(param)) {
            return true
          }
        }
      })
    },
    checkSelected(param) {
      return (this.linkageActiveParam.name === param.name || (this.linkageActiveParam.name === 'NO_DATA' && !param.name)) &&
        (this.linkageActiveParam.category === param.category)
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

    beforeDestroy() {
      if (this.myChart.container) {
        if (typeof this.myChart.container.getAttribute === 'function') {
          this.$G2SizeSensorClear(this.myChart.container)
        }
      }
      if (this.myChart) {
        if (this.myChart.clear) {
          this.myChart.clear()
        }
        if (this.myChart.unbindSizeSensor) {
          this.myChart.unbindSizeSensor()
        }
        if (this.myChart.unbind) {
          this.myChart.unbind()
        }
        if (this.myChart.destroy) {
          this.myChart.destroy()
        }
      }
      if (this.myChart) {
        for (const key in this.myChart.chart) {
          this.myChart.chart[key] = null
          this.$delete(this.myChart.chart, key)
        }
        for (const key in this.myChart) {
          this.myChart[key] = null
          this.$delete(this.myChart, key)
        }
      }
      for (const key in this.pointParam) {
        this.$delete(this.pointParam, key)
      }
      this.myChart = null
    },


  }
}
</script>

<style scoped lang="scss">

.track-bar > > > ul {
  width: 80px !important;
}
</style>
