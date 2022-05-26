// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/LayerMapSource","dojo/_base/declare dojo/_base/lang dojo/has ../kernel ../lang ./LayerSource".split(" "),function(a,b,c,d,e,f){a=a(f,{declaredClass:"esri.layers.LayerMapSource",type:"mapLayer",toJson:function(){return e.fixJson({type:"mapLayer",mapLayerId:this.mapLayerId,gdbVersion:this.gdbVersion})}});c("extend-esri")&&b.setObject("layers.LayerMapSource",a,d);return a});