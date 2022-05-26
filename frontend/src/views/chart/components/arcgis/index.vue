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
      <div :id="chartId"></div>
    </div>
  </div>
</template>

<script>
// import toolbar1 from "./components/toolbar1.vue"; // 工具条组件
// import Measurement from "./components/Measurement.vue"; // 测量组件
// import ArcGIS from "@/map/index.js";
import ArcGIS from "@/map/init.js"
const Map = new ArcGIS();
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
    }
  },
  data() {
    return {
      isShowMeasurement: false,
    };
  },
  mounted() {
    console.log('chartId:    ',this.chartId)
    Map.init(this.chartId)
  },
  methods: {
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
  }
}
</script>

<style lang="less">
.main {
  width: 100%;
  height: 100%;
  #map {
    width: 100%;
    height: 100%;
  }
}
</style>