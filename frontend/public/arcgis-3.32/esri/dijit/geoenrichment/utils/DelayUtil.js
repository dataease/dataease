// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/utils/DelayUtil",["esri/dijit/geoenrichment/Deferred"],function(d){return{delay:function(a,e){var b=new d,c="function"===typeof a?a:null;setTimeout(b.resolve,("number"===typeof a?a:e)||0);return b.promise.then(function(){return c&&c()})}}});