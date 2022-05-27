<template>
  <div>

    <div id="mapView" />
  </div>
</template>
<script>
export default {
  props: {
    chartId: {
      type: String,
      required: true
    }
  },
  data() {
    return {

    }
  },
  created() {
    console.log('chartId', this.chartId)
  },
  mounted() {
    const that = this
    that.$loadModules([
      'esri/Map',
      'esri/views/MapView',
      'esri/layers/MapImageLayer',
      'esri/widgets/Home',
      'esri/widgets/ScaleBar'
    ], that.$option)
      .then(([Map, MapView, MapImageLayer, Home, ScaleBar]) => {
        var ygyx = new MapImageLayer({
          url:
          'https://sampleserver6.arcgisonline.com/arcgis/rest/services/Census/MapServer',
          id: 'ygyx'
        })
        // 创建地图
        var map = new Map({
          layers: [ygyx]
        })
        // 地图实例化
        that.view = new MapView({
          container: 'mapView',
          map: map,
          center: [120.2494, 30.2867],
          zoom: 11
        })
        // 比例尺
        var scaleBar = new ScaleBar({
          view: this.view,
          unit: 'metric' // The scale bar displays both metric and non-metric units.
        })
        // 比例尺添加到地图左下角
        this.view.ui.add(scaleBar, {
          position: 'bottom-left'// 左下角
        })
        // home 回到视图中心
        var homeBtn = new Home({
          view: this.view,
          container: 'home'
        })
        this.view.ui.add(homeBtn, 'top-right')// 右上角
      })
  }
}
</script>

<style scoped lang="less">
#mapView{
  position: relative;
  width:100%;
  height:100%;
  .xiao{
    position:absolute;
    right: 20px;
    top: 50px;
  }
  .remove,.add{
    position:absolute;
    right: 70px;
    bottom: 20px;
  }
  .none{
    display: none;
  }
  #widgets{
    position:absolute;
    right: 120px;
    bottom: 20px;
    margin: 0 5px;
    font-size: 11px;
    color: #fff;
  }
}
/deep/ .esri-ui{
  // display: none;
  position: absolute;
}
/deep/ .esri-component.esri-attribution.esri-widget{
  display: none;
}
</style>

