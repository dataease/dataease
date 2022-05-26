// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/supportClasses/map/MapEventUtil",["dojo/on","dojo/sniff","esri/dijit/geoenrichment/utils/DeviceUtil"],function(d,c,e){return{onLayerMouseOver:function(a,f,g){var b=e.isMobileDevice();a=b?a:c("touch")?f:a;b=b?"mouse-over":c("touch")?"mouse-down, mouse-move":"mouse-move";return d(a,b,g)}}});