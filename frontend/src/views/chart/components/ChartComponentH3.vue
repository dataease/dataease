<template>
  <div ref="chartContainer" style="padding: 0;widht: 100%;height: 100%;overflow: hidden;" :style="bg_class">
    <view-track-bar ref="viewTrack" :track-menu="trackMenu" class="track-bar" :style="trackBarStyleTime" @trackClick="trackClick" />
    <span v-if="chart.type&&chart.type==='arc_map'" v-show="title_show" ref="title" :style="title_class" style="cursor: default;display: block;">
      <p style="padding:6px 10px 0 10px;margin: 0;overflow: hidden;white-space: pre;text-overflow: ellipsis;">{{ chart.title }}</p>
    </span>
    <div v-if="chart.type === '3dpie'" :id="chartId" style="width: 100%;overflow: hidden;" :style="{height:chartHeight}"></div>
    <div v-if="chart.type === 'arc_map'" :id="chartId" style="width: 100%;overflow: hidden;" :style="{height:chartHeight}">
      <!-- <ArcGIS :chartId="chartId" :chartHeight="chartHeight" :chart="chart" /> -->
    </div>
  </div>
</template>

<script>
import highcharts from 'highcharts'
import highcharts3d from 'highcharts/highcharts-3d'
highcharts3d(highcharts)

import { loadModules,loadCss } from 'esri-loader'
import config from '@/map/config.js'

