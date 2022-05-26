// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/supportClasses/map/MapLoadTracker",["esri/dijit/geoenrichment/Deferred","esri/dijit/geoenrichment/when","dojo/on"],function(h,a,k){return{waitForLoad:function(b){function c(a,c,l){function g(a){d&&d.remove();e&&clearTimeout(e);a||!l?f.resolve():f.reject(Error("The map can't be loaded."))}var f=new h,d,e;d=k.once(b,a,function(){g(!0)});e=setTimeout(function(){g(!1)},c);return f.promise}return a(!b.loaded&&c("load",6E4,!0),function(){return a(b.updating&&
c("update-end",3E4,!1),function(){return b})})}}});