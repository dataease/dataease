// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/tasks/NAMessage",["dojo/_base/declare","dojo/_base/lang","dojo/has","../kernel"],function(a,b,c,d){a=a(null,{declaredClass:"esri.tasks.NAMessage",constructor:function(a){b.mixin(this,a)}});b.mixin(a,{TYPE_INFORMATIVE:0,TYPE_PROCESS_DEFINITION:1,TYPE_PROCESS_START:2,TYPE_PROCESS_STOP:3,TYPE_WARNING:50,TYPE_ERROR:100,TYPE_EMPTY:101,TYPE_ABORT:200});c("extend-esri")&&b.setObject("tasks.NAMessage",a,d);return a});