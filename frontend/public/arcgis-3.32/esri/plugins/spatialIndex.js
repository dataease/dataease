// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/plugins/spatialIndex",["../process/SpatialIndex","dojo/Deferred"],function(e,f){return{add:function(a,b){b=b||{};if(a.spatialIndex)return a.spatialIndex;if("spatialIndex"in b){if(!1!==b.spatialIndex)return a.spatialIndex=b.spatialIndex,a.spatialIndex}else{var d=new f;b.autostart=!1;a.spatialIndex=new e(b);var c=a.spatialIndex;c.autostart=!0;c.on("start",function(){-1<a.declaredClass.indexOf("Map")?c.setMap(a):c.addLayer(a);d.resolve(c)});return d}},remove:function(a){var b=a.spatialIndex;
b&&(-1<a.declaredClass.indexOf("Map")?b.unsetMap():b.removeLayer(a),a.spatialIndex=void 0)}}});