<template>
  <div>
    <!-- 地图部分 -->
    <div class="main">
      <div :id="chartId" class="divView" :style="{height: chartHeight}"></div>
    </div>
  </div>
</template>

<script>
  import { loadModules,loadCss } from 'esri-loader'
  import config from '@/map/config.js'

  export default {
    name: "ArcGIS",
    components: {
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
      chart: {
        type: Object,
        required: true
      }
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
                url: require("@/assets/point2.png"),
                // 图片大小
                width: '196px',
                height: '156px'
              },
              // 实际的应用过程中会有地图上要显示不同种类、不同颜色的图形点位需求，可以在这里配置不同的点位参数及类别，然后在点击点位的事件方法里进行类别逻辑判断。
              attributes: {

              },
            });
            // 将图形添加到视图的图形层
            this.view.graphics.addMany([graphic])


            //给“地图视图”绑定点击事件
            const _this = this
            this.view.popup.autoOpenEnabled = false
            let mouseOn = _this.view.on('click', function (event) {//在MapView中添加鼠标监控事件
              _this.view.hitTest(event).then((res) => {
                if (res.results.length) {
                  // 获取每个图形上的 ID
                  // this.$message.info(JSON.stringify(res.results[0].graphic.attributes))
                  let results = res.results
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
