// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/tasks/DataFile",["dojo/_base/declare","dojo/_base/lang","dojo/has","../kernel"],function(b,c,d,e){b=b(null,{declaredClass:"esri.tasks.DataFile",constructor:function(a){a&&c.mixin(this,a)},url:null,itemID:null,toJson:function(){var a={};this.url&&(a.url=this.url);this.itemID&&(a.itemID=this.itemID);return a}});d("extend-esri")&&c.setObject("tasks.DataFile",b,e);return b});