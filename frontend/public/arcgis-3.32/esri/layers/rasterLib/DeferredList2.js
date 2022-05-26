// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/rasterLib/DeferredList2",["dojo/DeferredList"],function(c){return function(b,a,d,e,f){this.list=b;a=new c(b,a,d,e,f);var g=a.resolve.bind(a),h=a.cancel.bind(a);a.cancel=function(){b&&b.forEach(function(a){a.cancel&&a.cancel()});h.apply(this,arguments)}.bind(a);a.resolve=function(){this.isFulfilled()||g.apply(this,arguments)}.bind(a);return a}});