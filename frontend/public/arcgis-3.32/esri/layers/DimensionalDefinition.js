// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/DimensionalDefinition",["dojo/_base/declare","dojo/_base/lang","dojo/has","../kernel","../lang"],function(a,b,c,d,e){a=a(null,{declaredClass:"esri.layers.DimensionalDefinition",variableName:null,dimensionName:null,values:[],isSlice:!1,constructor:function(a){b.isObject(a)&&b.mixin(this,a)},toJson:function(){var a={variableName:this.variableName,dimensionName:this.dimensionName,values:b.clone(this.values),isSlice:this.isSlice};return e.filter(a,function(a){return null!==a})}});
c("extend-esri")&&b.setObject("layers.DimensionalDefinition",a,d);return a});