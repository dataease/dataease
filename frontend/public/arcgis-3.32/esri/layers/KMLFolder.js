// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/KMLFolder",["dojo/_base/declare","dojo/_base/lang","dojo/has","../kernel","../lang"],function(a,b,c,d,e){a=a(null,{declaredClass:"esri.layers.KMLFolder",constructor:function(a){b.mixin(this,a);e.isDefined(this.visibility)&&(this.visible=!!this.visibility)}});c("extend-esri")&&b.setObject("layers.KMLFolder",a,d);return a});