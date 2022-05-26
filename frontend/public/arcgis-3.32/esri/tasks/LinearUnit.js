// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/tasks/LinearUnit",["dojo/_base/declare","dojo/_base/lang","dojo/has","../kernel"],function(b,c,d,e){b=b(null,{declaredClass:"esri.tasks.LinearUnit",constructor:function(a){a&&c.mixin(this,a)},distance:0,units:null,toJson:function(){var a={};this.distance&&(a.distance=this.distance);this.units&&(a.units=this.units);return a}});d("extend-esri")&&c.setObject("tasks.LinearUnit",b,e);return b});