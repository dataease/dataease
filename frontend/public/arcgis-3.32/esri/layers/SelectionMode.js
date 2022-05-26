// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/SelectionMode",["dojo/_base/declare","dojo/_base/lang","dojo/has","../kernel","./RenderMode"],function(a,b,c,d,e){a=a([e],{declaredClass:"esri.layers._SelectionMode",constructor:function(a){this.featureLayer=a;this._featureMap={}},propertyChangeHandler:function(a){this._init&&0===a&&this._applyTimeFilter()},resume:function(){this.propertyChangeHandler(0)},hasAllFeatures:function(){return!this.featureLayer._hasPartialSelectedFeatures},hasUpdateError:function(){return this.featureLayer._hasSelectionError}});
c("extend-esri")&&b.setObject("layers._SelectionMode",a,d);return a});