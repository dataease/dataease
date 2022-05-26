// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/nlsFix",["dojo/_base/config","dojo/i18n"],function(e,f){return{load:function(k,g,d){var h=function(c,a){for(var b in c)"object"===typeof a[b]?h(c[b],a[b]):void 0===a[b]&&(a[b]=c[b])};e.locale?f.load("esri/nls/en/jsapi",g,function(c){f.load("esri/nls/"+e.locale+"/jsapi",g,function(a){h(c,a);d&&d()})}):d&&d()}}});