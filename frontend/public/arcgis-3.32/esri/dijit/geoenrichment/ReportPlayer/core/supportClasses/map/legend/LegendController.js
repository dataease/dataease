// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/supportClasses/map/legend/LegendController",["dojo/_base/declare"],function(d){return d(null,{showLegend:null,_callbacks:null,addCallback:function(a,b){var c=this;this._callbacks=this._callbacks||{};this._callbacks[b]=a;return{remove:function(){delete c._callbacks[b]}}},setLegendVisible:function(a,b){this.showLegend=a;for(var c in this._callbacks)this._callbacks[c](a,b)}})});