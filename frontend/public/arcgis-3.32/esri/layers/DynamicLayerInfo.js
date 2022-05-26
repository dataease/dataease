// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/DynamicLayerInfo","dojo/_base/declare dojo/_base/lang dojo/has ../kernel ../lang ./LayerInfo ./LayerMapSource ./LayerDataSource".split(" "),function(b,d,e,f,g,h,c,k){b=b(h,{declaredClass:"esri.layers.DynamicLayerInfo",defaultVisibility:!0,parentLayerId:-1,maxScale:0,minScale:0,constructor:function(a){a&&(a.source?a="mapLayer"===a.source.type?new c(a.source):new k(a.source):(a=new c,a.mapLayerId=this.id),this.source=a)},toJson:function(){var a=this.inherited(arguments);a.source=
this.source&&this.source.toJson();return g.fixJson(a)}});e("extend-esri")&&d.setObject("layers.DynamicLayerInfo",b,f);return b});