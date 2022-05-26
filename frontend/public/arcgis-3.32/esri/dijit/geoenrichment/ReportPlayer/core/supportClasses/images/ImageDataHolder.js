// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/supportClasses/images/ImageDataHolder",[],function(){var a={},c={},d={};a.putImageData=function(b,a){if("string"===typeof b){var e=b.toLowerCase();d[e]=b;c[e]=a}};a.getImageData=function(b){return"string"!==typeof b?null:c[b.toLowerCase()]};a.findFileNameByData=function(b){for(var a in c)if(b===c[a])return d[a];return null};return a});