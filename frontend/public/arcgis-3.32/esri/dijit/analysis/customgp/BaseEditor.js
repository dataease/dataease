// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/analysis/customgp/BaseEditor",["dojo/_base/declare","dojo/Deferred","dijit/_WidgetBase","dijit/_TemplatedMixin"],function(d,b,e){return d([e],{baseClass:"jimu-gp-editor-base",editorName:"BaseEditor",dependParam:"",postCreate:function(){this.inherited(arguments)},getValue:function(){},getGPValue:function(){var a=new b;a.resolve(this.getValue());return a},wrapGPValue:function(a){if(!a)return a;a.toJson||(a.toJson=function(){return a});return a},wrapValueToDeferred:function(a){var c=
new b;c.resolve(a);return c},update:function(a){}})});