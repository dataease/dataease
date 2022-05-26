// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/charts/utils/ChartLineStyles",[],function(){var a={SOLID:"Solid",DASHED:"Dashed",DOTTED:"Dotted",isSupported:function(c){for(var b in a)if(a[b]===c)return!0;return!1},toGFXValue:function(a,b){switch(a){case "Dashed":return 1>b?"LongDash":"Dash";case "Dotted":return 1>b?"Dash":"Dot";default:return"Solid"}}};return a});