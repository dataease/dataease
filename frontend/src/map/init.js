/* eslint-disable no-unused-vars */
/*
 * @Description  : arcgis地图部分
 */

import { loadModules, loadCss } from 'esri-loader' // 异步加载模
import config from './config' // 配置项

function ArcGIS() {
  this.map = null // 地图
  this.baseMap = null // 地图底图
}

ArcGIS.prototype.init = function init($el) {
  // 加载地图必备样式文件
  loadCss('http://localhost:9528/arcgis-3.32/esri/css/esri.css')
  loadCss('http://localhost:9528/arcgis-3.32/dijit/themes/claro/claro.css')
  // loadCss("http://localhost:9528/arcgis-3.32/esri/css/esri.css");
  // loadCss("http://localhost:9528/arcgis-3.32/dijit/themes/claro/claro.css");

  // 异步加载对应 js 模块
  loadModules(
    [
      'esri/map',
      'tdlib/SDTDTLayer',
      'tdlib/SDRasterLayer',
      'tdlib/SDRSAnnoLayer',
      'esri/geometry/Extent',
      'esri/SpatialReference',
      'dojo/parser'
    ],
    config.loadConfig
  )
    .then(
      ([
        Map, // 地图模块
        SDTDTLayer, // 山东天地图矢量地图
        SDRasterLayer, // 山东天地图影像地图
        SDRSAnnoLayer, // 山东天地图影像地图注记
        Extent, // 范围模块
        SpatialReference, // 坐标系模块
        Parser // 样式解析模块
      ]) => {
        // 设置地图地图图层
        this.baseMap = {
          vectorMap: new SDTDTLayer(), // 矢量地图
          rasterMap: new SDRasterLayer(), // 影像地图
          rasterMapAnnotation: new SDRSAnnoLayer(), // 影像注记
          type: 1 // 1 为矢量 | 2：影像
        }

        Parser.parse() // 解析

        // 设置初始化地图位置
        const startExtent = new Extent(
          ...config.startExtent,
          new SpatialReference({ wkid: 4490 })
        )

        // 添加地图实例
        this.map = new Map($el, {
          extent: startExtent, // 初始化位置
          zoom: 10, // 缩放级别
          logo: false, // esri logo
          maxZoom: 18, // 最大缩放级别
          sliderPosition: 'top-right' // 缩小放大按钮位置
        })

        // 将图层添加到地图实例上 (图层,图层层级)
        this.map.addLayer(this.baseMap.vectorMap, 0)
      }
    ) // end
    .catch((err) => {
      console.error(err)
    })
}

export default ArcGIS