import { uuid } from 'vue-uuid'
import ViewTrackBar from '@/components/canvas/components/Editor/ViewTrackBar.vue'
import { hexColorToRGBA } from '@/views/chart/chart/util'
import { BASE_PIE, basePieOption } from '@/views/chart/chart/pie/pie_hc'
export default {
  components: {
    ViewTrackBar
  },
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
    }
  },
  data() {
    return {
      myChart: null,
      chartId: uuid.v1(),
      showTrackBar: true,
      trackBarStyle: {
        position: 'absolute',
        left: '0px',
        top: '0px'
      },
      pointParam: null,
      dynamicAreaCode: null,
      borderRadius: '0px',
      chartHeight: '100%',
      title_class: {
        margin: '0 0',
        width: '100%',
        fontSize: '18px',
        color: '#303133',
        textAlign: 'left',
        fontStyle: 'normal',
        fontWeight: 'normal',
        background: hexColorToRGBA('#ffffff', 0)
      },
      container_bg_class: {
        background: hexColorToRGBA('#ffffff', 0)
      },
      title_show: true,
      antVRenderStatus: false,
      currentPage: {
        page: 1,
        pageSize: 20,
        show: 0
      },
      tableData: [],
      graphicData: "",
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
      handler(newVal, oldVal) {
        this.initTitle()
        this.calcHeightDelay()
        new Promise((resolve) => { resolve() }).then(() => {
          this.drawView()
        })
      },
      deep: true
    },
    resize() {
      this.drawEcharts()
    }
  },
  created() {
    console.log('3ddddddddddd')
    if (!this.$highcharts) {
      this.$highcharts = highcharts
    }
  },
  mounted() {
    this.preDraw()
    console.log('饼数据。。。', this.chart)
  },
  methods: {
    preDraw() {
      this.initTitle()
      this.calcHeightDelay()
      new Promise((resolve) => { resolve() }).then(() => {
        this.drawView()
      })
    },
    drawView() {
      const chart = this.chart
      this.antVRenderStatus = true
      if (chart.type === '3dpie') {
        console.log('3dpie...')
        this.myChart = this.$highcharts.chart(this.chartId, JSON.parse(JSON.stringify(BASE_PIE)))

        this.drawEcharts()
      } else if (chart.type === 'arc_map') {
        console.log('arcMap......')
        this.drawArcMap(this.chartId,chart)
      }
      // else {
      //   if(this.myChart) {
      //     this.antVRenderStatus = false
      //     this.myChart.destroy()
      //   }
      // }

      // if (this.myChart && this.searchCount > 0) {
      //   this.myChart.options.animation = false
      // }

      // if (this.antVRenderStatus) {
      //   this.myChart.render()
      // }
    },

    // 实例化arcgis地图
    drawArcMap(container,chart){
      console.log('arcMap::::::::::',container,chart)
      let arr = []
      let arrObj = []
      if(chart.data) {
        if(chart.data.fields.length > 0) {
          chart.data.fields.forEach(e => {
            arr.push(e.name)
            arrObj.push({name: e.name,datainsName: e.datainsName})
          })
        }
      }
      // 加载地图必备样式文件
      loadCss("/arcgisapi/esri/css/main.css");
      loadCss("/arcgisapi/dijit/themes/claro/claro.css");

      // 异步加载对应 js 模块
      loadModules(
        [
          'esri/Map',
          'esri/views/MapView',
          "esri/layers/TileLayer",
          "esri/widgets/Home",
          "esri/widgets/ScaleBar",
          "esri/geometry/SpatialReference",
          "esri/symbols/PictureMarkerSymbol",
          "esri/Graphic",
          "esri/layers/GraphicsLayer",
          "esri/layers/FeatureLayer",
          "esri/geometry/Point",
          "esri/identity/IdentityManager"
        ],
        config.loadConfig
      ).then(
        ([
          Map,
          MapView,
          TileLayer,
          Home,
          ScaleBar,
          SpatialReference,
          PictureMarkerSymbol,
          Graphic,
          GraphicsLayer,
          FeatureLayer,
          Point,
          IdentityManager
        ]) => {
          IdentityManager.registerToken({
            server: "http://2.40.7.227:8080/OneMapServer/rest/services",
            token: "QbGyxIz4ZomJ7QeG5aZ515OALV9RVsvf2M2zOALRvciUvf3ir3YDw5zNt_zy9XAd_bKHHm0UojqXeqfFlp_Dz5PiT6wuiuQhJazQCinTPozNKjGNo7SG5-mZs4yj6kmbocoiXBK8jLIvv6qj8hF_5A.."
          })
          let that = this
          var ygyx = new TileLayer({
            // url: "https://sampleserver6.arcgisonline.com/arcgis/rest/services/Census/MapServer",
            url: "http://2.40.7.227:8080/OneMapServer/rest/services/DZDTGuSuMapDark/MapServer",
            id:'ygyx'
          });
          // 创建地图
          var map=new Map({
            layers:[ygyx]
            // basemap: 'osm',
          });
          // 地图实例化
          // 中心点
          var wktString = "PROJCS[\"2000SZ\",GEOGCS[\"GCS_China_Geodetic_Coordinate_System_2000\",DATUM[\"D_China_2000\",SPHEROID[\"CGCS2000\",6378137.0,298.257222101]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Gauss_Kruger\"],PARAMETER[\"False_Easting\",350000.0],PARAMETER[\"False_Northing\",-2800000.0],PARAMETER[\"Central_Meridian\",120.7833333333333],PARAMETER[\"Scale_Factor\",1.0],PARAMETER[\"Latitude_Of_Origin\",0.0],UNIT[\"Meter\",1.0]],VERTCS[\"EGM96_Geoid\",VDATUM[\"EGM96_Geoid\"],PARAMETER[\"Vertical_Shift\",0.0],PARAMETER[\"Direction\",1.0],UNIT[\"Meter\",1.0]]"
          var point = new Point({
              x: 331780.7808473259,
              y: 667386.2572571054,
              spatialReference: {wkt: wktString}
            }
          );

          let view = new MapView({
            container: container,
            map: map,
            center: point,
            zoom: 4,
            popup: {
              collapseEnabled : false, // 是否需title点击折叠功能
              dockEnabled: true,  // 指示弹出窗口的位置是否停靠在视图的一侧
              dockOptions: {
                position: 'top-center',
                buttonEnabled: false, // 开启固定标签页
                // breakpoint: true,  // 开启 点击停靠气泡窗
              },
              actions: [] // 清空事件按钮 （缩放至、...）
            },
            // rotation: 45
          });
          // 比例尺
          var scaleBar = new ScaleBar({
            view: view,
            unit: "metric" // 比例尺显示公制和非公制单位。
          });
          // 比例尺添加到地图左下角
          view.ui.add(scaleBar, {
            position: "bottom-left"//左下角
          });
          // home 回到视图中心
          var homeBtn = new Home({
            view: view,
            container:"home"
          });
          view.ui.add(homeBtn, "top-right");//右上角

          // 先清除所有
          view.graphics.removeAll();

          if(arr.includes('经度') && arr.includes('纬度') && arr.includes('街道名称') && arr.includes('街道简介')){
            let arrGrap =  this.loadPoint(arrObj,chart.data.tableRow)

            arrGrap.forEach(item => {
              graphicView(item)
            })

          }

          //给“地图视图”绑定点击事件
          view.popup.autoOpenEnabled = false
          var tmpGraphic = null
          let mouseOn = view.on('click', function (event) {//在MapView中添加鼠标监控事件
            console.log(event)
            view.hitTest(event).then((res) => {
              console.log(res)
              if (res.results.length) {
                let results = res.results
                if (results.length > 0) {
                  var ori = results[0].graphic
                  // 画新点
                  let g = ori.clone()
                  g.symbol.url = require('@/assets/point.png')
                  // that.graphicData = g
                  console.log(g)
                  // graphicView1(g.attributes)
                  view.graphics.add(g)
                  // let geo = g.geometry
                  let attr = g.attributes
                  // let point = new Point(geo.x, geo.y, view.spatialReference)
                  view.popup.open({
                    location: event.mapPoint,
                    title: attr.name,
                    content: `<p style="width:350px;overflow: auto;white-space:break-spaces;">${attr.desc}</p>`
                  })
                  // 删除原来的点
                  view.graphics.remove(ori)
                  tmpGraphic = g
                  console.log('tmpGraphic: ', tmpGraphic)
                }
              } else {
                // console.log(that.graphicData)
                // if(that.graphicData !== '') {
                //   view.graphics.remove(that.graphicData)
                //   let g = JSON.parse(JSON.stringify(that.graphicData)).attributes
                //   graphicView(g)
                // }
                view.popup.close()
              }
            })
          })

          // view popup关闭事件监听
          view.popup.watch('visible', evt => {
            console.log('popup event', evt)
            console.log('ori tmpGraphic: ', tmpGraphic)
            var var3 = tmpGraphic.clone()
            if(!evt) {
              var3.symbol.url = require("@/assets/point2.png")
            } else {
              var3.symbol.url = require("@/assets/point.png")
            }
            view.graphics.add(var3)
            view.graphics.remove(tmpGraphic)
            tmpGraphic = var3
            console.log('new tmpGraphic: ', tmpGraphic)
          })

          this.myChart = view
          console.log('view',view,this.myChart)

          function graphicView(data) {
            var wktString = "PROJCS[\"2000SZ\",GEOGCS[\"GCS_China_Geodetic_Coordinate_System_2000\",DATUM[\"D_China_2000\",SPHEROID[\"CGCS2000\",6378137.0,298.257222101]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Gauss_Kruger\"],PARAMETER[\"False_Easting\",350000.0],PARAMETER[\"False_Northing\",-2800000.0],PARAMETER[\"Central_Meridian\",120.7833333333333],PARAMETER[\"Scale_Factor\",1.0],PARAMETER[\"Latitude_Of_Origin\",0.0],UNIT[\"Meter\",1.0]],VERTCS[\"EGM96_Geoid\",VDATUM[\"EGM96_Geoid\"],PARAMETER[\"Vertical_Shift\",0.0],PARAMETER[\"Direction\",1.0],UNIT[\"Meter\",1.0]]"
            let graphic = new Graphic({  // 图形是现实世界地理现象的矢量表示，它可以包含几何图形，符号和属性
              geometry: {  //点位信息
                type: 'point',
                longitude: data.lng,
                latitude: data.lat,
                spatialReference: {
                  wkt: wktString
                }
              },
              symbol: {  //图像
                //类型有 图片标记 和 点
                type: 'picture-marker',
                //图片地址，可以使用网络路径或本地路径 (base64也可以)
                url: require("@/assets/point2.png"),
                // 图片大小
                width: '130px',
                height: '71px'
              },
              // 实际的应用过程中会有地图上要显示不同种类、不同颜色的图形点位需求，可以在这里配置不同的点位参数及类别，然后在点击点位的事件方法里进行类别逻辑判断。
              attributes: {
                // name: data.name,
                // desc: data.desc,
                ...data
              },
            });
            // 将图形添加到视图的图形层
            console.log('graphic:' , graphic)
            view.graphics.addMany([graphic])
            if(that.graphicData !== ''){
              console.log('graphicData',that.graphicData)
              that.graphicData = ''
            }
          }
          // function graphicView1(data) {
          //   var wktString = "PROJCS[\"2000SZ\",GEOGCS[\"GCS_China_Geodetic_Coordinate_System_2000\",DATUM[\"D_China_2000\",SPHEROID[\"CGCS2000\",6378137.0,298.257222101]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Gauss_Kruger\"],PARAMETER[\"False_Easting\",350000.0],PARAMETER[\"False_Northing\",-2800000.0],PARAMETER[\"Central_Meridian\",120.7833333333333],PARAMETER[\"Scale_Factor\",1.0],PARAMETER[\"Latitude_Of_Origin\",0.0],UNIT[\"Meter\",1.0]],VERTCS[\"EGM96_Geoid\",VDATUM[\"EGM96_Geoid\"],PARAMETER[\"Vertical_Shift\",0.0],PARAMETER[\"Direction\",1.0],UNIT[\"Meter\",1.0]]"
          //
          //   let graphic = new Graphic({  // 图形是现实世界地理现象的矢量表示，它可以包含几何图形，符号和属性
          //     geometry: {  //点位信息
          //       type: 'point',
          //       longitude: data.lng,
          //       latitude: data.lat,
          //       spatialReference: {
          //         wkt: wktString
          //       }
          //     },
          //     symbol: {  //图像
          //       //类型有 图片标记 和 点
          //       type: 'picture-marker',
          //       //图片地址，可以使用网络路径或本地路径 (base64也可以)
          //       url: require("@/assets/point.png"),
          //       // 图片大小
          //       width: '130px',
          //       height: '71px'
          //     },
          //     // 实际的应用过程中会有地图上要显示不同种类、不同颜色的图形点位需求，可以在这里配置不同的点位参数及类别，然后在点击点位的事件方法里进行类别逻辑判断。
          //     attributes: {
          //       // name: data.name,
          //       // desc: data.desc,
          //       ...data
          //     },
          //   });
          //   // 将图形添加到视图的图形层
          //   console.log('graphic:' , graphic)
          //   view.graphics.addMany([graphic])
          // }
        }
      ).catch(err => {
        console.log(err)
      })
    },
    // 数据处理
    loadPoint(data,items) {
      console.log(data,items)
      let objs = {}

      for(let item of data) {
        if(item){
          let key = item.datainsName || index
          objs[key] = item.name
        }
      }
      let arrData = []
      items.forEach(item => {
        let obj = {}
        let formatObj = {
          '经度': "lng",
          '纬度': "lat",
          '街道名称': "name",
          '街道简介': "desc",
        }
        for(let key in item) {
          let formatKey = formatObj[objs[key]];
          obj[formatKey] = item[key]
        }
        arrData.push(obj)
      })
      console.log(arrData)
      return arrData
    },

    drawEcharts() {
      const chart = this.chart
      let chart_option = {}

      if (this.myChart && this.searchCount > 0) {
        chart_option.animation = false
      }
      const base_json = JSON.parse(JSON.stringify(BASE_PIE))

      chart_option = basePieOption(base_json, chart, this.terminalType)
      this.myEcharts(chart_option)
    },
    myEcharts(option) {
      // 指定图表的配置项和数据
      const chart = this.myChart
      this.setBackGroundBorder()
      setTimeout(chart.update(option, true), 500)
      window.onresize = function() {
        this.myChart.reflow()
      }
    },
    antVAction(param) {
      console.log(param)
      if (this.chart.type === 'treemap') {
        this.pointParam = param.data.data
      } else {
        this.pointParam = param.data
      }
      if (this.trackMenu.length < 2) { // 只有一个事件直接调用
        this.trackClick(this.trackMenu[0])
      } else { // 视图关联多个事件
        this.trackBarStyle.left = param.x + 'px'
        this.trackBarStyle.top = (param.y + 10) + 'px'
        this.$refs.viewTrack.trackButtonClick()
      }
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
          this.title_class.fontSize = customStyle.text.fontSize + 'px'
          this.title_class.color = customStyle.text.color
          this.title_class.textAlign = customStyle.text.hPosition
          this.title_class.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
          this.title_class.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'
        }
        if (customStyle.background) {
          this.title_class.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
          this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'
        }
      }
    },
    calcHeightDelay() {
      setTimeout(() => {
        this.calcHeightRightNow()
      }, 100)
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
    }
  }
}
</script>
<style lang="scss" scoped>

  ::v-deep .esri-popup__header-title {
    border-radius: 2px;
    font-size: 14px;
    padding: 6px 7px;
    margin: 6px auto 6px 7px;
    display: block;
    transition: background-color 125ms ease-in-out;
    white-space: pre-wrap;
    word-break: break-all;
    word-wrap: break-word;
    word-break: break-word;
    margin-top: 36px;
    margin-left: 50px;
    color: #fadc43;
  }

  ::v-deep .esri-view-width-xlarge .esri-popup__main-container {
    width: 400px;
  }

  ::v-deep .esri-popup__main-container {
   pointer-events: auto;
   position: relative;
   z-index: 1;
   width: 340px;
   max-height: 340px;
   background-color: rgba(0,0,0,0);
   display: flex;
   flex-flow: column nowrap;
   width: 400px;
   height: 271px;
   background-image: url('../../../assets/popup-background.png');
   color: white;
 }

  ::v-deep .esri-popup__pointer {
    position: absolute;
    width: 0;
    height: 0;
    display: none;
  }

  ::v-deep .esri-popup__content {
   display: flex;
   flex-flow: column nowrap;
   flex: 1 1 auto;
   font-size: 12px;
   font-weight: 400;
   margin: 0 15px 12px;
   overflow: auto;
   line-height: normal;
   margin-left: 28px;
   margin-top: 10px;
   white-space: break-spaces;
 }


</style>
