<template>
  <div ref="chartContainer" style="padding: 0;width: 100%;height: 100%;overflow: hidden;" :style="bg_class">
    <view-track-bar ref="viewTrack" :track-menu="trackMenu" class="track-bar" :style="trackBarStyleTime" @trackClick="trackClick" />
    <span v-if="chart.type && antVRenderStatus" v-show="title_show" ref="title" :style="titleClass" style="cursor: default;display: block;">
      <div>
        <p style="padding:6px 4px 0;margin: 0;overflow: hidden;white-space: pre;text-overflow: ellipsis;display: inline;">{{ chart.title }}</p>
        <title-remark v-if="remarkCfg.show" style="text-shadow: none!important;" :remark-cfg="remarkCfg" />
      </div>
    </span>
    <div :id="chartId" style="width: 100%;overflow: hidden;" :style="{height:chartHeight}" />

    <div class="map-zoom-box" :class="loading ? 'symbol-map-loading' : 'symbol-map-loaded'">
      <div style="margin-bottom: 0.5em;">
        <el-button :style="{'background': buttonTextColor ? 'none' : '', 'opacity': buttonTextColor ? '0.75': '', 'color': buttonTextColor, 'borderColor': buttonTextColor}" size="mini" icon="el-icon-plus" circle @click="roamMap(true)" />
      </div>

      <div style="margin-bottom: 0.5em;">
        <el-button :style="{'background': buttonTextColor ? 'none' : '', 'opacity': buttonTextColor ? '0.75': '', 'color': buttonTextColor, 'borderColor': buttonTextColor}" size="mini" icon="el-icon-refresh" circle @click="resetZoom()" />
      </div>

      <div>
        <el-button :style="{'background': buttonTextColor ? 'none' : '', 'opacity': buttonTextColor ? '0.75': '', 'color': buttonTextColor, 'borderColor': buttonTextColor}" size="mini" icon="el-icon-minus" circle @click="roamMap(false)" />
      </div>

    </div>


  </div>
</template>

<script>
import {PointLayer, Popup, Scene} from '@antv/l7'
import {hexColorToRGBA, uuid} from '@/utils/symbolmap'
import ViewTrackBar from '@/components/views/ViewTrackBar'
import {getDefaultTemplate} from '@/utils/map'
import {getRemark} from "../../../components/views/utils";

