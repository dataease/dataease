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
      :style="title_class"
      style="cursor: default;display: block;"
    >
          <div style="padding:4px 4px 0;margin: 0;">
            <chart-title-update
              :title-class="title_class"
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
import {Sankey} from '@antv/g2plot'
import {uuid, hexColorToRGBA} from '@/utils/sankey'
import ViewTrackBar from '@/components/views/ViewTrackBar'
import {getRemark} from "@/components/views/utils";
import {DEFAULT_TITLE_STYLE} from '@/utils/map';
import ChartTitleUpdate from '@/components/views/ChartTitleUpdate';
import _ from 'lodash';
import {clear} from 'size-sensor'

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
    !this.$sankey && (this.$sankey = Sankey)
  },
  mounted() {
    this.preDraw()
  },
  destroyed() {
    this.myChart.destroy()
  },
  methods: {
    preDraw() {
      this.myChart = new this.$sankey(this.chartId, this.getParam())

      this.myChart.off('edge:click')
      this.myChart.on('edge:click', this.antVAction)

      this.myChart.render();

      this.initTitle();

      const that = this;


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
          this.titleClass.fontSize = customStyle.text.isBolder ? 'bold' : 'normal'

          this.titleClass.fontFamily = customStyle.text.fontFamily ? customStyle.text.fontFamily : 'Microsoft YaHei'
          this.titleClass.letterSpacing = (customStyle.text.letterSpace ? customStyle.text.letterSpace : '0') + 'px'
          this.titleClass.textShadow = customStyle.text.fontShadow ? '2px 2px 4px' : 'none'
        }
        if (customStyle.background) {
          this.titleClass.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
          this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'
        }

        if (customStyle.text) {
          this.title_show = customStyle.text.show
          this.title_class.fontSize = customStyle.text.fontSize + 'px'
          this.title_class.color = customStyle.text.color
          this.title_class.textAlign = customStyle.text.hPosition
          this.title_class.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
          this.title_class.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'

          this.title_class.fontFamily = customStyle.text.fontFamily ? customStyle.text.fontFamily : DEFAULT_TITLE_STYLE.fontFamily
          this.title_class.letterSpacing = (customStyle.text.letterSpace ? customStyle.text.letterSpace : DEFAULT_TITLE_STYLE.letterSpace) + 'px'
          this.title_class.textShadow = customStyle.text.fontShadow ? '2px 2px 4px' : 'none'
        }
        if (customStyle.background) {
          this.title_class.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
          this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'
        }

      }
      this.initRemark()
    },
    initRemark() {
      this.remarkCfg = getRemark(this.chart)
    },

    getParam() {
      let _data = this.chart.data && this.chart.data.data && this.chart.data.data.length > 0 ? _.map(this.chart.data.tableRow, (t, _index) => {
        const obj = t;
        obj.dimensionList = this.chart.data.data[0].data[_index].dimensionList;
        obj.quotaList = this.chart.data.data[0].data[_index].quotaList;
        return obj;
      }) : undefined;

      let _source = null, _target = null, _value = null;

      if (_data === undefined || _data === null || this.chart.data.fields.length < 3) {
        _data = [];
      } else {
        _source = this.chart.data.fields[0].dataeaseName
        _target = this.chart.data.fields[1].dataeaseName
        _value = this.chart.data.fields[2].dataeaseName
      }

      const params = {
        data: _data,
        sourceField: _source,
        targetField: _target,
        weightField: _value,
        rawFields: ['dimensionList', 'quotaList'],
      };

      let xaxisList = this.chart.xaxis ? JSON.parse(this.chart.xaxis) : [];
      let xNodeSort = undefined;
      if (xaxisList[0] && xaxisList[0].sort && (xaxisList[0].sort === 'asc' || xaxisList[0].sort === 'desc')) {
        xNodeSort = function (a, b) {
          return xaxisList[0].sort === 'asc' ? a.name.localeCompare(b.name) : b.name.localeCompare(a.name);
        }
      }
      let yaxisList = this.chart.yaxis ? JSON.parse(this.chart.yaxis) : [];
      let yNodeSort = undefined;
      if (yaxisList[0] && yaxisList[0].sort && (yaxisList[0].sort === 'asc' || yaxisList[0].sort === 'desc')) {
        yNodeSort = function (a, b) {
          return yaxisList[0].sort === 'asc' ? a.value - b.value : b.value - a.value;
        }
      }
      if (yNodeSort) {
        params.nodeSort = yNodeSort;
      } else if (yNodeSort === undefined && xNodeSort) {
        params.nodeSort = xNodeSort;
      } else {
        params.nodeSort = undefined;
      }

      if (this.chart.customAttr) {
        const customAttr = JSON.parse(this.chart.customAttr);
        if (customAttr.color) {
          params.color = customAttr.color.colors;

          //透明度，设置了之后没有hover加深，看不出来选了啥
          /*const alpha = customAttr.color.alpha / 100;
          params.edgeStyle = {
            fillOpacity: alpha,
          }*/
        }

        if (customAttr.label) {
          params.label = {
            formatter: function (_a) {
              let name = _a.name;
              return name;
            },
            callback: function (x) {
              let isLast = x[1] === 1; // 最后一列靠边的节点
              return {
                style: {
                  fill: customAttr.label.show ? customAttr.label.color : 'transparent',
                  fontSize: parseInt(customAttr.label.fontSize),
                  textAlign: isLast ? 'end' : 'start',
                },
                offsetX: isLast ? -8 : 8,
              };
            },
            layout: [
              {
                type: 'hide-overlap',
              },
            ],
          }
        }

        if (customAttr.tooltip) {
          params.tooltip = customAttr.tooltip.show ? {
            showTitle: false,
            showMarkers: false,
            shared: false,
            // 内置：node 不显示 tooltip，edge 显示 tooltip
            showContent: function (items) {
              //return !Object(_antv_util__WEBPACK_IMPORTED_MODULE_1__["get"])(items, [0, 'data', 'isNode']);
              return customAttr.tooltip.show && items.length > 0 && items[0].value !== undefined;
            },
            formatter: function (datum) {
              let source = datum.source, target = datum.target, value = datum.value;
              return {
                name: source + ' -> ' + target,
                value: value,
              };
            },
            //showContent: customAttr.tooltip.show,
            domStyles: {
              'g2-tooltip': {
                fontSize: customAttr.tooltip.textStyle.fontSize + 'px',
                color: customAttr.tooltip.textStyle.color,
                backgroundColor: customAttr.tooltip.backgroundColor,
              }
            }
          } : false;
        }

      }

      return params;
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
          clear(this.myChart.container)
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
