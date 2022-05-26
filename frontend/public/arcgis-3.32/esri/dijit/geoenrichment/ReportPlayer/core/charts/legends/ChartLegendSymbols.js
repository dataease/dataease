// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/charts/legends/ChartLegendSymbols",["../utils/ChartTypes"],function(d){var a={SQUARE:"Square",CIRCLE:"Circle",RECTANGLE:"Rectangle",LINE:"Line",isSupported:function(b){for(var c in a)if(a[c]===b)return!0;return!1},getDefaultSymbol:function(b,c){return c&&b!==d.WAFFLE?a.CIRCLE:a.SQUARE}};return a});