export default {
    name: 'ChartComponentG2',
    components: { ViewTrackBar },
    props: {
      chart: {
        type: Object,
        required: true
      },
      filter: {
        type: Object,
        required: false,
        default: function() {
          return {}
        }
      },
      trackMenu: {
        type: Array,
        required: false,
        default: function() {
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
      }
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
        chartHeight: '100%',
        titleClass: {
          margin: '0 0',
          position: 'absolute',
          zIndex: 1,
          width: '100%',
          fontSize: '18px',
          color: '#303133',
          textAlign: 'left',
          fontStyle: 'normal',
          fontWeight: 'normal',
          background: hexColorToRGBA('#ffffff', 0)
        },
        title_show: true,
        antVRenderStatus: false,
        remarkCfg: {
          show: false,
          content: ''
        },
        buttonTextColor: null,
        loading: true,
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
      }
    },
    watch: {
      chart: {
        handler(newVal, oldVla) {
          this.initTitle()
          this.calcHeightDelay()
          new Promise((resolve) => { resolve() }).then(() => {
            this.drawView()
          })
        },
        deep: true
      }
    },
    mounted() {
      this.preDraw()
    },
    beforeDestroy() {
      window.removeEventListener('resize', this.calcHeightDelay)

      this.pointLayer.mapService.map.clearMap()
      this.pointLayer.mapService.map.destroy()
      this.pointLayer.destroy()
      this.pointLayer.layerService.clear()
      this.pointLayer.layerService.destroy()
      if (this.pointLayer.layerService.sceneService) {
        this.pointLayer.layerService.sceneService.destroy()
        for (const key in this.pointLayer.layerService.sceneService) {
          this.pointLayer.layerService.sceneService[key] = null
          this.$delete(this.pointLayer.layerService.sceneService, key)
        }
        this.pointLayer.layerService.sceneService.rendererService = {
          destroy: () => {}
        }
        this.pointLayer.layerService.sceneService._events = {}
      }

      if (this.textLayer) {
        this.textLayer.mapService.map.clearMap()
        this.textLayer.mapService.map.destroy()
        this.textLayer.destroy()
        this.textLayer.layerService.clear()
        this.textLayer.layerService.destroy()
        if (this.textLayer.layerService.sceneService) {
          this.textLayer.layerService.sceneService.destroy()
          for (const key in this.textLayer.layerService.sceneService) {
            this.textLayer.layerService.sceneService[key] = null
            this.$delete(this.textLayer.layerService.sceneService, key)
          }
          this.textLayer.layerService.sceneService.rendererService = {
            destroy: () => {}
          }
          this.textLayer.layerService.sceneService._events = {}
        }
      }


      this.myChart.destroy()
      this.pointLayer.layerPickService.layer.textureService.destroy()
      this.pointLayer.layerPickService.layer.textureService.rendererService.destroy()
      this.pointLayer.layerModel.dataTexture && this.pointLayer.layerModel.dataTexture.texture._texture.unbind()

      if (this.textLayer) {
        this.textLayer.layerPickService.layer.textureService.destroy()
        this.textLayer.layerPickService.layer.textureService.rendererService.destroy()
        this.textLayer.layerModel.dataTexture && this.textLayer.layerModel.dataTexture.texture._texture.unbind()
      }


      for(let i of this.myChart.container.parent._bindingDictionary._map.keys()) {
        this.myChart.container.parent._bindingDictionary._map.get(i).forEach(ele => {
          ele.cache = null
          ele.activated = false
        })
      }
      this.myChart.container._bindingDictionary._map.clear()

      this.pointLayer.configService.clean() // GlobalConfigService
      if (this.textLayer) {
        this.textLayer.configService.clean() // GlobalConfigService
      }

      if (this.pointLayer) {
        for (const key in this.pointLayer) {
          this.pointLayer[key] = null
          this.$delete(this.pointLayer, key)
        }
      }

      if (this.textLayer) {
        for (const key in this.textLayer) {
          this.textLayer[key] = null
          this.$delete(this.textLayer, key)
        }
      }
      if (this.myChart) {
        for (const key in this.myChart) {
          this.myChart[key] = null
          this.$delete(this.myChart, key)
        }
      }
      this.myChart = null
      this.textLayer = null
      this.pointLayer = null
    },
    methods: {
      preDraw() {
        this.loading = true
        this.initTitle()
        this.calcHeightDelay()
        this.initMap()
        window.addEventListener('resize', this.calcHeightDelay)

      },
      executeAxios(url, type, data, callBack) {
        const param = {
          url: url,
          type: type,
          data: data,
          callBack: callBack
        }
        this.$emit('execute-axios', param)
        if (process.env.NODE_ENV === 'development') {
          execute(param).then(res => {
            if (param.callBack) {
              param.callBack(res)
            }
          }).catch(e => {
            if (param.error) {
              param.error(e)
            }
          })
        }
      },
      getMapKey() {
        const key = 'online-map-key'
        return new Promise((resolve, reject) => {
          if (localStorage.getItem(key)) {
            resolve(localStorage.getItem(key))
          } else {
            const url = "/system/onlineMapKey"
            this.executeAxios(url, 'get', {}, res => {
              const val = res.data
              localStorage.setItem(key, val)
              resolve(val)
            })
          }
        })
      },
      async initMap() {
        if (!this.myChart) {
          let theme = this.getMapTheme(this.chart)
          const lang = this.$i18n.locale.includes('zh') ? 'zh' : 'en'
          const mapConfig = {
            lang: lang,
            pitch: 0,
            style: theme,
            center: [121.434765, 31.256735],
            zoom: 6
          }
          const key = await this.getMapKey()
          if (key) {
            mapConfig.token = key
          }
          this.myChart = new Scene({
            id: this.chartId,
            map: new this.$gaodeMap(mapConfig),
            logoVisible: false
          })
          const chart = JSON.parse(JSON.stringify(this.chart))


          this.antVRenderStatus = true
          if (!chart.data || !chart.data.data) {
            chart.data = {
              data: []
            }
          }
          this.myChart.on('loaded', () => {
            this.addGlobalImage()

            this.drawView()
          })
        } else {
          this.loading = false
        }
      },
      drawView() {
        this.loading = true
        this.setLayerAttr(this.chart)
        this.setBackGroundBorder()
      },
      addGlobalImage() {
        this.myChart.addImage('marker','/api/pluginCommon/staticInfo/map-marker/svg')
      },

      fillStrTemplate(template, properties) {
          if(!template) return null
          const regex = /\$\{([^}]+)\}/g
          const replacer = (match, item) => {
              return properties[item]
          }
          return template.replace(regex, replacer)
      },



      addTextLayer(originData, chart) {
        let customAttr = {}
        let TextSize = 5
        let textColor = null
        if (!chart.customAttr) {
            return
        }
        customAttr = JSON.parse(chart.customAttr)
        if (!customAttr.label || !customAttr.label.show) {
            return
        }
        TextSize = customAttr.label.fontSize
        textColor = customAttr.label.color

        const defaultTemplate = "经度：${longitude}，纬度：${latitude}"
        const templateWithField = getDefaultTemplate(chart, 'labelAxis', false, false)
        const labelTemplate = customAttr.label.labelTemplate || templateWithField || defaultTemplate
        const data = originData.filter(item => item.longitude && item.latitude)
        data.forEach(item => {
          const properties = item.properties || {}
          properties.longitude = item.longitude
          properties.latitude = item.latitude

          try {
              item.labelResult = this.fillStrTemplate(labelTemplate, properties)
          }catch (error) {

          }
          item.labelResult = item.labelResult || this.fillStrTemplate(defaultTemplate, properties)
          item.labelResult = item.labelResult.replaceAll('\n', ' ')
        })

        this.textLayer = new PointLayer({})
            .source(data,
            {
                parser: {
                type: 'json',
                x: 'longitude',
                y: 'latitude'
                }
            }
            )
            .shape("labelResult", 'text')
            .size(TextSize)
            .color(textColor || '#ffffff')
            .style({
                textAnchor: 'center', // 文本相对锚点的位置 center|left|right|top|bottom|top-left
                textOffset: [ 5, -15 ], // 文本相对锚点的偏移量 [水平, 垂直]
                spacing: 2, // 字符间距
                padding: [ 1, 4 ], // 文本包围盒 padding [水平，垂直]，影响碰撞检测结果，避免相邻文本靠的太近
                stroke: '#ffffff', // 描边颜色
                strokeWidth: 0.3, // 描边宽度
                strokeOpacity: 1.0,
                fontFamily: 'Times New Roman',
                textAllowOverlap: false
            });
        this.myChart.addLayer(this.textLayer);
      },
      removeTextLayer() {
          this.myChart.layerService.removeLayer(this.textLayer)
      },

      setLayerAttr (chart) {
        let defaultSymbol = 'marker'
        let customAttr = {}
        let layerStyle = {}
        if (chart.customAttr) {
            customAttr = JSON.parse(chart.customAttr)
            if (customAttr.size && customAttr.size.scatterSymbol) {
                defaultSymbol = customAttr.size.scatterSymbol
            }
            if (customAttr.size && customAttr.size.symbolOpacity) {
                layerStyle.opacity = customAttr.size.symbolOpacity / 10
            }
            if (customAttr.size && customAttr.size.symbolStrokeWidth) {
                layerStyle.strokeWidth = customAttr.size.symbolStrokeWidth
            }
        }

        this.myChart.removeAllLayer().then(() => {
          let data = chart.data && chart.data.data || []
          data = data.filter(item => item.longitude && item.latitude)
          this.pointLayer = new PointLayer({autoFit: true})
          this.pointLayer.source(data, {
            parser: {
              type: 'json',
              x: 'longitude',
              y: 'latitude'
            }
          }).shape(defaultSymbol).active(true).style(layerStyle)
          this.pointLayer.on('add', ev => {
            setTimeout(() => {
              this.loading = false
            }, 500)
          })
          this.myChart.addLayer(this.pointLayer);
          this.addTextLayer(data, chart)
          this.pointLayer.on('click', ev => {
            const param = {...ev, ...{'data': ev.feature}}
            this.pointParam = param
            if (this.trackMenu.length < 2) { // 只有一个事件直接调用
              this.trackClick(this.trackMenu[0])
            } else { // 视图关联多个事件
              this.trackBarStyle.left = ev.target.offsetX + 'px'
              this.trackBarStyle.top = (ev.target.offsetY - 15) + 'px'
              this.$refs.viewTrack.trackButtonClick()
            }
          })

          this.pointLayer && this.pointLayer.off('mousemove')

          const theme = this.getMapTheme(chart)
          this.myChart && this.myChart.setMapStyle && this.myChart.setMapStyle(theme)
          const colors = []

          if (customAttr) {
            if (customAttr.color) {
              const c = JSON.parse(JSON.stringify(customAttr.color))
              c.colors.forEach(ele => {
                colors.push(hexColorToRGBA(ele, c.alpha))
              })
              const colorAxis = JSON.parse(chart.xaxisExt)
              if (colorAxis && colorAxis.length) {
                this.pointLayer.color('color', colors)
              } else {
                this.pointLayer.color(colors[0])
              }
            }
            const yaxis = JSON.parse(chart.yaxis)
            const hasYaxis =  yaxis && yaxis.length


            if (customAttr.tooltip) {
              const t = JSON.parse(JSON.stringify(customAttr.tooltip))
              if (t.show) {
                const fontSize = t.textStyle.fontSize
                const fontColor = t.textStyle.color

                const htmlPrefix = '<div style=\'font-size:'+fontSize+'px;color:'+fontColor+';\'>'
                const htmlSuffix = '</div>'

                const templateWithField = getDefaultTemplate(chart, 'tooltipAxis', true, true)

                this.pointLayer.on('mousemove', event => {
                  if (!t.show) {
                    return
                  }
                  let content = event.feature.longitude + ',' + event.feature.latitude
                  if (event.feature.properties && (customAttr.tooltip.tooltipTemplate || templateWithField)) {
                    const properties = event.feature.properties
                    const tooltipTemplate = customAttr.tooltip.tooltipTemplate || templateWithField
                    try {
                      content = this.fillStrTemplate(tooltipTemplate, properties)
                    } catch (error) {

                    }
                    content = content || event.feature.longitude + ',' + event.feature.latitude
                  }
                  content = content.replaceAll('\n', '<br>')
                  const innerHtml = htmlPrefix + content + htmlSuffix
                  const popup = new Popup({
                    offsets: [ 0, 0 ],
                    closeButton: false
                  }).setHTML(innerHtml);
                  popup.setLnglat(event.lngLat)
                  this.myChart.addPopup(popup)
                })
                this.pointLayer.on('mouseout', event => {
                  const popups = this.myChart.popupService.popups
                  popups.forEach(popup => {
                    popup.remove()
                  })
                })
              }
            }
            let defaultSize = 15
            if (customAttr.size && customAttr.size.scatterSymbolSize) {
              defaultSize = customAttr.size.scatterSymbolSize
            }
            const baseSizeMap = this.calcStepBase(data)
            if (hasYaxis && baseSizeMap) {
              this.pointLayer.size('busiValue', val => {
                return Math.ceil(baseSizeMap.baseLine + (val - baseSizeMap.valMin) * baseSizeMap.step)
              })
            } else {
              this.pointLayer.size(defaultSize)
            }
          }
          this.myChart.render()
          this.pointLayer && this.resetZoom()
          // 自动放大两级
          let index = 2
          while (index--) {
            this.roamMap(true)
          }
        })
      },

      calcStepBase(data) {
        if (!data || data.length === 0) {
          return null
        }
        const valueArray = data.map(item => item.busiValue || 0)

        const min = Math.min(...valueArray)
        const max = Math.max(...valueArray)
        if (max === min) {
          return null
        }
        const baseMin = 5
        const baseMax = 35
        const step =  (baseMax - baseMin) / (max - min)
        return {
          baseLine: baseMin,
          valMin: min,
          step
        }
      },

      getMapTheme(chart) {
        let theme = 'normal'
        if (chart.customStyle) {
            const customStyle = JSON.parse(chart.customStyle)
            if (customStyle.baseMapStyle && customStyle.baseMapStyle.baseMapTheme) {
                theme = customStyle.baseMapStyle.baseMapTheme
            }
        }
        if (theme === 'light') {
          theme = 'normal'
        }
        return `amap://styles/${theme}`
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
        this.calcHeightDelay()
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
        const linkageParam = {
          option: 'linkage',
          viewId: this.chart.id,
          dimensionList: this.pointParam.data.dimensionList,
          quotaList: this.pointParam.data.quotaList
        }
        const jumpParam = {
          option: 'jump',
          viewId: this.chart.id,
          dimensionList: this.pointParam.data.dimensionList,
          quotaList: this.pointParam.data.quotaList
        }
        switch (trackAction) {
          case 'drill':
            this.$emit('onChartClick', this.pointParam)
            break
          case 'linkage':
            this.$store.commit('addViewTrackFilter', linkageParam)
            break
          case 'jump':
            this.$emit('onJumpClick', jumpParam)
            break
          default:
            break
        }
      },

      initTitle() {
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

      calcHeightRightNow() {
        this.$nextTick(() => {
          if (this.$refs.chartContainer) {
            const currentHeight = this.$refs.chartContainer.offsetHeight
            if (this.$refs.title) {
              const titleHeight = this.$refs.title.offsetHeight
              this.chartHeight = (currentHeight - titleHeight) + 'px'
            }
          }
        })
      },
      calcHeightDelay() {
        setTimeout(() => {
          this.calcHeightRightNow()
        }, 100)
      },

      roamMap(flag) {
          return flag ? this.myChart.zoomIn() : this.myChart.zoomOut()
      },
      resetZoom() {
        this.pointLayer.fitBounds()
      },
      initRemark() {
        this.remarkCfg = getRemark(this.chart)
      }
    }
  }
</script>

<style scoped lang="scss">
  .map-zoom-box {
    position: absolute;
    z-index: 999;
    left: 2%;
    bottom: 10px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    text-align: center;
    padding: 2px;
    border-radius: 5px
  }
  .track-bar >>> ul {
    width: 80px !important;
  }
</style>
