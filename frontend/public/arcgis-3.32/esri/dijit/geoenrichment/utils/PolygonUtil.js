// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/utils/PolygonUtil",[],function(){var d={getNumberOfPoints:function(a){if(!a)return 0;Array.isArray(a)||(a=[a]);var b=0;a.forEach(function(a){b+=d.getGeometryPointCount(a.geometry||a)});return b},getGeometryPointCount:function(a){if(!a)return 0;var b=0,c=a.rings||a.paths;!c&&a.points&&(c=[a.points]);c?c.forEach(function(a){b+=a&&a.length||0}):b+="x"in a?1:2;return b}};return d});