// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/themes/PageBorderStyles",[],function(){var a={NONE:"none",ALL:"all",INNER_ONLY:"innerOnly",OUTER_ONLY:"outerOnly",HORIZONTAL_INNER_ONLY:"horizontalInnerOnly",VERTICAL_INNER_ONLY:"verticalInnerOnly",isSupported:function(b){for(var c in a)if(a[c]===b)return!0;return!1},toSupportedValue:function(b){return a.isSupported(b)?b:a.ALL}};return a});