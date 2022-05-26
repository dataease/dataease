// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/charts/utils/ChartStyleUtil",["dojo/_base/lang"],function(f){var e={},g={left:1,right:1,top:1,bottom:1,width:1,height:1,fontSize:1};e.getStyleObjWithUnits=function(a,b){b=b||"px";a=f.mixin({},a);for(var d in a)if(g[d]){var c=a[d];!c||"string"===typeof c&&-1!==c.indexOf(b)||(a[d]=c+b)}return a};return e});