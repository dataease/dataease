// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/grid/coreUtils/gridLayoutCalcUtils/rows/_RowModifierNormal",["./RowDataUtil"],function(d){return{adjustRowHeight:function(a,g,e,c){var f=a.layoutDefaults.rowMinHeight;c=Math.max(c,f);var b=c-d.getDataHeight(a,g,e);if(a.keepGridHeightWhenResized){var h=a.store.data[g.index+1];h&&(b=d.getDataHeight(a,h,e)-b,b<f&&(c-=f-b,b=f),d.setDataHeight(a,h,e,b,!0))}d.setDataHeight(a,g,e,c,!0)}}});