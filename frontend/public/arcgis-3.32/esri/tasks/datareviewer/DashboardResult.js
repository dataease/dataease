// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/tasks/datareviewer/DashboardResult",["dojo/_base/declare","dojo/has","dojo/_base/lang","../../kernel"],function(a,c,d,e){a=a(null,{declaredClass:"esri.tasks.datareviewer.DashboardResult",fieldName:null,fieldValues:null,counts:null,filters:null,constructor:function(){this.fieldName="";this.fieldValues=[];this.counts=[]},getCount:function(a){if(0<this.fieldValues.length&&0<this.counts.length&&this.fieldValues.length===this.counts.length)for(var b=0;b<this.fieldValues.length;b++)if(this.fieldValues[b]===
a)return this.counts[b];return 0}});c("extend-esri")&&d.setObject("tasks.datareviewer.DashboardResult",a,e);return a});