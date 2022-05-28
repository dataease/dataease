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
      <div :id="chartId" class="divView" :style="{ height: chartHeight }" />
    </div>
  </div>
</template>

<script>
// import toolbar1 from "./components/toolbar1.vue"; // 工具条组件
// import Measurement from "./components/Measurement.vue"; // 测量组件
// import ArcGIS from "@/map/index.js";

// import ArcGIS from "@/map/init.js"
// const Map = new ArcGIS();

import { loadModules, loadCss } from "esri-loader";
import config from "@/map/config.js";

export default {
  name: "ArcGIS",
  components: {
    // toolbar1,
    // Measurement,
  },
  props: {
    chartId: {
      type: String,
      required: true,
    },
    chartHeight: {
      type: String,
      default: "300px",
    },
  },
  data() {
    return {
      isShowMeasurement: false,
      view: null,
    };
  },
  mounted() {
    console.log("chartId:    ", this.chartId);
    // Map.init(this.chartId)
    this.loadArcGIS(this.chartId);
  },
  methods: {
    // 地图展示
    loadArcGIS($el) {
      const that = this;

      // 加载地图必备样式文件
      // loadCss("http://localhost:9528/arcgisapi/esri/css/main.css");
      // loadCss("http://localhost:9528/arcgisapi/dijit/themes/claro/claro.css");

      loadCss("/arcgisapi/esri/css/main.css");
      loadCss("/arcgisapi/dijit/themes/claro/claro.css");

      // 异步加载对应 js 模块
      loadModules(
        [
          "esri/Map",
          "esri/views/MapView",
          "esri/layers/MapImageLayer",
          "esri/widgets/Home",
          "esri/widgets/ScaleBar",
        ],
        config.loadConfig
      )
        .then(([Map, MapView, MapImageLayer, Home, ScaleBar]) => {
          var ygyx = new MapImageLayer({
            url: "https://sampleserver6.arcgisonline.com/arcgis/rest/services/Census/MapServer",
            // url: "http://2.40.7.227:8080/OneMapServer/rest/services/DZDTGuSuMapDark/MapServer?token=QbGyxIz4ZomJ7QeG5aZ515OALV9RVsvf2M2zOALRvciUvf3ir3YDw5zNt_zy9XAd_bKHHm0UojqXeqfFlp_Dz5PiT6wuiuQhJazQCinTPozNKjGNo7SG5-mZs4yj6kmbocoiXBK8jLIvv6qj8hF_5A..",
            id: "ygyx",
          });
          console.log("啥玩意？？", ygyx);
          // 创建地图
          var map = new Map({
            layers: [ygyx],
          });
          console.log("啥地图？？", map);
          // 地图实例化
          that.view = new MapView({
            container: $el,
            map: map,
            center: [120.2494, 30.2867],
            zoom: 11,
          });
          console.log("啥视图？？", this.view);
          // 比例尺
          var scaleBar = new ScaleBar({
            view: this.view,
            unit: "metric", // The scale bar displays both metric and non-metric units.
          });
          // 比例尺添加到地图左下角
          this.view.ui.add(scaleBar, {
            position: "bottom-left", // 左下角
          });
          // home 回到视图中心
          var homeBtn = new Home({
            view: this.view,
            container: "home",
          });
          this.view.ui.add(homeBtn, "top-right"); // 右上角
        })
        .catch((err) => {
          console.log(err);
        });
    },

    // 测量
    measurement(type) {
      console.log("测量：", type);
      switch (type) {
        case 0:
          this.isShowMeasurement = false;
          Map.MeasurementClose();
          break;
        case 1:
          this.isShowMeasurement = true;
      }
    },
    // 地图切换
    baseMapChange(type) {
      console.log("地图切换", type);
      Map.baseMapChange(type);
    },
    // 标绘
    draw(type) {
      console.log("标绘", type);
      Map.drawActive(type);
    },
    // 显示图例
    showLegend() {
      console.log("显示图例");
    },
    // 显示图层
    showLayerList() {
      console.log("显示图层");
    },
  },
};
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
