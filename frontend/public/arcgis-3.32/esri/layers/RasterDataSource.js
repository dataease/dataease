// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/RasterDataSource","dojo/_base/declare dojo/_base/lang dojo/has ../kernel ../lang ./DataSource".split(" "),function(a,b,c,d,e,f){a=a(f,{declaredClass:"esri.layers.RasterDataSource",toJson:function(){return e.fixJson({type:"raster",workspaceId:this.workspaceId,dataSourceName:this.dataSourceName})}});c("extend-esri")&&b.setObject("layers.RasterDataSource",a,d);return a});