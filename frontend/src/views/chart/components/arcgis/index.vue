<template>
  <div>
    <!-- 工具条组件 -->
    <!-- <toolbar1
      @measurement="measurement"
      @baseMapChange="baseMapChange"
      @draw="draw"
      @showLegend="showLegend"
      @showLayerList="showLayerList"
    /> -->
    <!-- 测量组件 -->
    <!-- <Measurement :show="isShowMeasurement" @closMmeasurement="measurement" /> -->
    <!-- 地图部分 -->
    <div class="main">
      <div :id="chartId" class="divView" :style="{height: chartHeight}"></div>
    </div>
  </div>
</template>

<script>
  // import toolbar1 from "./components/toolbar1.vue"; // 工具条组件
  // import Measurement from "./components/Measurement.vue"; // 测量组件
  // import ArcGIS from "@/map/index.js";

  // import ArcGIS from "@/map/init.js"
  // const Map = new ArcGIS();

  import { loadModules,loadCss } from 'esri-loader'
  import config from '@/map/config.js'

  export default {
    name: "ArcGIS",
    components: {
      // toolbar1,
      // Measurement,
    },
    props: {
      chartId:{
        type: String,
        required: true
      },
      chartHeight: {
        type: String,
        default: '300px'
      },
      // chart: {
      //   type: Object,
      //   required: true
      // }
    },
    data() {
      return {
        isShowMeasurement: false,
        view: null,
        pointData: [],
      };
    },
    mounted() {
      console.log('chartId:    ',this.chartId,this.chart)
      // Map.init(this.chartId)
      this.loadArcGIS(this.chartId)
    },
    methods: {
      // 地图展示
      loadArcGIS($el){
        let that = this

        // 加载地图必备样式文件
        loadCss("http://localhost:9528/arcgisapi/esri/css/main.css");
        loadCss("http://localhost:9528/arcgisapi/dijit/themes/claro/claro.css");

        // 异步加载对应 js 模块
        loadModules(
          [
            'esri/Map',
            'esri/views/MapView',
            "esri/core/lang",
            "esri/layers/MapImageLayer",
            "esri/widgets/Home",
            "esri/widgets/ScaleBar",
            "esri/geometry/SpatialReference",
            "esri/symbols/PictureMarkerSymbol",
            "esri/Graphic",
            "esri/layers/GraphicsLayer",
            "esri/layers/FeatureLayer",
            "esri/geometry/Point",
          ],
          config.loadConfig
        ).then(
          ([
             Map,
             MapView,
             esriLang,
             MapImageLayer,
             Home,
             ScaleBar,
             SpatialReference,
             PictureMarkerSymbol,
             Graphic,
             GraphicsLayer,
             FeatureLayer,
             Point
           ]) => {
            var ygyx = new MapImageLayer({
              url: "https://sampleserver6.arcgisonline.com/arcgis/rest/services/Census/MapServer",
              // url: this.chart.urlMap,
              // url: "http://2.40.7.227:8080/OneMapServer/rest/services/DZDTGuSuMapDark/MapServer?token=QbGyxIz4ZomJ7QeG5aZ515OALV9RVsvf2M2zOALRvciUvf3ir3YDw5zNt_zy9XAd_bKHHm0UojqXeqfFlp_Dz5PiT6wuiuQhJazQCinTPozNKjGNo7SG5-mZs4yj6kmbocoiXBK8jLIvv6qj8hF_5A..",
              id:'ygyx'
            });

            // let layer = new FeatureLayer({
            //   url: '', // 特性图层的请求api 可以是官方，也可以是自己发布的图层地址
            //   objectIdField: "ObjectID", // 如果不是远程图层链接，而是自己定义的数据源，这个值必须定义
            //   geometryType: 'point', // 地理属性
            //   spatialReference: {wkid: 4326},
            //   source: '', //地图数据库，当没有远程图层地址时，可以自己构造数据源，
            //   popupTemplate: '',  // 自定义弹出窗的对象,
            //   renderer: '', // 自定义render函数
            // })
            // let layer = new FeatureLayer({
            //   portalItem: {
            //     id:
            //   }
            // })

            // 创建地图

            var map=new Map({
              // layers:[ygyx]
              basemap: 'osm',
            });
            // 地图实例化
            that.view = new MapView({
              container: $el,
              map: map,
              center: [120.585294, 31.299758],
              zoom: 14,
              popup: {
                dockEnabled: true,
                dockOptions: {
                  buttonEnabled: false,
                  breakpoint: false
                }
              },
              // rotation: 45
            });
            // 比例尺
            var scaleBar = new ScaleBar({
              view: this.view,
              unit: "metric" // 比例尺显示公制和非公制单位。
            });
            // 比例尺添加到地图左下角
            this.view.ui.add(scaleBar, {
              position: "bottom-left"//左下角
            });
            // home 回到视图中心
            var homeBtn = new Home({
              view: this.view,
              container:"home"
            });
            this.view.ui.add(homeBtn, "top-right");//右上角


            let graphic = new Graphic({  // 图形是现实世界地理现象的矢量表示，它可以包含几何图形，符号和属性
              geometry: {  //点位信息
                type: 'point',
                longitude: 120.619907,
                latitude: 31.317887
              },
              symbol: {  //图像
                //类型有 图片标记 和 点
                type: 'picture-marker',
                //图片地址，可以使用网络路径或本地路径 (base64也可以)
                url: require("@/assets/point.png"),
                // 图片大小
                width: '32px',
                height: '48px'
              },
              // 实际的应用过程中会有地图上要显示不同种类、不同颜色的图形点位需求，可以在这里配置不同的点位参数及类别，然后在点击点位的事件方法里进行类别逻辑判断。
              attributes: {
                Foo: 'Hello world!'
              },
            });
            console.log('gggggggggggggg',graphic)
            // 将图形添加到视图的图形层
            this.view.graphics.addMany([graphic])

            //给“地图视图”绑定点击事件
            const _this = this
            this.view.popup.autoOpenEnabled = false
            let mouseOn = _this.view.on('click', function (event) {//在MapView中添加鼠标监控事件
              _this.view.hitTest(event).then((res) => {
                if (res.results.length) {
                  let results = res.results
                  // let results = res.results.filter((result) => { // 检查图形是否属于感兴趣的图层
                  //   return result.graphic.layer.id === gl.id
                  // })
                  if (results.length > 0) {
                    let g = results[0].graphic
                    let geo = g.geometry
                    let point = new Point(geo.x, geo.y, _this.view.spatialReference)
                    _this.view.popup.open({ location: point,
                      title: '平江街道',
                      content: '平江路街道位于江苏省州古城区东北隅，东到护城河，南至干将河，<br>西至齐门河、临顿路河，北至护城河。辖区面积约2.48平方千米，总人口约36479人（2010年）。<br>辖历史街区、大儒巷、中张家巷、菉葭巷和拙政园、北园、东园7个社区。现已更名为平江街道'
                    })
                  }
                } else {
                  _this.view.popup.close()
                }
              })
            })
          }
        ).catch(err => {
          console.log(err)
        })
      },

      // 测量
      measurement(type) {
        console.log('测量：',type)
        switch (type) {
          case 0:
            this.isShowMeasurement = false;
            Map.MeasurementClose()
            break;
          case 1:
            this.isShowMeasurement = true;
        }
      },
      // 地图切换
      baseMapChange(type) {
        console.log('地图切换', type)
        Map.baseMapChange(type)
      },
      // 标绘
      draw(type) {
        console.log('标绘',type)
        Map.drawActive(type)
      },
      // 显示图例
      showLegend() {
        console.log("显示图例")
      },
      // 显示图层
      showLayerList() {
        console.log("显示图层")
      }
    },
    beforeDestroy () {
      this.view = null
    }
  }
</script>

<style lang="less">
  .main {
    width: 100%;
    height: 100%;

    .divView {
      width: 100%;
      height: 100%;
    }
  }
</style>
