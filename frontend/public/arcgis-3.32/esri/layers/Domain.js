// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/Domain",["dojo/_base/declare","dojo/_base/lang","dojo/has","../kernel","../lang"],function(a,b,c,d,e){a=a(null,{declaredClass:"esri.layers.Domain",constructor:function(a){a&&b.isObject(a)&&(this.name=a.name,this.type=a.type)},toJson:function(){return e.fixJson({name:this.name,type:this.type})}});c("extend-esri")&&b.setObject("layers.Domain",a,d);return a});