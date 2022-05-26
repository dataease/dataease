// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/srUtils",["./SpatialReference","./ImageSpatialReference","./kernel","./sniff","dojo/_base/lang"],function(e,f,g,h,k){function c(a){var b=!1;a&&(a.ics||a.icsid)&&(b=!0);return b}var d={isICS:c,createSpatialReference:function(a){var b=null;a&&(b=c(a)?new f(a):new e(a));return b}};h("extend-esri")&&k.mixin(g,d);return d});