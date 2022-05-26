// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/charts/legends/ChartLegendPlacements",[],function(){var b={NONE:"None",LEFT:"Left",RIGHT:"Right",TOP:"Top",BOTTOM:"Bottom",isSupported:function(a){for(var c in b)if(b[c]===a)return!0;return!1},toSupportedValue:function(a){return b.isSupported(a)?a:b.NONE},isOnSide:function(a){return a===b.LEFT||a===b.RIGHT}};return b});