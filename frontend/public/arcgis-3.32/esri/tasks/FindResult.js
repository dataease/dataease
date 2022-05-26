// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/tasks/FindResult","dojo/_base/declare dojo/_base/lang dojo/has ../kernel ../graphic ../geometry/jsonUtils".split(" "),function(a,b,c,d,e,f){a=a(null,{declaredClass:"esri.tasks.FindResult",constructor:function(a){b.mixin(this,a);this.feature=new e(a.geometry?f.fromJson(a.geometry):null,null,a.attributes);delete this.geometry;delete this.attributes}});c("extend-esri")&&b.setObject("tasks.FindResult",a,d);return a});