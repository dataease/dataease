// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/tasks/MultipartColorRamp","dojo/_base/declare dojo/_base/lang dojo/_base/array dojo/has ../kernel ../symbols/Symbol ./ColorRamp".split(" "),function(a,b,c,d,e,f){a=a(f,{declaredClass:"esri.tasks.MultipartColorRamp",type:"multipart",constructor:function(){this.colorRamps=[]},addColorRamp:function(a){this.colorRamps.push(a)},toJson:function(){return{type:"multipart",colorRamps:c.map(this.colorRamps,function(a){return a.toJson()})}}});d("extend-esri")&&b.setObject("tasks.MultipartColorRamp",
a,e);return a});